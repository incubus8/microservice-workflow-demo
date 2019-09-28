package id.co.bri.demo.controller;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import id.co.bri.demo.adapter.NotifySemaphorAdapter;

/**
 * Step4: Use Camunda state machine for long-running retry, NodeJS external task
 * & compensation
 */
@RestController
public class PaymentV4Controller {

  @Autowired
  private ProcessEngine camunda;

  @RequestMapping(path = "/api/payment/v4", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "08122XYZABCDEF"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    Semaphore newSemaphore = NotifySemaphorAdapter.newSemaphore(traceId);
    ProcessInstance pi = chargeCreditCard(traceId, customerId, amount);
    boolean finished = newSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
    NotifySemaphorAdapter.removeSemaphore(traceId);

    if (finished) {
      boolean failed = camunda.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(pi.getId())
          .activityId("EndEvent_PaymentFailed")
          .count() > 0;
      if (failed) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "{\"status\":\"failed\", \"traceId\": \"" + traceId + "\"}";
      }

      HistoricVariableInstance historicVariableInstance = camunda.getHistoryService()
          .createHistoricVariableInstanceQuery().processInstanceId(pi.getId())
          .variableName("paymentTransactionId")
          .singleResult();
      if (historicVariableInstance != null) {
        String paymentTransactionId = (String) historicVariableInstance.getValue();
        return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"paymentTransactionId\": \""
            + paymentTransactionId + "\"}";
      }

      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"payedByCredit\": \"true\"}";
    }

    response.setStatus(HttpServletResponse.SC_ACCEPTED);
    return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
  }

  public ProcessInstance chargeCreditCard(String traceId, String customerId, long remainingAmount) {
    return camunda.getRuntimeService().startProcessInstanceByKey("paymentV4", traceId,
        Variables.putValue("amount", remainingAmount));
  }

}