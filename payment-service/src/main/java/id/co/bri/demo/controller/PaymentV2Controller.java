package id.co.bri.demo.controller;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Step2: Use Camunda state machine for long-running retry
 */
@RestController
public class PaymentV2Controller {

  @Autowired
  private ProcessEngine camunda;

  @RequestMapping(path = "/api/payment/v2", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "08122XYZABCDEF"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    chargeDebitCard(customerId, amount);
    
    response.setStatus(HttpServletResponse.SC_ACCEPTED);    
    return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
  }

  public void chargeDebitCard(String customerId, long remainingAmount) {
    ProcessInstance pi = camunda.getRuntimeService()
        .startProcessInstanceByKey("paymentV2",
            Variables.putValue("amount", remainingAmount));    
  }
  
  public static class CreateChargeRequest {
    public long amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

}