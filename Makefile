.PHONY: all
all: build-jars build-loader-docker-image run

.PHONY: build-jars
build-jars:
	cd exporter; ./mvnw clean package
	cd camunda-8-dataloader; make buildall
	
.PHONY: build-audit-docker-image
build-loader-docker-image:
	cd camunda-8-dataloader; docker build -t camunda-community/loader .
	
.PHONY: run
run:
	docker-compose up -d