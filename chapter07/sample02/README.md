## Chapter 7: Securing east/west traffic with JWT (sample02)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample02](https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample02)

# Notes

## 7.3 Securing microservices with JWT

* **Page 171**, Now let’s invoke the Order Processing microservice with the following curl command
with no security token. As expected, you should see an error message:

```bash
\> curl -k https://localhost:9443/orders/11 |jq "."
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}

```
To invoke the Order Processing microservice with proper security, you need to get a
JWT from the STS using the following curl command. This example assumes that the
security token service discussed in the preceding section still runs on HTTPS port
8443. For clarity, we removed the long JWT in the response and replaced it with the
value jwt_access_token:
```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token |jq "."

{
"access_token":"jwt_access_token",
"token_type":"bearer",
"refresh_token":"",
"expires_in":1533280024,
"scope":"foo"
}
```
Now let’s invoke the Order Processing microservice with the JWT we got from the
curl command. Set the same JWT in the HTTP Authorization Bearer header using
the following curl command and invoke the Order Processing microservice. Because
the JWT is a little lengthy, you can use a small trick when using the curl command.
First, export the JWT to an environmental variable (TOKEN), and then use that environmental
variable in your request to the Order Processing microservice:

```bash
\> export TOKEN=jwt_access_token
\> curl -k -H "Authorization: Bearer $TOKEN" \
https://localhost:9443/orders/11  |jq "."

{
  "customer_id": "101021",
  "order_id": "11",
  "payment_method": {
    "card_type": "VISA",
    "expiration": "01/22",
    "name": "John Doe",
    "billing_address": "201, 1st Street, San Jose, CA"
  },
  "items": [
    {
      "code": "101",
      "qty": 1
    },
    {
      "code": "103",
      "qty": 5
    }
  ],
  "shipping_address": "201, 1st Street, San Jose, CA"
}

```

## 7.4 Using JWT as a data source for access control

* **Page 172**, Open the ****OrderProcessingService.java*** file in the directory ***sample02/src/main/java/com/manning/mss/ch07/sample02/service/*** and uncomment the method-level annotation ***@PreAuthorize("#oauth2.hasScope('bar')")*** from the getOrder method so that the code looks like the following:

```java
@PreAuthorize("#oauth2.hasScope('bar')")
@RequestMapping(value = "/{id}", method = RequestMethod.GET)
public ResponseEntity<?> getOrder(@PathVariable("id") String orderId) {
}
```

One important thing to notice is that when the client application talks to the STS, it’s asking for an access token for the scope foo.

```bash
\> export TOKEN=jwt_access_token
\> curl -k -H "Authorization: Bearer $TOKEN" \
https://localhost:9443/orders/11  |jq "."

{
  "error": "access_denied",
  "error_description": "Access is denied"
}
```
It failed as expected. Try the same thing with a valid scope. First, request a JWT with
the bar scope from the STS:

```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=bar" \
https://localhost:8443/oauth/token |jq .""
{
"access_token":"jwt_access_token",
"token_type":"bearer",
"refresh_token":"",
"expires_in":1533280024,
"scope":"bar"
}
```
Now invoke the Order Processing microservice with the right token, and we should
get a positive response:

```bash
\> export TOKEN=jwt_access_token
\> curl -k -H "Authorization: Bearer $TOKEN" \
https://localhost:9443/orders/11  |jq "."

{
  "customer_id": "101021",
  "order_id": "11",
  "payment_method": {
    "card_type": "VISA",
    "expiration": "01/22",
    "name": "John Doe",
    "billing_address": "201, 1st Street, San Jose, CA"
  },
  "items": [
    {
      "code": "101",
      "qty": 1
    },
    {
      "code": "103",
      "qty": 5
    }
  ],
  "shipping_address": "201, 1st Street, San Jose, CA"
}

```