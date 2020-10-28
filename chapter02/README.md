## Chapter 2: First steps in securing microservices

<img src="../cover.jpeg" style="float: left; width: 100%" />

[Amazon](https://www.amazon.com/Microservices-Security-Action-Prabath-Siriwardena/dp/1617295957/) | [Manning](https://www.manning.com/books/microservices-security-in-action) | [YouTube](https://www.youtube.com/channel/UCoEOYnrqEcANUgbcG-BuhSA) | [Slack](https://bit.ly/microservices-security) | [Notes](../notes.md) | [Supplementary Readings](../supplementary-readings.md)

# Notes

* Page 43 / Section: 2.2.3, the curl command is missing closing doulbe quote after "read write"
```
\> curl -u orderprocessingapp:orderprocessingappsecret \
-H "Content-Type: application/json" \
-d '{"grant_type": "client_credentials", "scope": "read write"}' \
http://localhost:8085/oauth/token |jq "."
```