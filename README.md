[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)


# Camunda Platform 8 example for long term auditability

This project is made to provide an example of storing Camunda Operate information in a relational database and displaying it in a custom front-end using the bpmn-js library (as Operate)

:information_source: This is a community project that you can use during exploration phase, PoCs, trainings, etc. It's **not production ready** and you should not use it in production.

## Repository content

This repository contains a [Java application](auditapp) that stores Operate history in a H2 db and display it in a custom front-end. This is built with Spring Boot, the Operate Client and a [React front-end](auditapp/src/main/front/) that you can execute independently (npm run start) or serve from the spring boot application (you should first run a `mvnw package` at the project root).

It also contains an [exporter](exporter) that triggers an endpoint from the auditapp application to store the process instance history once completed.

Finally, there is a Makefile to execute this example on a local setup. The Makefile will :
- package the 2 java projects. 
- It will build a docker image from the auditapp application.
- it will start the docker-compose that contains a Camunda 8 platform preconfigured to have the custom exporter in zeebe and the auditapp running in the same cluster.

## First steps with the application

The easiest is to use the Makefile.
```
make
```

To do so, you need a proper JDK, Make, docker-compose.

If you don't change any configuration, you should be able to access the audit app UI at [http://localhost:8090/](http://localhost:8090/)
The credentials are demo/demo.

To start populating your application, you should start and complete process instances against your cluster.

## Configurations
To make the audit app works on SaaS clusters (or on a zeebe engine that you don't want to extend with a custom exporter), there is a configuration available in the yaml file :
```
operate.sync.scheduled: true
```

In such case:
- the audit endpoint exposed to the exporter will discard incoming messages.
- A scheduled task (every 4 hours) is executed to get all completed instances by chunks (100) from the Operate's API. The last "sortValues" is stored in the DB as the next sync point. 