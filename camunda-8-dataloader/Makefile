all: buildfront run

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
	mvnw clean package

run:
	mvnw spring-boot:run

npminstall:
	cd src/main/front; npm install
  
builddockerimage:
	docker build -t camunda-community/camunda-data-loader .

rundockerimage:
	docker run -p 8888:8080 camunda-community/camunda-data-loader