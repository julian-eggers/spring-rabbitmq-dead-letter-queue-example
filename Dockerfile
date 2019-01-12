FROM adoptopenjdk/openjdk11:alpine-slim
EXPOSE 8080
ADD target/spring-rabbitmq-dead-letter-queue-example.jar spring-rabbitmq-dead-letter-queue-example.jar
ENTRYPOINT ["java", "-jar", "-Dfile.encoding=UTF-8", "-Djava.security.egd=file:/dev/./urandom", "spring-rabbitmq-dead-letter-queue-example.jar"]
