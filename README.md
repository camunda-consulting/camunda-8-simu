[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)


# Camunda Platform 8 data simulator

This project is made to populate data into a C8 installation to provide meaningfull data in Optimize, Operate and Tasklist.

:information_source: This is a draft project. It's **not production ready** and you should not use it in production.

## Repository content

This repository contains a [Java application](src/main/java) built with Spring Boot and an [Angular front-end](src/main/front/) that you can execute independently (npm run start) or serve from the spring boot application (you should first run a `mvnw package` at the project root).

Finally, there is a Makefile to execute this example on a local setup. The Makefile will :
- package the front-end and java project. 
- build a docker image from the auditapp application.
- start the docker-compose that contains a Camunda 8 platform and the loader running in the same cluster.

## First steps with the application

The easiest is to use the Makefile.
```
make
```

To do so, you need a proper JDK, Make, docker-compose.

If you don't change any configuration, you should be able to access the Simulator UI at [http://localhost:8080/](http://localhost:8080/).

To start populating your application, you should execute a plan.

## Other options
You can also build and run the simulation application locally 
```
make buildall
```

```
make runjava
```
Just pay attention to the configurations to connect to Operate and Zeebe in the [application.yaml](src/main/resources/application.yaml) file.

You can connect it to any Self-Managed cluster. Just pay attention to :
- add the configuration ZEEBE_CLOCK_CONTROLLED=true in Zeebe
- Create the client/secret that allows the Simulation application to speak with Operate (from Identity UI or from configs):
```
KEYCLOAK_CLIENTS_1_NAME: dataloader
KEYCLOAK_CLIENTS_1_ID: dataloader
KEYCLOAK_CLIENTS_1_SECRET: Tcx9kFqVwxlnWMZOhjYCemnLvBr1Gvs3
KEYCLOAK_CLIENTS_1_TYPE: M2M
KEYCLOAK_CLIENTS_1_PERMISSIONS_0_RESOURCE_SERVER_ID: operate-api
KEYCLOAK_CLIENTS_1_PERMISSIONS_0_DEFINITION: read:*
```