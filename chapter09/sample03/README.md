## Chapter 9: Securing reactive microservices (sample03)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample03](https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample03)

### 9.5.1 Creating and signing the TLS keys and certificates for Kafka

* **Page 211**,To spin up the OpenSSL Docker container,run the following command from the chapter09/keys directory.
```bash
\> docker run -it -v $(pwd):/export prabath/openssl
#
```

### 9.5.2 Configuring TLS on the Kafka server

* **Page 212**, To enable TLS on Kafka, first make sure the Kafka server is shut down if it’s already
running, but keep the ZooKeeper server running (from section 9.2). You need to
press Ctrl-C on your keyboard on the respective command-line terminal process. After
the process shuts down, use your command-line client tool or file explorer to navigate
to the kafka_home/config directory. Open the server.properties file by using your text
editor of choice and add the following properties to the file.

```server.properties
listeners=PLAINTEXT://:9092,SSL://:9093
ssl.keystore.location=chapter09/keys/kafka_server/kafka_server.jks
ssl.keystore.password=manning123
ssl.enabled.protocols=TLSv1.2,TLSv1.1,TLSv1
ssl.keystore.type=JKS
ssl.secure.random.implementation=SHA1PRNG
```
Or modify the docker-compose.yml
```yaml
version: '2'
services:
  (ommited....)
  kafka:
    container_name: kafka
    image: wurstmeister/kafka:2.12-2.5.0
    network_mode: host
    environment:
      KAFKA_LISTENERS: 'PLAINTEXT://0.0.0.0:9092,SSL://0.0.0.0:9093'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://localhost:9092,SSL://localhost:9093'
      KAFKA_SSL_KEYSTORE_LOCATION: 'keys/kafka_server/kafka_server.jks'      
      KAFKA_SSL_KEYSTORE_PASSWORD: 'manning123'
      KAFKA_SSL_ENABLED_PROTOCOLS: 'TLSv1.2,TLSv1.1,TLSv1'
      KAFKA_SSL_KEYSTORE_TYPE: JKS
      KAFKA_SSL_SECURE_RANDOM_IMPLEMENTATION: SHA1PRNG
```
### 9.5.3 Configuring TLS on the microservices
* **Page 214**,Open a new terminal tab on your command-line client, navigate to the sample03/orders_ms directory, and execute the following command:
```bash
\> curl -k https://localhost:8080/order -X POST -d @order.json \
-H "Content-Type: application/json" -v
```
If you observe the console output of the terminal tab on which you ran the Buying
History microservice, you should see this output:

```bash
Updated buying history of customer with order: <order_id>
```

The preceding output means that the curl client has talked successfully to the Order
Processing microservice over TLS, and then the Order Processing microservice has
published an event to the ***ORDERS*** topic on Kafka over TLS, and finally, the Buying History
microservice has read the order details from the ***ORDERS*** topic on Kafka over TLS.

### 9.6 Using mTLS for authentication

* **Page 216**,Then open the server.properties
file located at kafka_home/config with your text editor.

```properties
ssl.truststore.location=chapter09/keys/kafka_server/truststore.jks
ssl.truststore.password=manning123
```
Or modify the docker-compose.yml
```yaml
version: '2'
services:
  (ommited....)
  kafka:
    container_name: kafka
    image: wurstmeister/kafka:2.12-2.5.0
    network_mode: host
    environment:
      KAFKA_SSL_TRUSTSTORE_LOCATION: 'keys/kafka_server/truststore.jks'      
      KAFKA_SSL_TRUSTSTORE_PASSWORD: 'manning123'
      KAFKA_SSL_CLIENT_AUTH: 'required'
```
Change the value of the property ssl.client.auth to required (in the same
server.properties file), as shown next.

```properties
ssl.client.auth=required
```

The next step would be to configure the ***Order Processing*** and ***Buying History***
microservices to be able to authenticate themselves when connecting to the Kafka
server.Open the application.properties file located in the chapter09/
samole03/orders_ms/src/main/resources directory.
```properties
spring.kafka.ssl.key-store-location=file:orderprocessing.jks  
spring.kafka.ssl.key-store-password=manning123  
spring.kafka.ssl.key-store-type=JKS 
```
.Open the application.properties file located in the chapter09/
samole03/buying_history_ms/src/main/resources directory.
```properties
spring.kafka.ssl.key-store-location=file:buyinghistory.jks
spring.kafka.ssl.key-store-password=manning123
spring.kafka.ssl.key-store-type=JKS
```

Perform the same steps to build and run the Buying History microservice from the
sample03/buying_history_ms directory and then execute the following curl command
from sample03/orders_ms to make an order request to the Order Processing
microservice:
```bash
\> curl -k https://localhost:8080/order -X POST -d @order.json \
-H "Content-Type: application/json" -v
```
You should receive a successful response from this command, and you should notice
the following message printed on the terminal tab that’s running the Buying History
microservice:
```bash
Updated buying history of customer with order: <order_id>
```

### 9.7.1 Enabling ACLs on Kafka and identifying the clients

* **Page 219**,Open the server.properties file located at kafka_home/config with your text editor
and add the following properties to it:

```properties
authorizer.class.name=kafka.security.auth.SimpleAclAuthorizer
allow.everyone.if.no.acl.found=true
ssl.principal.mapping.rules=RULE:^CN=(.*?)$/$1/L,DEFAULT
```

Or modify the docker-compose.yml
```yaml
version: '2'
services:
  (ommited....)
  kafka:
    container_name: kafka
    image: wurstmeister/kafka:2.12-2.5.0
    network_mode: host
    environment:
      KAFKA_AUTHORIZER_CLASS_NAME: 'kafka.security.auth.SimpleAclAuthorizer'
      KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND: 'true'
      KAFKA_SSL_PRINCIPAL_MAPPING_RULES: 'RULE:^CN=(.*?)$/$1/L,DEFAULT'
```
### 9.7.2 Defining ACLs on Kafka
* **Page 220**,Open your command-line tool, navigate to the kafka_home directory, and execute the
following command:

```bash
\> docker exec -it kafka  kafka-acls.sh --authorizer-properties \
zookeeper.connect=localhost:2181 \
--add --allow-principal User:"orders.ecomm.com" --producer --topic ORDERS
```

This shows you output similar to the following:

```bash
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=ORDERS, patternType=LITERAL)`:
        (principal=User:orders.ecomm.com, host=*, operation=WRITE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=CREATE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=DESCRIBE, permissionType=ALLOW)

Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=ORDERS, patternType=LITERAL)`:
        (principal=User:orders.ecomm.com, host=*, operation=WRITE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=CREATE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=DESCRIBE, permissionType=ALLOW)
```

* **Page 221**,In case you want to
remove the ACL we just created, you can use the following command, but don’t run it
until we complete this chapter:

```bash
\> docker exec -it kafka  kafka-acls.sh --authorizer-properties \
zookeeper.connect=localhost:2181 --remove \
--allow-principal User:"orders.ecomm.com" --producer --topic ORDERS
```

Next, execute the following command to enable read access to the ORDERS topic for
our Buying History microservice that has a certificate with the CN bh.ecomm.com:

```bash
\> docker exec -it kafka  kafka-acls.sh --authorizer-properties \
zookeeper.connect=localhost:2181 --add \
--allow-principal User:"bh.ecomm.com" --operation Read --topic ORDERS
```
This shows you output similar to the following:
```bash
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=ORDERS, patternType=LITERAL)`:
        (principal=User:bh.ecomm.com, host=*, operation=READ, permissionType=ALLOW)
```
If you want to remove the ACL we just created, you can use the following command,
but don’t run it until we complete this chapter:
```bash
\>  docker exec -it kafka  kafka-acls.sh --authorizer-properties \
zookeeper.connect=localhost:2181 --remove \
--allow-principal User:"bh.ecomm.com" --operation Read --topic ORDERS
```

You can list the ACLs on the ORDERS topic by executing the following command:
```bash
\>  docker exec -it kafka  kafka-acls.sh --authorizer-properties \
zookeeper.connect=localhost:2181 --list --topic ORDERS
```

This displays all the ACLs applied on the ORDERS topic. You should see output similar
to the following:
```bash
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=ORDERS, patternType=LITERAL)`:
        (principal=User:bh.ecomm.com, host=*, operation=READ, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=WRITE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=CREATE, permissionType=ALLOW)
        (principal=User:orders.ecomm.com, host=*, operation=DESCRIBE, permissionType=ALLOW)
```

* **Page 222**,To test how ACLs work with Kafka, run the following curl command from the
***chapter09/sample03/orders_ms*** directory to make an order request to the Order Processing
microservice:
```bash
\> curl -k https://localhost:8080/order -X POST -d @order.json \
-H "Content-Type: application/json" -v
```
You should receive a successful response from this command. Notice the following
message printed on the terminal tab that’s running the Buying History microservice:

```bash
Updated buying history of customer with order: <order_id>
```