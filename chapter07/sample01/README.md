## Chapter 7: Securing east/west traffic with JWT (sample01)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample01](https://github.com/microservices-security-in-action/samples/tree/master/chapter07/sample01)

# Notes

## 7.2 Setting up an STS to issue a JWT 

* **Page 168**, Run the following curl command, which talks to the STS and gets a JWT. You should
be familiar with the request, which is a standard OAuth 2.0 request following the password
grant type. We use password grant type here only as an example, and for simplicity.
In a production deployment, you may pick authorization code grant type or any other
grant type that fits better for your use cases. (In appendix A, we discuss OAuth 2.0 grant
types in detail.)

```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token | jq "."
```

In this command, ***applicationid*** is the client ID of the web application, and
***applicationsecret*** is the client secret. If everything works, the STS returns an
OAuth 2.0 access token, which is a JWT (or a JWS, to be precise):

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImQ3ZDg3NTY3LTE4NDAtNGY0NS05NjE0LTQ5MDcxZmNhNGQyMSJ9.eyJzdWIiOiJwZXRlciIsImF1ZCI6IiouZWNvbW0uY29tIiwidXNlcl9uYW1lIjoicGV0ZXIiLCJzY29wZSI6WyJmb28iXSwiaXNzIjoic3RzLmVjb21tLmNvbSIsImV4cCI6MTYyMjc5NDY3MSwiaWF0IjoxNjIyNzg4NjcxLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiOTNhYWY5NWUtODNkNy00OGFjLWFkMzctNGEwNGIzYzJhYzIyIiwiY2xpZW50X2lkIjoiYXBwbGljYXRpb25pZCJ9.EIEe9sw5kphAKD03Oe_5WRVUEGDClPDkncDLvdspEuB0_ELWC0hXFN7a6ubp9uvCjo98r6V-hJUxEwLTt1l0PIDzPzoqdQmEtVIc1zoLXiBuIkVS5cvLojWJrV7j18vd_lPn_fv4jsHS5Roy2oo-ML4D1MpFvLgnFvmfmp66hBuauzbxUb-WlWpaS9FEb7tjDRhDUBTntECT37qjhPlAliRam2YSICOlXaD00tKti3b8PfOd7zI_38ZTygdXgpwvwhwG9AujOi7MWJOMC9-e7AnMkOo0IOof9JeJxEJ7ki3zEn4wmPo_rO4eRa_AwwVTB2cYJW2JbUirbLA3kUUq1A",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImQ3ZDg3NTY3LTE4NDAtNGY0NS05NjE0LTQ5MDcxZmNhNGQyMSJ9.eyJzdWIiOiJwZXRlciIsImF1ZCI6IiouZWNvbW0uY29tIiwidXNlcl9uYW1lIjoicGV0ZXIiLCJzY29wZSI6WyJmb28iXSwiYXRpIjoiOTNhYWY5NWUtODNkNy00OGFjLWFkMzctNGEwNGIzYzJhYzIyIiwiaXNzIjoic3RzLmVjb21tLmNvbSIsImV4cCI6MTYyNTM4MDY3MSwiaWF0IjoxNjIyNzg4NjcxLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiMTZhMDNlODUtNDQzMS00ODkwLTlmZDQtNjFlOGJjYmFlNTA5IiwiY2xpZW50X2lkIjoiYXBwbGljYXRpb25pZCJ9.LkyKTgpAeGkA7RnOLqvInf97bTaO22e3wHB_TkhlIMVtZqHRanWttm2r3Blrq9OCBNfKPB5RNQxHcfhd_DClvGfCgwwXr3EFiL0yHlk_5jgJdjP3uuM1WEGroY1fsmgzcP5hUYwYpaWMc9vjynFxLr5WVRzesjOZgXPBvA9xbm0LllI-FHMMnI7l1dbso3CaApCYDzDJMyTPW7dMLvGmnWGUg4DCClukJIzu2fGa1zP7-X-WO98SwB3_2tmyRI9wpD5ERSh23Mro7G4AGjd14kN28LPiD0Uxq8pdecT3RxjMZ3nGpedBQDRr0fTLx9ge7KA281ISTl_10y7raZbrpw",
  "expires_in": 5999,
  "scope": "foo",
  "sub": "peter",
  "aud": "*.ecomm.com",
  "iss": "sts.ecomm.com",
  "iat": 1622788671,
  "jti": "93aaf95e-83d7-48ac-ad37-4a04b3c2ac22"
}
```