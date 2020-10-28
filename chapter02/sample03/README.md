## Chapter 2: First steps in securing microservices (sample03)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample03](https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample03)

# Notes

* Page 47 / Section: 2.3.2, the curl command :
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
* Page 48 / Section: 2.4, As before, you can use the following curl command to obtain an access token:
```
\> curl -u orderprocessingapp:orderprocessingappsecret \
-H "Content-Type: application/json" \
-d '{ "grant_type": "client_credentials", "scope": "read write" }' \
http://localhost:8085/oauth/token |jq "."

  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   169    0   108  100    61    309    174 --:--:-- --:--:-- --:--:--   482
{
  "access_token": "CZGcRwZhz/BcD86OiF7+UC+3bA4=",
  "token_type": "bearer",
  "expires_in": 3599,
  "scope": "read write"
}

```
As discussed earlier, CZGcRwZhz/BcD86OiF7+UC+3bA4= is the value of the access token you got, and it’s valid for 5 minutes (300 seconds).
```
\> curl -v http://localhost:8080/orders \
-H 'Content-Type: application/json' \
-H "Authorization: Bearer CZGcRwZhz/BcD86OiF7+UC+3bA4=" \
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
Note that the -H parameter is used to pass the access token as an HTTP header 
named Authorization . This time, you should see the Order Processing microser-vice responding with a proper message saying that the order was successful:
```
{
  "orderId": "de186c66-e843-4550-a62f-79f259b3eb6b",
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
* Page 51/ Section: 2.5.1,   you can use the following curl command to obtain an access token:
```
\> curl -u orderprocessingservice:orderprocessingservicesecret \
-H "Content-Type: application/json" \
-d '{ "grant_type": "client_credentials", "scopes": "read write" }' \
http://localhost:8085/oauth/token |jq '.'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   164    0   102  100    62    927    563 --:--:-- --:--:-- --:--:--  1490
{
  "access_token": "f0EfoCdCx3+xYuI3txNePsw02GM=",
  "token_type": "bearer",
  "expires_in": 3431,
  "scope": "read"
}

```

* Page 53/ Section: 2.5.2,   Now try to access the POST /orders resource with the token that has only a read 
scope. Execute the same curl command you used last time to access this resource, but 
with a different token this time (one that has read access only):
```
\> curl -v http://localhost:8080/orders \
-H 'Content-Type: application/json' \
-H "Authorization: Bearer f0EfoCdCx3+xYuI3txNePsw02GM=" \
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
When this command executes, you should see this error response from the resource server:
```
{
    "error":"insufficient_scope",
    "error_description":"Insufficient scope for this resource",
    "scope":"write"
}
```
Assuming that you still have a valid orderId ( de186c66-e843-4550-a62f-79f259b3eb6b ) from a successful request to the POST /orders operation, try to 
make a GET /orders/{id} request with the preceding token to see whether it’s suc-cessful. You can use the following curl command to make this request. Note that the 
orderId used in the example won’t be the same orderId you got when you tried to 
create an order yourself. Use the one that you received instead of the one used in this 
example. Also make sure to replace the value of the token in the Authorization 
header with what you got in section 2.5.1:
```
\> curl -H "Authorization: Bearer f0EfoCdCx3+xYuI3txNePsw02GM=" \
http://localhost:8080/orders/de186c66-e843-4550-a62f-79f259b3eb6b |jq '.'
```
This request should give you a successful response, as follows. The token that you 
obtained bears the read scope, which is what the GET /order/{id} resource requires, 
as declared on the resource server:
```
{
  "orderId": "de186c66-e843-4550-a62f-79f259b3eb6b",
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