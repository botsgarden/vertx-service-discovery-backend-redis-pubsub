#!/usr/bin/env bash
mvn clean package
mvn install:install-file -Dfile=target/vertx-service-discovery-backend-redisson-1.0-SNAPSHOT.jar \
-DgroupId=org.typeunsafe \
-DartifactId=vertx-service-discovery-backend-redisson \
-Dversion=1.0-SNAPSHOT  \
-Dpackaging=jar
