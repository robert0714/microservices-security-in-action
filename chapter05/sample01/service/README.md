## Prerequisites to run the sample

* Download and install the Java Development Kit (JDK) for your operating system. JDK is required to compile and build the source code in the samples. You can [download](http://www.oracle.com/technetwork/java/javase/downloads/index.html) the latest JDK from the Oracle website.  We use Java version 11 to test all the samples.
* Download and install Apache [Maven](https://maven.apache.org/install.html). Maven is a project management and comprehension tool that makes it easy to declare third-party (external) dependencies of your Java project required in the compile/build phase. It has various plugins such as the compiler, which compiles your Java source code and produces the runnable artifact (binary). You can download Maven from the Apache website.  Follow the installation instructions  to install Maven on your operating system. We use Maven version 3.5 to test all the samples.
* Download and install the [curl](https://curl.haxx.se/download.html) command line tool from the curl website.  You use curl in the book as a client application to access microservices. Most of the operating systems do have curl installed out of the box.
* Download and install the Git command-line client on your computer. You only use Git client once to clone our samples Git repository. 

## Notes

* Page 115/Section 5.1.3 . First let’s get an access token to access the Order Processing microservice. We 
expose the Order Processing microservice via the Zuul API gateway, and it enforces 
OAuth 2.0-based security. So, we need a valid OAuth 2.0 access token to make requests 
to the API gateway. You can get an access token from the authorization server (via the 
gateway) by executing the following command from your command-line client: 
```
\> curl -u application1:application1secret \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials" \
'http://localhost:9090/token/oauth/token'  |jq '.'
```
If the request is successful, you should get a response similar to this:
```
{
  "access_token": "kB25t70Lxxu2JziStJJN2E9bopY=",
  "token_type": "bearer",
  "expires_in": 3593,
  "scope": "read write"
}
```
Once you have the token, we can access the Order Processing microservice via the 
Zuul API gateway. Let’s run the following curl command to do an HTTP POST to the 
Order Processing microservice. Make sure to use the value of the same access token 
you received from the previous request. If not, you will receive an authentication fail-
ure error:
```
\> curl -v http://localhost:9090/retail/orders \
-H "Authorization: Bearer kB25t70Lxxu2JziStJJN2E9bopY=" \
-H "Content-Type: application/json" \
--data-binary @- << EOF
{   "customer_id":"101021",
    "payment_method":{
        "card_type":"VISA",
        "expiration":"01/22",
        "name":"John Doe",
        "billing_address":"201, 1st Street, San Jose, CA"
    },
    "items":[
        {
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
If the request is successful, you get a response as follows with the status code 200. This 
indicates that the Order Processing microservice has successfully created the order. 
The orderId in the response is the unique identifier of the newly created order.
Take note of the orderId since we need it in the next step:
```
{
  "orderId": "804d296a-d844-494f-b747-8d3e9c4186df",
  "items": [
    {
      "itemCode": null,
      "quantity": 0
    },
    {
      "itemCode": null,
      "quantity": 0
    }
  ]
```
Next let’s use the orderId to query our order information. Make sure to use the 
same access token and the same orderId as before. In our request, the orderid is 
***804d296a-d844-494f-b747-8d3e9c4186df*** :
```
\> curl -v \
-H "Authorization: Bearer XRqH42qH/jdY/79a2kFMEYqQzSQ=" \
http://localhost:9090/retail/orders/804d296a-d844-494f-b747-8d3e9c4186df  |jq '.'
```
You should see a response with the status code 200 including the details of the order 
we placed before. Execute the same request three more times. You should observe 
the same response, with status code 200 being returned. If you execute the same 
request for the fourth time within a minute, you should see a response with the status 
code 429 saying the request is throttled out. The duration of the time window 
(1 minute) is configured in a Java class that we will take a look at shortly. The 
response looks like this:
```
< HTTP/1.1 429 
< Transfer-Encoding: chunked
< Date: Fri, 30 Oct 2020 01:31:20 GMT
< 
{
  "error": true,
  "reason": "Request Throttled."
}

```