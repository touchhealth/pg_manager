FROM maven:3.8.4-jdk-11-slim AS build_pgman
RUN mkdir -p /usr/src/mymaven
COPY ./src /usr/src/mymaven/src
COPY ./pom.xml /usr/src/mymaven
RUN mvn -f /usr/src/mymaven/pom.xml clean package

FROM openjdk:11.0-jre-slim-buster
RUN mkdir -p /opt/pgman
COPY dockerfiles/config.yml dockerfiles/pgman /opt/pgman/
COPY --from=build_pgman /usr/src/mymaven/target/pgman-1.4.0.jar /opt/pgman/pgman-1.4.0.jar

ENTRYPOINT ["/opt/pgman/pgman", "run"]
