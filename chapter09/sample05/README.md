## Chapter 9: Securing reactive microservices (sample05)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample05](https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample05)

### 9.8 Setting up NATS as a message broker

* **Page 223**, We will be using the NATS Docker image for this, so Docker is a prerequisite to
run this sample. Setting up a NATS server through Docker is simple. If you have
Docker installed, you just have to open a command-line terminal session and execute
the following command:
```bash
\> docker run -p 4222:4222 -p 8222:8222 -p 6222:6222 \
--name nats-server -ti nats:latest
```

If this is the first time you are running this command, this will download the NATS
server Docker image to your local Docker repository and run the Docker container.
You should see the following messages after the process has started:
```bash
Listening for client connections on 0.0.0.0:4222
Server is ready
Listening for route connections on 0.0.0.0:6222
```

We are now ready to connect to the NATS server for sending and receiving messages.
To stop the NATS server, you can execute the following command in a new terminal tab:

```bash
\> docker stop nats-server
```

To start the NATS server again, you can execute the following command:

```bash
\> docker start nats-server
```

Now that we have the NATS server up and running, the next step is to execute the
NATS client and server, which communicate with each other. The code of the client
and server can be found in the ***chapter09/sample05*** directory. Open a new commandline
terminal, navigate to the ***sample05/natssub*** directory, and execute the following
command to compile the code of the subscriber (message receiver):

```bash
\> mvn clean package
```
Once the code is built, you should see a BUILD SUCCESS message on the terminal. You
should also see a directory named target being created within the natssub directory.

* **Page 224**, Execute the following command on your terminal from within the natssub directory
to run the NATS subscriber process:
```bash
\> java -jar target/com.manning.mss.ch09.sample05.natssub-1.0.0.jar
```

This will start the NATS subscriber process, which will be listening on NATS for messages.
Next, open a new command-line terminal, navigate to the ***sample05/natspub***
directory, and execute the following command to compile the code of the NATS publisher
process:
```bash
\> mvn clean package
```

Once the build is successful, execute the following command to run the NATS publisher:
```bash
\> java -jar target/com.manning.mss.ch09.sample05.natspub-1.0.0.jar
```

This process will send a message to the NATS server. The NATS subscriber process should receive the message that was sent. If you observe the terminal on which you ran the NATS subscriber process, you should now see a message as follows:

```bash
Received message: Welcome to NATS
```

To take a look at the code that produced the message, open the file ***sample05/natspub/src/main/java/com/manning/mss/ch09/sample05/natspub/NatsPublisher.java*** with a text editor. You will see that the code in the following listing performs this action.
```java
try {
    //Connects to the NATS server
    natsConnection = Nats.connect("nats://localhost:4222");

    //Pushes message to subject "updates"
    natsConnection.publish("updates", "Welcome to 
       NATS".getBytes(StandardCharsets.UTF_8));

     //Makes sure the message goes through before we close  
    natsConnection.flush(Duration.ZERO);
    }
```

Similarly, open the file ***sample05/natssub/src/main/java/com/manning/mss/ch09/sample05/natssub/NatsSubscriber.java*** with a text editor and observe the code in the following listing that prints the message received via NATS.
```java
try {
    //Connects to the NATS server
    natsConnection = Nats.connect("nats://localhost:4222");

    //Subscribes to subject "updates"
    Subscription sub = natsConnection.subscribe("updates");
    Message msg = sub.nextMessage(Duration.ZERO);

    String str = new String(msg.getData(), StandardCharsets.UTF_8);
    System.out.print("Received message: ");

    //Prints the received message
    System.out.println(str);
}
```

Similar to the options we discussed in securing a Kafka server, the connections to a
NATS server can be protected for confidentiality with TLS, and we can use mTLS or
JWT to authenticate client applications, which connect to the NATS server. We did not
talk about NATS security in detail in this book, but you can always refer to the NATS
documentation at ***https://docs.nats.io/nats-server/configuration/securing_nats***.
