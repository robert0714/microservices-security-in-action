## Chapter 9: Securing reactive microservices

<img src="../cover.jpeg" style="float: left; width: 100%" />

[Amazon](https://www.amazon.com/Microservices-Security-Action-Prabath-Siriwardena/dp/1617295957/) | [Manning](https://www.manning.com/books/microservices-security-in-action) | [YouTube](https://www.youtube.com/channel/UCoEOYnrqEcANUgbcG-BuhSA) | [Slack](https://bit.ly/microservices-security) | [Notes](../notes.md) | [Supplementary Readings](../supplementary-readings.md)

* **Page 204**, To see whether the topic was created successfully, you can execute the following command
in your command-line client. It should list the topic that we just created in the
output:
```bash
$ docker exec -it kafka   kafka-topics.sh --list --bootstrap-server localhost:9092

firsttopic
```

Now that Kafka is up and running and a topic is created on it, we can start to send and
receive messages from Kafka. Open a new tab in your command-line client from the
kafka_home directory and execute the following command to start a console process
that we can type a message into:

```bash
\> docker exec -it kafka   kafka-console-producer.sh --broker-list localhost:9092 --topic firsttopic
```
This returns a prompt so you can type your messages. Before we start typing in any
message, though, let’s start a message consumer process as well, so we can observe the
messages being typed in. Open a new terminal tab from the kafka_home directory
and execute this command to start a consumer process:

```bash
\> docker exec -it kafka   kafka-console-consumer.sh --bootstrap-server localhost:9092 \
--topic firsttopic --from-beginning
```
Now go back to the previous prompt and start typing in messages. Press the Enter key
after each message. You should notice that the messages you type appear on the terminal
tab on which you started the consumer process. What happens here is that your
first command prompt delivers a message to a topic named ***firsttopic***.