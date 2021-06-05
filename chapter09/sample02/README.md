## Chapter 9: Securing reactive microservices (sample02)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample02](https://github.com/microservices-security-in-action/samples/tree/master/chapter09/sample02)

### 9.4 Developing a microservice to read events from a Kafka topic


* **Page 209**, Execute the following
command to make an order request (note the Order Processing microservice
from section 9.3 handles this request):
```bash
\> curl http://localhost:9000/order -X POST -d @order.json \
-H "Content-Type: application/json" -v  |jq "."
```
 