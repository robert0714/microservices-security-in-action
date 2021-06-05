## Appendix F: Open Policy Agent

<img src="../cover.jpeg" style="float: left; width: 100%" />

[Amazon](https://www.amazon.com/Microservices-Security-Action-Prabath-Siriwardena/dp/1617295957/) | [Manning](https://www.manning.com/books/microservices-security-in-action) | [YouTube](https://www.youtube.com/channel/UCoEOYnrqEcANUgbcG-BuhSA) | [Slack](https://bit.ly/microservices-security) | [Notes](../notes.md) | [Supplementary Readings](../supplementary-readings.md)
# Notes

## F.4  Deploying OPA as a Docker container

* **Page 452**, Here, we mount the policies directory from the current location of the host filesystem
to the policies directory of the container filesystem under the root:
```bash
\> docker run --mount type=bind,source="$(pwd)"/policies,target=/policies \
-p 8181:8181 openpolicyagent/opa:0.29.0 run /policies --server
```
To start the OPA server, run the following command from the appendix-f/sample01
directory. This loads the OPA policies from the appendix-f/sample01/policies directory
(in section F.6, we discuss OPA policies in detail):

```bash
\> sh run_opa.sh

{
    "addrs":[
        ":8181"
    ],
    "insecure_addr":"",
    "level":"info",
    "msg":"Initializing server.",
    "time":"2019-11-05T07:19:34Z"
}
```

You can run the following command from the appendix-f/sample01 directory to test
the OPA server. The appendix-f/sample01/policy_1_input_1.json file carries the
input data for the authorization request in JSON format (in section F.6, we discuss
authorization requests in detail):

```bash
\> curl -v -X POST --data-binary @policy_1_input_1.json \
http://localhost:8181/v1/data/authz/orders/policy1

{"result":{"allow":true}}
```
The process of deploying OPA in Kubernetes is similar to deploying any other service
on Kubernetes, as we discuss in appendix J. You can check the OPA documentation
available at https://www.openpolicyagent.org/docs/latest/deployments/#kubernetes for details.


## F.5  protecting an OPA server with mTLS

* **Page 453**, Here, we mount the policies directory from the current location of the host filesystem

```bash
\> docker run -it -v $(pwd):/export prabath/openssl
#

```
Once the container boots up successfully, you’ll find a command prompt where you
can type OpenSSL commands. Let’s run the following command to execute the
gen-key.sh file that runs a set of OpenSSL commands:

```bash
\>  sh /export/gen-key.sh
```
* **Page 454**,  In case you’re already running the OPA server, stop it by pressing Ctrl-C on the
corresponding command console. To start the OPA server with TLS support, use the
following command from the appendix-f/sample01 directory:
```bash
\> sh run_opa_tls.sh

{
    "addrs":[
        ":8181"
    ],
    "insecure_addr":"",
    "level":"info",
    "msg":"Initializing server.",
    "time":"2019-11-05T19:03:11Z"
}

```
You can run the following command from the appendix-f/sample01 directory to test
the OPA server. The appendix-f/sample01/policy_1_input_1.json file carries the
input data for the authorization request in JSON format. Here we use HTTPS to talk
to the OPA server:

```bash
\> curl -v -k -X POST --data-binary @policy_1_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy1

{"result":{"allow":true}}
```

Let’s check what’s in the run_opa_tls.sh script, shown in the following listing. 

If you’re already running the OPA server, stop it by pressing Ctrl-C on the corresponding command console. To start the OPA server enabling mTLS, run the following command from the appendix-f/sample01 directory:

```bash
\> sh run_opa_mtls.sh
```
You can use the following command from the appendix-f/sample01 directory to test the OPA server, which is now secured with mTLS:
```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_1_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy1
```
## F.6   OPA Policies 

* **Page 456**, Let’s evaluate this policy with two different input documents. The first is the input
document in listing F.4, which you’ll find in the ***policy_1_input_1.json*** file. 
```json
{
    "input":{
      "path":"orders",
      "method":"POST",
      "role":"manager"
    }
}
```

Run the following curl command from the appendix-f/sample01 directory nd it returns true, because the inputs in the request match with the first allow rule in the policy (listing F.3):
```rego
package authz.orders.policy1    
  
default allow = false    

allow {    
  input.method = "POST"                    
  input.path = "orders"
  input.role = "manager"
}

allow {    
  input.method = "POST"                    
  input.path = ["orders",dept_id]
  input.deptid = dept_id
  input.role = "dept_manager"
}
```

```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_1_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy1

{"result":{"allow":true}}
```

* **Page 457**, Let’s try with another input document, as shown in listing F.5, which you’ll find in the ***policy_1_input_2.json*** file. 
```json
{
    "input":{
      "path":["orders",1000],
      "method":"POST",
      "deptid":1000,
      "role":"dept_manager"
    }
}
```
Run the following curl command from the appendix-f/sample01 directory and it returns true, because the inputs in the request match with the second allow rule in the policy (listing F.3). 
```json
{
    "input":{
      "path":"orders",
      "method":"POST",
      "role":"manager"
    }
}
```
You can see how the response from OPA server changes by changing the values of the inputs:
```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_1_input_2.json \
https://localhost:8181/v1/data/authz/orders/policy1

{"result":{"allow":true}}
```

Now let’s have a look at a slightly improved version of the policy in listing F.3. 
```rego
package authz.orders.policy1    
  
default allow = false    

allow {    
  input.method = "POST"                    
  input.path = "orders"
  input.role = "manager"
}

allow {    
  input.method = "POST"                    
  input.path = ["orders",dept_id]
  input.deptid = dept_id
  input.role = "dept_manager"
}
```

You can find this new policy in listing F.6, and it’s already deployed to the OPA server you’re running. 
```rego
package authz.orders.policy2    
  
default allow = false    

allow {    
  allowed_methods_for_manager[input.method]
  input.path = "orders"
  input.role = "manager"
}

allow {    
  allowed_methods_for_dept_manager[input.method]
  input.deptid = dept_id
  input.path = ["orders",dept_id]
  input.role = "dept_manager"
}

allow {    
  input.method = "GET"
  input.empid = emp_id
  input.path = ["orders",emp_id]
}

allowed_methods_for_manager = {"POST","PUT","DELETE"}
allowed_methods_for_dept_manager = {"POST","PUT","DELETE"}
```

Here, our expectation is that if a user has the manager role, they will be able to do HTTP PUTs, POSTs, or DELETEs on any orders resource, and if a user has the dept_manager role, they will be able to do HTTP PUTs, POSTs, or DELETEs only on the orders resource in their own department. Also any user, regardless of the role, should be able to do HTTP GETs to any orders resource under their own account.

The annotations in the following listing explain how the policy is constructed.

* **Page 458**, Let’s evaluate this policy with the input document in listing F.7, which you’ll find in the policy_2_input_1.json file.
```json
{
    "input":{
      "path":"orders",
      "method":"POST",
      "role":"manager"
    }
}
```

Run the following curl command from the appendix-f/sample01 directory and it returns true, because the inputs in the request match with the first allow rule in the policy (listing F.6):

```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_2_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy2

{
    "result":{
        "allow":true,
        "allowed_methods_for_dept_manager":["POST","PUT","DELETE"],
        "allowed_methods_for_manager":["POST","PUT","DELETE"]
    }
}
```
You can also try out the same curl command as shown here with two other input documents:

1.  policy_2_input_2.json  
2.  policy_2_input_3.json  

You can find these files inside the appendix-f/sample01 directory.

## F.7.1  Push data

* **Page 459**, The push data approach to bring in external data to the OPA server uses the data API provided by the OPA server. Let’s look at a simple example. This is the same example we used in section 5.3. 
```rego
package authz.orders.policy3    
  
import input    

import data.order_policy_data as policies    

default allow = false    

allow {    
  policy = policies[_]    
  policy.method = input.method    
  policy.path = input.path    
  policy.scopes[_] = input.scopes[_]    
}
```
The policy in listing F.8 returns true if method, path, and the set of scopes in the input message match some data read from an external data
source that’s loaded under the package named ***data.order_policy_data***. 

This policy consumes all the external data from the JSON file ***appendix-f/sample01/order_policy_data.json*** (listing F.9), which we need to push to the OPA server using the OPA data API. 
```json
[	
	{
	  "id": "r1",   
	  "path": "orders",    
	  "method": "POST",    
	  "scopes": ["create_order"]    
	},
	{   
	  "id": "r2",
	  "path": "orders",
	  "method": "GET",
	  "scopes": ["retrieve_orders"]
	},
	{   
	  "id": "r3",
	  "path": "orders/{order_id}",
	  "method": "PUT",
	  "scopes": ["update_order"]
	}
] 
```
Assuming your OPA server is running on port 8181, you can run the following curl command from the ***appendix-f/sample01*** directory to publish the data to the OPA server. Keep in mind that here we’re pushing only external data, not the policy. 

The policy that consumes the data is already on the OPA server, which you
can find in the ***appendix-f/sample01/policies/policy_3.rego*** file:

```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -H "Content-Type: application/json" \
-X PUT --data-binary @order_policy_data.json \
https://localhost:8181/v1/data/order_policy_data
```

Now you can run the following curl command from the appendix-f/sample01 directory with the input message, which you’ll find in the JSON file ***appendix-f/sample01/policy_3_input_1.json*** (in listing F.10) to check if the request is authorized:

```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_3_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy3

{"result":{"allow":true}}
```

```json
{
 "input":{
   "path":"orders",
   "method":"GET",
   "scopes":["retrieve_orders"]
 }
}
```

## F7.2 Loading data from the filesystem
Let’s have a look at the ***appendix-f/sample-01/run_opa_mtls.sh*** shell script, shown in the following listing.

```bash
docker run \
-v "$(pwd)"/policies:/policies \
-v "$(pwd)"/keys:/keys \
-p 8181:8181 openpolicyagent/opa:0.29.0 \
run /policies  \
--tls-cert-file /keys/opa/opa.cert  \
--tls-private-key-file /keys/opa/opa.key  \
--tls-ca-cert-file /keys/ca/ca.cert  \
--authentication=tls  \
--log-level=debug  \
--server
```
Let’s first check the external data file (***order_policy_data_from_file.json***), which is available in the ***appendix-f/sample01/policies*** directory.
```json
{ "order_policy_data_from_file" :[
	{
	 "id": "p1",
	 "path": "orders",
	 "method": "POST",
	 "scopes": ["create_order"]
	},
	{
	 "id": "p2",
	 "path": "orders",
	 "method": "GET",
     "scopes": ["retrieve_orders"]
	},
	{
	 "id": "p3",
	 "path": "orders/{order_id}",
	 "method": "PUT",
	 "scopes": ["update_order"]
	}
 ]
}
```
You can see in the JSON payload that we have a root element called ***order_policy_data_from_file***. The OPA server derives the package name corresponding to this data set as ***data.order_policy_data_from_file***, which is used in the policy in the following listing. This policy is exactly the same as in listing F.8 except the package name has changed.

```rego
package authz.orders.policy4    
  
import input    

import data.order_policy_data_from_file as policies    

default allow = false    

allow {    
  policy = policies[_]    
  policy.method = input.method    
  policy.path = input.path    
  policy.scopes[_] = input.scopes[_]    
}
```
Now you can run the following curl command from the ***appendix-f/sample01*** directory with the input message (***appendix-f/sample01/policy_4_input_1.json***) from listing F.10 to check whether the request is authorized:
```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_4_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy4

{"result":{"allow":true}}
```

One issue with loading data from the filesystem is that when there’s any update,you need to restart the OPA server. There is, however, a configuration option (see ***appendix-f/sample01/run_opa_mtls_watch.sh***) to ask the OPA server to load policies dynamically (without a restart), but that option isn’t recommended for production deployments. In practice, if you deploy an OPA server in a Kubernetes environment,you can keep all your policies and data in a Git repository and use an init container
along with the OPA server in the same Pod to pull all the policies and data from Git when you boot up the corresponding Pod. This process is the same as the approach we discussed in section 11.2.7 to load keystores. And when there’s an update to the policies or data, we need to restart the Pods.

## F.7.3 Overload
The ***overload*** approach to bringing in external data to the OPA server uses the input document itself. When the PEP builds the authorization request, it can embed external data into the request. Say, for example, the orders API knows, for anyone wanting to do an HTTP ***POST*** to it, they need to have the ***create_order*** scope. Rather than pre-provisioning all the scope data into the OPA server, the PEP can send it along with the authorization request. Let’s have a look at a slightly modified version of the policy in listing F.8. You can find the updated policy in the following listing.
```rego
package authz.orders.policy5    
  
import input.external as policy    

default allow = false    

allow {    
  policy.method = input.method    
  policy.path = input.path    
  policy.scopes[_] = input.scopes[_]    
}
```
You can see that we used the ***input.external*** package name to load the external data from the input document. Let’s look at the input document in the following listing, which carries the external data with it.
```json
{
    "input":{
      "path":"orders",
      "method":"GET",
      "scopes":["retrieve_orders"],
      "external" : {
            "id": "r2",
            "path": "orders",
            "method": "GET",
            "scopes": ["retrieve_orders"]
      }
    }
}
```
Now you can run the following curl command from the appendix-f/sample01 directory
with the input message from listing F.15 (***appendix-f/sample01/policy_5_input_1.json***) to check whether the request is authorized:
```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_5_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy5

{"result":{"allow":true}}
```
Reading external data from the input document doesn’t work all the time. For example, there should be a trust relationship between the OPA client (or the policy enforcement point) and the OPA server. Next we discuss an alternative for sending data in the input document that requires less trust and is applicable especially for enduser
external data.

## F.7.4 JSON Web Token
 
First, we need to have an STS that issues a JWT. You can spin up an STS by using the following command. This is the same STS we discussed in chapter 10:

```bash
\> docker run -p 8443:8443 prabath/insecure-sts-ch10:v1
```
Here, the STS starts on port 8443. Once it starts, run the following command to get a
JWT:
```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token
```
In this command, ***applicationid*** is the client ID of the web application, and ***applicationsecret*** is the client secret (which are hardcoded in the STS). If everything works fine, the STS returns an OAuth 2.0 access token, which is a JWT (or a JWS, to be precise):
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwZXRlciIsImF1ZCI6IiouZWNvbW0uY29tIiwibmJmIjoxNjIyNjE1MzIxLCJ1c2VyX25hbWUiOiJwZXRlciIsInNjb3BlIjpbImZvbyJdLCJpc3MiOiJzdHMuZWNvbW0uY29tIiwiZXhwIjoxNjIyNjIxMzIxLCJpYXQiOjE2MjI2MTUzMjEsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI1OGIzMDIyMC1jZjkyLTRkZGQtOTdhMy02NWZkZjVmNzQxNmQiLCJjbGllbnRfaWQiOiJhcHBsaWNhdGlvbmlkIn0.UAQx6lRBwy7NFOaGEaP8x4qr5_TcJLhYPq5fD9GcoHeSlvu2eRXkqQ2z-kb6nHxCjH-5GY_DQ4N--SLxo0Q2TcQAoYIVyQnUdpD-RHnkkAGklMoK6BRBManBk7mmkDl5aXKpFAOGeSOgotwg4n5uy9EgJQ32ncQ9Th6Br1ips9C6VAKJXT4Yw-dx5T30cpe-3FN3kapyUkqUPUrRfXXQM5rOixuOMMj-WUUYzQZZeVanodalJhaYjE103EnWCximbnZ64gk69wuURGkJuvBUs24N6orV5_PwmpLQ6FNZDQfzRaYP7Jb5R_pC7Hu1dsiPY1FCG86Lh813PPdBMWgvTg",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwZXRlciIsImF1ZCI6IiouZWNvbW0uY29tIiwibmJmIjoxNjIyNjE1MzIxLCJ1c2VyX25hbWUiOiJwZXRlciIsInNjb3BlIjpbImZvbyJdLCJhdGkiOiI1OGIzMDIyMC1jZjkyLTRkZGQtOTdhMy02NWZkZjVmNzQxNmQiLCJpc3MiOiJzdHMuZWNvbW0uY29tIiwiZXhwIjoxNjI1Mj* Connection #0 to host localhost left intact A3MzIxLCJpYXQiOjE2MjI2MTUzMjEsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI2NmY5MTZlZS04NzQ2LTQ1ZGUtYWZlNy0zZDk4ZWQ5MzUzYmMiLCJjbGllbnRfaWQiOiJhcHBsaWNhdGlvbmlkIn0.JFj9Uz5ANXGoVnDdbx2d37AeIt5_zK3DTLcXwmeDVIrHgP5SRpLXTouxW-o7_jwnW7pBw7g9K7uHGUKR64MpffSC-fqT-vxVnjf-Xqha7U2dL4owNMcPnRC36FYNXxYGkr-T0xGLTkLN6RGaJzod37fVQKBVQpphCCgtTJptzw0ye-2sPH_zmCMWQCLX-vRwR9HqaRdhS60CiJZ_nOqE6ILKgyp7gUpUE1b7PQvSkls0QDc4i01YVLN4YT1PXBCuZX_12Ktj12WY41JiQzVJujCNDKiuAdNqJpMq8wGdWLEoFkWL3z4yuN9lJmmakeFUJraslEZC2HTsmkd92FcC1w",
  "expires_in": 5999,
  "scope": "foo",
  "sub": "peter",
  "aud": "*.ecomm.com",
  "nbf": 1622615321,
  "iss": "sts.ecomm.com",
  "iat": 1622615321,
  "jti": "58b30220-cf92-4ddd-97a3-65fdf5f7416d"
}
```
Now you can extract the JWT from the output, which is the value of the
***access_token*** parameter. It’s a bit lengthy, so make sure that you copy the complete string. In listing F.16, you’ll find the input document. There we use the copied value of the JWT as the value of the ***token*** parameter. The listing shows only a part of the JWT, but you can find the complete input document in the ***appendix-f/sample01/policy_6_input_1.json*** file.
```json
{
    "input":{
      "path": ["orders",101],
      "method":"GET",
      "empid" : 101,
      "token" : "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwZXRlciIsImF1ZCI6IiouZWNvbW0uY29tIiwibmJmIjoxNTczMTczNTc4LCJ1c2VyX25hbWUiOiJwZXRlciIsInNjb3BlIjpbImZvbyJdLCJpc3MiOiJzdHMuZWNvbW0uY29tIiwiZXhwIjoxNTczMTc5NTc4LCJpYXQiOjE1NzMxNzM1NzgsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI5ZmM3M2YxZi0zNTMxLTQ3ZjQtOWUxMC1hZjIxMDYyNGM4YWEiLCJjbGllbnRfaWQiOiJhcHBsaWNhdGlvbmlkIn0.erJtcd6cmP2iB5XOvHs4ZpI832ji7w6UXajAwJ4R3awInEq1ju8B6pf8HXz5VKQDZC95ON7Xw79iAUji8wLdCtpsoele14u_VcD7XuJbodKyb-Y7PMruCiW4ewoe7sBFiZGZWmHFsIuWv5WGUEAVRGm9EBASqQORxdcTBxCsG05jZvJHCI5DulBP05FrOzLv5uwFJUfs6cBMpDXsqFX415-OxAmuRhnvQnFgNxAdxu6J27Gs89I2JV2BqhzTgDLSYK2TWkVf9T2YBw9rN-N5asnJMCQnN6xuSvMlG69BeOJ5QF52-fMieUM5gehwQVkrkYnoBNZVxOnv6Or9dJqNnA"
    }
}
```
The following listing shows the policy corresponding to the input document in listing F.16. The code annotations here explain all key instructions.

```rego
package authz.orders.policy6    
  
default allow = false    

certificate = `-----BEGIN CERTIFICATE-----
MIICxzCCAa+gAwIBAgIEHP9VkjANBgkqhkiG9w0BAQsFADAUMRIwEAYDVQQDEwls
b2NhbGhvc3QwHhcNMTgwNDI3MDAzNTAyWhcNMTgwNzI2MDAzNTAyWjAUMRIwEAYD
VQQDEwlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQD5
ZwGM+ysW8Y7CpUl3y+lX6A3HmidPIuNjHzria0i9TE7wPibABpimNcmCyt7Z1xeN
DTcE4sl1yNjk1z0pyV5rT2eEUgQkMbehvDGb2BDDk6nVNKEI/fRep/xvsjvfwQcM
VPqoAG6XuK0jFKvP4CpS+P0tJQoTD9x1esl67pvvWod39iISVQgDR+NXCUVy1vDt
ERuLdLLedZ2b3KTszcYgqRrvuPHDUzAgGDaSV8MmCcTvZ8+Q+LcWZolMkDj72wqB
+eIWp0w1+TItVs6L0TcOVqgbESK3p8pMj0ZHVJZfjQWGGAt1PJZ27bP1FLYE6n7d
31YUxN11pvz593gvaZgJAgMBAAGjITAfMB0GA1UdDgQWBBRvOfq/9vqyjGZay5cx
O/FFUdfH+TANBgkqhkiG9w0BAQsFAAOCAQEAVPl27J8nYnbRlL2FtUieu5afVLi2
Xg7XRn80wcbx/1zH4zjgZLyV3PRw99BKDerxdObeDWhWBnHHylrY2bi4XHRhxbGl
6n7Mi7NNGtYxb8fpi7IMKZrnLGxmXE2s+yGcX8ksmw1axQDJJ6VIKrspeUZ+5Bgd
kIj0Q0Ia1I707BI5wHz4UBylPDQ0XHamR4u7Mj30+rSZVIk/sPhiLo9gAis3E5+4
oWgYufC89m2ROc2G877DNdlcKQF5bO1dC9zMB3ZNBDleRjL/op18k5C6uay2rLEb
5Amlg9MMzHR0Yt/WNsewUmhwZi+oArfEl5XONZmtBYTs5jIgkOwsDPcZVg==
-----END CERTIFICATE-----`

allow {    
  input.method = "GET"
  input.empid = emp_id
  input.path = ["orders",emp_id]
  token.payload.authorities[_] = "ROLE_USER"
}

token = {"payload": payload} {
  io.jwt.verify_rs256(input.token, certificate)
  [header, payload, signature] := io.jwt.decode(input.token)
  payload.exp >= now_in_seconds
}

now_in_seconds = time.now_ns() / 1000000000
```
Now you can run the following curl command from the ***appendix-f/sample01*** directory with the input message from listing F.16 (***appendix-f/sample01/policy_6_input_1.json***) to check whether the request is authorized:

```bash
\> curl -k -v --key keys/client/client.key \
--cert keys/client/client.cert -X POST \
--data-binary @policy_6_input_1.json \
https://localhost:8181/v1/data/authz/orders/policy6

{"result":{"allow":true}}
```

In listing F.17, to do the JWT validation, we first needed to validate the signature and then check the expiration. OPA has a built-in function, called ***io.jwt.decode_verify(string, constraints)*** that validates all in one go. For example, you can use this function to validate the signature, expiration (exp), not before use (nbf),audience, issuer, and so on.

