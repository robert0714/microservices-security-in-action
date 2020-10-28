## Chapter 3: Securing north/south traffic with an API gateway (sample02)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample02](https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample02)

* Page 69/ Section: 3.3.2.  Execute the following command from
your terminal application (make sure to have the correct order ID from section 3.3.1):
```
\> curl \
http://localhost:9090/retail/orders/e0046f59-e57e-4df6-89a0-93fd85fbad8a  |jq "."

```
If the request is successful, you should see a response like this:
```
{
  "orderId": "e0046f59-e57e-4df6-89a0-93fd85fbad8a",
  "items": [
    {
      "itemCode": "IT0001",
      "quantity": 3
    },
    {
      "itemCode": "IT0004",
      "quantity": 1
    }
  ],
  "shippingAddress": "No 4, Castro Street, Mountain View, CA, USA"
}
```