[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# An example to show how to migrate active process instances from a cluster to another

Many customers are moving from Self-Managed Camunda 8 clusters to Camunda cloud. A recurring question is how to migrate our running instances to the cloud. The purpose of this project is to show, in a simple manner, how that could be achieved.

The idea is simply to read active process instances from the source operate and start a new instance in the target zeebe cluster, starting at the current active task. If the process instance has multiple active tasks, all the tasks will be activated in the target instance. the target instance will also have a new variable doing a reference to the source process instance. The migration will ensure to start the new process instance with the same definition as the source.

:information_source: In case the process instance is active on call activities, the tool will add a "migrate" catch event right after the "start event". This migrate catch event is associated with a [worker]((src/main/java/org/example/camunda/worker/MigrationWorker.java) that will restore the original activated elements of the sub process and will cancel itself. 

:information_source: This project is kind of "naive" and it may be more difficult in real life, especially if your process instances use sequential or parallel expanded subprocesses.

## Repository content

This repository contains a Java application for Camunda Platform 8 using Spring Boot
and a [docker-compose.yaml](docker-compose.yaml) file for local development. For production setups we recommend to use our [helm charts](https://docs.camunda.io/docs/self-managed/platform-deployment/kubernetes-helm/).

- [Documentation](https://docs.camunda.io)
- [Camunda Platform SaaS](https://camunda.io)
- [Getting Started Guide](https://github.com/camunda/camunda-platform-get-started)
- [Releases](https://github.com/camunda/camunda-platform/releases)
- [Helm Charts](https://helm.camunda.io/)
- [Zeebe Workflow Engine](https://github.com/camunda/zeebe)
- [Contact](https://docs.camunda.io/contact/)

The Spring Boot Java application includes an [Angular front-end](src/main/front/). Run `make buildfront` from the project root, start the Spring Boot app (`make run` or `mvnw spring-boot:run`), and then browse to http://localhost:8080.

If needed, you can also run the [Angular front-end](src/main/front/) independent of the spring boot app. To do so, run `npm run start` to start a nodejs server serving over port 4200. You can also use the `make runfront`


## First steps with the application

The application requires 2 running clusters, a source and a target.
You can run Zeebe locally using the instructions below for Docker Compose
or have a look at our
[recommended deployment options for Camunda Platform](https://docs.camunda.io/docs/self-managed/platform-deployment/#deployment-recommendation.).

Before starting the app go to http://localhost:8084/applications/
and create application of type M2M with read/write access to Operate
and set `operate.selfmanaged.clientId` and `operate.selfmanaged.clientSecret` in [application.yaml](/src/main/resources/application.yaml).

Run the application via
```
./mvnw spring-boot:run
```

UI [http://localhost:8080/](http://localhost:8080/)
Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
