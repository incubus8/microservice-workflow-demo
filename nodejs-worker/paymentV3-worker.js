const rp = require('request-promise-native');
const { Client, Variables } = require('camunda-external-task-client-js');

const config = { baseUrl: 'http://localhost:8200/rest/engine/default/', interval: 50 };
const client = new Client(config);

const briapiUrl = 'http://localhost:8000/charge';

client.subscribe("paymentV3", async function ({ task, taskService }) {

  const amount = task.variables.get("amount");
  if (!amount) {
    await taskService.handleBpmnError(task, "Error_NoRetries", "Error invalid amount");
  }

  const body = {
    url: briapiUrl,
    body: {
      amount: 15
    },
    method: 'POST',
    timeout: 1000, // timeout in one second
    json: true
  };

  const response = await rp(body);
  console.log('Got :', response);

  // In this demo, you would need to maintain retry state
  // For the demo purpose, we simulate no more retry left
  if (response.errorCode) {
    await taskService.handleBpmnError(task, "Error_NoRetries", "Error no retry left");
  } else {
    const processVariables = new Variables();
    processVariables.set('paymentTransactionId', response.transactionId);

    await taskService.complete(task, processVariables);
  }
});

client.subscribe("paymentV3cancel", async function ({ task, taskService }) {
  console.log('Perform payment cancellation!');
  await taskService.complete(task);
});

console.log('Listen to camunda \'paymentV3\' and \'paymentV3cancel\'');
