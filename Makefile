.PHONY: all
all: buildall build-docker-image run

	
.PHONY: build-docker-image
build-docker-image:
	docker build -t camunda-community/loader .
	
.PHONY: run
run:
	docker-compose up -d

runfront:
	cd src/main/front; npm run start

buildall: buildfront package

buildfront:
ifeq ("$(wildcard src/main/front/node_modules)","")
	cd src/main/front; npm install
endif
	cd src/main/front; npm run build
	-rm -rf src/main/resources/static
	cp -r src/main/front/dist/front src/main/resources/static
	-rm -rf target

package:	
	./mvnw clean package

runjava:
	./mvnw spring-boot:run

npminstall:
	cd src/main/front; npm install
