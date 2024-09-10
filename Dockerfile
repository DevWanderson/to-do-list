# Etapa de construção
FROM ubuntu:latest

RUN apt-get update && apt-get upgrade -y
RUN apt-get install openjdk-17-jdk maven -y

WORKDIR /app
COPY . .
RUN mvn clean install

ENTRYPOINT ["java", "-jar", "/app/target/todolist-0.0.1-WA.jar"]
