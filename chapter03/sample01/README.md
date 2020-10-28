## Chapter 3: Securing north/south traffic with an API gateway (sample01)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample01](https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample01)

* Page 68/ Section: 3.3.1.     
If the service started successfully, you should see a log statement on the terminal that 
says Started OrderApplication in <X> seconds . If you see this message, your 
Order Processing microservice is up and running. Now send a request to it, using curl, 
to make sure that it responds properly:
```
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
Upon successful execution of this request, you should see a response message:
```
{
    "orderId":"b5dd25a5-b39c-42c8-87d5-5103850c3cbc",
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
```