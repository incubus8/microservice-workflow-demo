const rp = require('request-promise-native');
const { Client, Variables } = require("camunda-external-task-client-js");

const config = { baseUrl: 'http://localhost:8200/rest/engine/default/', interval: 50 };
const client = new Client(config);

const briapiUrl = 'http://localhost:8000/charge';

client.subscribe("customer-credit", async function({ task, taskService }) {
  
  // simulate customer credit for demo
  let remainingAmount = 0;
  if (Math.random() > 0.5) {
    remainingAmount = 15;
  }

  const processVariables = new Variables();
  processVariables.set("remainingAmount", remainingAmount);

  console.log('[%s] done for process instance %s with remainingAmount=%s', task.topicName, task.processInstanceId, remainingAmount);
  await taskService.complete(task, processVariables);
});

client.subscribe("customer-credit-refund", async function({ task, taskService }) {
  console.log('[%s] done for process instance %s', task.topicName, task.processInstanceId);
  await taskService.complete(task);
});

client.subscribe("charge-debit-card", async function({ task, taskService }) {
  const amount = task.variables.get("amount") || 0;

  const body = {
    url: briapiUrl,
    body: {
      amount
    },
    method: 'POST',
    timeout: 1000, // timeout in one second
    json: true
  };

  const response = await rp(body)
  console.log('Got: ', response);

  if (response.errorCode) {
    await taskService.handleBpmnError(task, "Error_CreditCardError", "Error no retry left");
  } else {
    const processVariables = new Variables();
    processVariables.set('paymentTransactionId', response.transactionId);

    await taskService.complete(task, processVariables);
  }
});

console.log('Listen to camunda \'customer-credit\', \'customer-credit-refund\', and \'charge-debit-card\'');
