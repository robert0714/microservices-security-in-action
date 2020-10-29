## Chapter 3: Securing north/south traffic with an API gateway (sample04)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample04](https://github.com/microservices-security-in-action/samples/tree/master/chapter03/sample04)


* Page 72/ Section: 3.3.3.     
Once the gateway has started successfully on port 9090, execute the following com-mand on a new terminal window to get an access token from the authorization server,
through the Zuul gateway. Here we use the OAuth 2.0 client_credentials grant type, with application1 as the client ID and application1secret as the client secret:
```
\> curl -u application1:application1secret \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials" \
http://localhost:9090/token/oauth/token  |jq "."

```
You should receive the access token in a response that looks like this:
```
{
  "access_token": "Fm7f7RLZKj8eRssZZNbyT2GUdJ0=",
  "token_type": "bearer",
  "expires_in": 3588,
  "scope": "read write"
}
```

* Page 73/ Section: 3.3.3.   Now, let’s try to access the Order Processing microservice through the Zuul gate-way as before with the following command (with no valid access token):
```
\> curl -v \
http://localhost:9090/retail/orders/e0046f59-e57e-4df6-89a0-93fd85fbad8a |jq '.'
```
This command should now give you an authentication error message that looks like 
the following. This error message confirms that the Zuul gateway, with OAuth 2.0 
security screening enforced, does not allow any request to pass through it without a 
valid token:
```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 9090 (#0)
> GET /retail/orders/e0046f59-e57e-4df6-89a0-93fd85fbad8a HTTP/1.1
> Host: localhost:9090
> User-Agent: curl/7.61.1
> Accept: */*
> 
< HTTP/1.1 401 
< Transfer-Encoding: chunked
< Date: Thu, 29 Oct 2020 06:59:10 GMT
< 
{ [55 bytes data]
100    49    0    49    0     0  16333      0 --:--:-- --:--:-- --:--:-- 16333
* Connection #0 to host localhost left intact
{
  "error": true,
  "reason": "Authentication Failed"
}
```

You can use the following command for this purpose. Make sure to 
have the correct order ID from section 3.3.1 and replace e0046f59-e57e-4df6-89a0-93fd85fbad8a in the following command with it:

```
\> curl \
http://localhost:9090/retail/orders/e0046f59-e57e-4df6-89a0-93fd85fbad8a \
-H "Authorization: Bearer Fm7f7RLZKj8eRssZZNbyT2GUdJ0="  |jq '.'
```
You should see a successful response as shown here:
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