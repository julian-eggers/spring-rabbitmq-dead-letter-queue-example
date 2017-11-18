FROM openjdk:8-jre-alpine

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring-rabbitmq-dead-letter-queue-example.jar"]

ADD target/spring-rabbitmq-dead-letter-queue-example.jar spring-rabbitmq-dead-letter-queue-example.jar