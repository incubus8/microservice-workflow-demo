
## Microservice Pattern with Workflow Engine using NodeJS

Demo camunda using NodeJS as External Task Demo for JSDay 2019. This example is a simplification for JS Day 2019 taken from Berndruecker Flowing Retail demo https://github.com/berndruecker/flowing-retail/


### How to run

1. Open new terminal, start briapi mock server first

```shell
$ cd briapi-mock
$ mvn clean install
$ mvn exec:java
```

2. Open new terminal, start nodejs worker for payment demo.

```shell
$ cd nodejs-worker
$ yarn

# To run payment v2 worker
$ node paymentV2-worker.js

# To run payment v4
$ node paymentV4-worker.js
```

3. Open new terminal, start camunda and rest controller

```shell
$ cd payment-service
$ mvn clean install
$ mvn exec:java
```


----

Credits to:
1. https://github.com/berndruecker/flowing-retail/
2. https://camunda.com

