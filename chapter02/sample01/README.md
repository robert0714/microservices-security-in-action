## Chapter 2: First steps in securing microservices (sample01)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample01](https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample01)
 
## Notes

* **Page 36**, To invoke the microservice, open your command-line client
and execute the following curl command:
```bash
\> curl -v http://localhost:8080/orders \
-H 'Content-Type: application/json' \
--data-binary @- << EOF
{
    "items":[
        {
            "itemCode":"IT0001",
            "quantity":3
        },
        {
            "itemCode":"IT0004",
            "quantity":1
        }
    ],
    "shippingAddress":"No 4, Castro Street, Mountain View, CA, USA"
}
EOF
```
You should see this message on your terminal:
```json
{
  "orderId": "4bf48c91-9961-4714-84bb-fe0c83d2c5b8",
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