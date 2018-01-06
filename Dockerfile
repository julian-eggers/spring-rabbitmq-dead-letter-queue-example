FROM openjdk:9-jre-slim
EXPOSE 8080
ADD target/spring-rabbitmq-dead-letter-queue-example.jar spring-rabbitmq-dead-letter-queue-example.jar
ENTRYPOINT ["java", "-jar", "-Dfile.encoding=UTF-8", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "spring-rabbitmq-dead-letter-queue-example.jar"]
