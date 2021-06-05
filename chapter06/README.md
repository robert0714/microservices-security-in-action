## Chapter 6: Securing east/west traffic with certificates

<img src="../cover.jpeg" style="float: left; width: 100%" />

[Amazon](https://www.amazon.com/Microservices-Security-Action-Prabath-Siriwardena/dp/1617295957/) | [Manning](https://www.manning.com/books/microservices-security-in-action) | [YouTube](https://www.youtube.com/channel/UCoEOYnrqEcANUgbcG-BuhSA) | [Slack](https://bit.ly/microservices-security) | [Notes](../notes.md) | [Supplementary Readings](../supplementary-readings.md)


# Notes

## 6.2.1 Creating a certificate authority
You need a key pair for your CA, the Order Processing
microservice, and the Inventory microservice. Create a directory structure as follows:

```bash
\> mkdir –p keys/ca
\> mkdir –p keys/orderprocessing
\> mkdir –p keys/inventory
```

## 6.2.4 Using a single script to generate all the keys

* **Page 142**, First, copy the gen-key.sh script from the chapter06 directory to the keys directory
that you created in section 6.2.1. Here we run OpenSSL in a Docker container. If
you’re new to Docker, see appendix E, but you don’t need to be thoroughly familiar
with Docker to follow the rest of this section. To spin up the OpenSSL Docker container,
run the following docker run command from the keys directory.

```bash
\> docker run -it -v $(pwd):/export prabath/openssl
#
```
Once the container boots up successfully, you’ll find a command prompt where you
can type OpenSSL commands. Let’s run the following command to execute the
gen-key.sh file that runs a set of OpenSSL commands:

```bash
\>  sh /export/gen-key.sh
```
Once the command completes
successfully, you can type exit at the command prompt to exit from the
Docker container:

```bash
# sh /export/gen-key.sh
# exit
```
Now, if you look at the keys directory in the host filesystem, you’ll find the following
set of files:
1.  ca_key.pem and ca_cert.pem files in the keys/ca directory
2.  orderprocessing.jks file in the keys/orderprocessing directory
3.  inventory.jks file in the keys/inventory directory


## 6.3.1 Running the Order Processing microservice over TLS

* **Page 143**,Use the following curl command to test the Order Processing microservice. If everything
goes well, the command returns a JSON response, which represents an order:

```
\> curl -v http://localhost:6443/orders/11
```
You’ll need to edit the application.properties file in the ***chapter06/sample01/src/main/resources/*** directory and uncomment the following properties.
```properies
server.ssl.key-store: orderprocessing.jks
server.ssl.key-store-password: manning123
server.ssl.keyAlias: orderprocessing
```

If the service starts successfully, you’ll find a log that says that the Order Processing microservice is available on HTTPS port 6443 (if you used that port). Use the following ***curl*** command to test it over TLS:
```
\> curl -v -k https://localhost:6443/orders/11
```

## 6.3.2 Running the Inventory microservice over TLS
To enable TLS, uncomment the following properties in the ***chapter06/sample02/src/main/resources/application.properties*** file.

```properies
server.ssl.key-store: inventory.jks
server.ssl.key-store-password: manning123
server.ssl.keyAlias: inventory
```

* **Page 146**,If the service starts successfully, you see a log that says the Inventory microservice is
available on HTTPS port 8443 (shown in the previous output). Use the following
curl command to test it over TLS:

```bash
\> curl -k -v -X PUT -H "Content-Type: application/json" \
-d '[{"code":"101","qty":1},{"code":"103","qty":5}]' \
https://localhost:8443/inventory
```

* **Page 148**,Now you have both services running again. Use the following curl command to POST
an order to the Order Processing microservice, which internally talks to the Inventory
microservice over TLS to update the inventory. The following curl command is formatted
with line breaks for clarity:
```bash
\> curl -k -v https://localhost:6443/orders \
-H 'Content-Type: application/json' \
-d @- << EOF
{   "customer_id":"101021",
    "payment_method":{
        "card_type":"VISA",
        "expiration":"01/22",
        "name":"John Doe",
        "billing_address":"201, 1st Street, San Jose, CA"
    },
    "items":[ {
        "code":"101",
        "qty":1
        },
        {
        "code":"103",
        "qty":5
        }
    ],
    "shipping_address":"201, 1st Street, San Jose, CA"
}
EOF
```

And Then you will see :
```json
{
  "timestamp": 1622636320284,
  "status": 500,
  "error": "Internal Server Error",
  "exception": "org.springframework.web.client.ResourceAccessException",
  "message": "I/O error on PUT request for \"https://localhost:8443/inventory\": sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target; nested exception is javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target",
  "path": "/orders"
}
```
## 6.4 Engaging mTLS
* **Page 149**,To enforce mTLS at the Inventory
microservice end, uncomment the following property in the application.properties file in ***chapter06/sample02/src/main/resources/***:

```
server.ssl.client-auth = need
```
Setting this property to need isn’t sufficient, however .You also need to identify which
clients to trust. In this example, you’re going to trust any client with a certificate
signed by your CA. To do that, set the value of the system property ***javax.net.ssl.trustStore*** to a keystore that carries the public certificate of your trusted
CA. You already have the public certificate of the trusted CA in the inventory.jks keystore,
so all you have to do is set the system property that points to that keystore.
Uncomment the following code block (inside the setEnvironment method) in ***chapter06/sample02/src/main/java/com/manning/mss/ch06/sample02/InventoryAppConfiguration.java*** :

```java
// points to the path where inventory.jks keystore is.
System.setProperty("javax.net.ssl.trustStore", "inventory.jks");
// password of inventory.jks keystore.
System.setProperty("javax.net.ssl.trustStorePassword", "manning123");
```

Now both services are running again. Use the following curl command to POST an
order to the Order Processing microservice, which internally talks to the Inventory
microservice over TLS to update the inventory. You might expect this request to fail
because you enabled mTLS at the Inventory microservice end but didn’t change the
Order Processing microservice to authenticate to the Inventory microservice with its
private key:
```bash
\> curl -k -v https://localhost:6443/orders \
-H 'Content-Type: application/json' \
-d @- << EOF
{   "customer_id":"101021",
    "payment_method":{
        "card_type":"VISA",
        "expiration":"01/22",
        "name":"John Doe",
        "billing_address":"201, 1st Street, San Jose, CA"
    },
    "items":[ {
        "code":"101",
        "qty":1
        },
        {
        "code":"103",
        "qty":5
        }
    ],
    "shipping_address":"201, 1st Street, San Jose, CA"
}
EOF
```
This request results in an error, and if you look at the terminal that runs the Order
Processing microservice, you see the following error log:
```
javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate
```

The communication between the two microservices fails during the TLS handshake.
To fix it, first take down the Order Processing service. Then uncomment the following
code (inside the ***setEnvironment*** method) in the OrderAppConfiguration.java
file (in ***chapter06/sample01/src/main/java/com/manning/mss/ch06/sample01/***).
This code asks the system to use its private key from orderprocessing.jks to authenticate
to the Inventory microservice:

```java
// points to the path where orderprocessing.jks keystore is located.
System.setProperty("javax.net.ssl.keyStore", "orderprocessing.jks");
// password of orderprocessing.jks keystore.
System.setProperty("javax.net.ssl.keyStorePassword", "manning123");
```

Open the ****OrderProcessingApp.java*** file in the directory ***/chapter06/sample01/src/main/java/com/manning/mss/ch06/sample01/*** and uncomment 
```java
@SpringBootApplication
public class OrderProcessingApp {
	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}
(ommited)...
```


Next, run the following Maven commands from the ***chapter06/sample01*** directory to
build and start the Order Processing microservice:
```bash
\> curl -k -v https://localhost:6443/orders \
-H 'Content-Type: application/json' \
-d @- << EOF
{   "customer_id":"101021",
    "payment_method":{
        "card_type":"VISA",
        "expiration":"01/22",
        "name":"John Doe",
        "billing_address":"201, 1st Street, San Jose, CA"
    },
    "items":[ {
        "code":"101",
        "qty":1
        },
        {
        "code":"103",
        "qty":5
        }
    ],
    "shipping_address":"201, 1st Street, San Jose, CA"
}
EOF
```