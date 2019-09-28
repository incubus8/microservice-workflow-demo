const rp = require('request-promise-native');
const { Client, Variables } = require('camunda-external-task-client-js');

const config = { baseUrl: 'http://localhost:8200/rest/engine/default/', interval: 50};
const client = new Client(config);

const briapiUrl = 'http://localhost:8000/charge';

client.subscribe("paymentV2", async function({ task, taskService }) {
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

  const processVariables = new Variables();
  processVariables.set('paymentTransactionId', response.transactionId);

  await taskService.complete(task, processVariables);
});

console.log('Listen to camunda \'paymentV2\'');
