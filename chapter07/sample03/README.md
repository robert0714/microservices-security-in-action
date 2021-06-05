## Chapter 7: Securing east/west traffic with JWT (sample03)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample03](https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample03)

# Notes

## 7.5 Securing service-to-service communications with JWT
* **Page 173**, Now we want to get a JWT from the STS by using the following curl command, which
is the same one you used in the preceding section. For clarity, we removed the long
JWT in the response and replaced it with the value jwt_access_token:
```
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=bar" \
https://localhost:8443/oauth/token |jq "."

{
"access_token":"jwt_access_token",
"token_type":"bearer",
"refresh_token":"",
"expires_in":1533280024,
"scope":"foo"
}
```

Now let’s post an order to the Order Processing microservice with the JWT you got
from the preceding curl command. First, export the JWT to an environmental variable
(TOKEN) and then use that environmental variable in your request to the Order
Processing microservice. If everything goes well, the Order Processing microservice
validates the JWT, accepts it, and then talks to the Inventory microservice to update
the inventory. You’ll find the item numbers printed on the terminal that runs the
Inventory microservice:
```bash
\> export TOKEN=jwt_access_token
\> curl -k -H "Authorization: Bearer $TOKEN" \
-H 'Content-Type: application/json' \
-v https://localhost:9443/orders \
-d @- << EOF
{   "customer_id":"101021",
    "payment_method":{
        "card_type":"VISA",
        "expiration":"01/22",
        "name":"John Doe",
        "billing_address":"201, 1st Street, San Jose, CA"
    },
    "items":[{"code":"101","qty":1},{"code":"103","qty":5}],
    "shipping_address":"201, 1st Street, San Jose, CA"
}
EOF 
```