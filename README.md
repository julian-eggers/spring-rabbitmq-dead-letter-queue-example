RabbitMQ Dead Letter Queue Example with Spring
==============================================

## Concept
- Every queue has its dead letter queue
- Dead letter messages are routed directly to the dead letter queue, not via bindings
- Dead letter queues are [lazy](https://www.rabbitmq.com/lazy-queues.html) to save resources

This is different to the [rabbitmq-concept](https://www.rabbitmq.com/dlx.html) which recommends to use a separate exchange to route the dead letter messages.


## Queue Properties

### Queue
- x-dead-letter-exchange=
- x-dead-letter-routing-key=DLQ-Name

### Dead letter queue
- x-queue-mode=lazy



## Docker Example
Run the following command to start a container which creates example-queues and sends messages to them.
It tries to connect to a rabbitmq-server on localhost with username "guest" and password "guest".

```
docker run \
-d \
--name=spring-rabbitmq-dead-letter-queue-example \
jeggers/spring-rabbitmq-dead-letter-queue-example:latest \
--spring.rabbitmq.addresses=localhost \
--spring.rabbitmq.username=guest \
--spring.rabbitmq.password=guest
```
