## Chapter 2: First steps in securing microservices (sample02)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample02](https://github.com/microservices-security-in-action/samples/tree/master/chapter02/sample02)

# Notes

* Page 43 / Section: 2.2.3, the curl command is missing closing doulbe quote after "read write"
```
\> curl -u orderprocessingapp:orderprocessingappsecret \
-H "Content-Type: application/json" \
-d '{"grant_type": "client_credentials", "scope": "read write"}' \
http://localhost:8085/oauth/token |jq "."
```

You should see this message on your terminal:
```json
{
  "access_token": "ND0BufVUqWjiV5jg0n7XvBkWof4",
  "token_type": "bearer",
  "expires_in": 3599,
  "scope": "read write"
}
```