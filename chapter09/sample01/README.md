## Chapter 9: Securing reactive microservices (sample01)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample01](https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample01)

### 9.3 Developing a microservice to push events to a Kafka topic

* **Page 205**, Once the Order Processing microservice starts, you should see a topic named ORDERS
being created on Kafka. You can verify that it’s there by listing the topics in Kafka. Execute
the following command in a new terminal window from within the kafka_home
location:

```bash
$ docker exec -it kafka   kafka-topics.sh --list --bootstrap-server localhost:9092
```

Let’s now open a new console process that prints the messages sent to the ORDERS topic.
As in section 9.2, we execute this command from within the kafka_home directory:

```bash
$ docker exec -it kafka  kafka-console-consumer.sh --bootstrap-server localhost:9092 \
--topic ORDERS --from-beginning
```

Once the console process is running, we can send a message to the Order Processing
microservice and observe its behavior. Open a new terminal window and navigate to
the sample01 directory and execute the following curl command to send a request
to the Order Processing microservice to place an order:

```bash
\> curl http://localhost:8080/order -X POST -d @order.json \
-H "Content-Type: application/json" -v
```
The order.json file (which is inside the sample01 directory) contains the message (in
JSON format) that we sent to the Order Processing microservice. If the order was placed
successfully, you should get an HTTP 201 response code to the request. If you observe
the console process that prints messages on the ***ORDERS*** topic, you should see that the
content within the order.json file (request payload) has been printed on the console.
