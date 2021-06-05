## Chapter 10: Conquering container security with Docker (sample03)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample03](https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample03)


### 10.7.1 Enabling remote access to the Docker daemon

* **Page 254**, In this section, we’re going to set up NGINX and socat with Docker Compose. If
you’re new to Docker Compose, we recommend you read through section E.16 of
appendix E. The following listing shows the complete docker-compose.yaml file. You
can find the same in the chapter10/sample03 directory.

```yaml
version: '3'
services:
 nginx:
  image: nginx:alpine
  volumes:
  - ./nginx.conf:/etc/nginx/nginx.conf
  ports:
  - "8080:8080"
  depends_on:
  - "socat"
 socat:
  image: alpine/socat
  volumes:
  - /var/run/docker.sock:/var/run/docker.sock
  ports:
  - "2345:2345"
  command: TCP-L:2345,fork,reuseaddr,bind=socat UNIX:/var/run/docker.sock
```

This defines two services: one for NGINX and the other for socat. The NGINX service
uses the ***nginx:alpine*** Docker image, and the socat service uses the ***alpine/socat***
Docker image. For the NGINX image, we have a bind mount that mounts the
nginx.conf file from the chapter10/sample03 directory of the host filesystem to the
/etc/nginx/nginx.conf file of the container filesystem. This is the main NGINX configuration
file that forwards all the traffic it gets to socat. The following listing shows
the NGINX configuration.
```conf
events {}
http {
    server {
        listen 8080;
        location / {
            proxy_pass         http://socat:2345/;
            proxy_redirect     off;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Host $server_name;
        }
    }
}
```
For the socat image, we have a bind mount that mounts the ***/var/run/docker.sock*** file
from the host filesystem to the ***/var/run/docker.sock*** file of the container filesystem.
This is the file that represents the UNIX socket the Docker daemon listens to on the
host machine. When we do this bind mounting, the container that runs socat can write
directly to the UNIX socket on the host filesystem so that the Docker daemon gets the
messages. Let’s have a look at the following line, which is the last line in listing 10.12:

```bash
command: TCP-L:2345,fork,reuseaddr,bind=socat UNIX:/var/run/docker.sock
```

The ***TCP-L:2345*** flag instructs socat to listen on port 2345 for TCP traffic. The fork
flag enables socat to handle each arriving packet by its own subprocess. When we
use ***fork***, socat creates a new process for each newly accepted connection. The
***bind=127.0.0.1*** flag instructs socat to listen only on the loopback interface, so no
one outside the host machine can directly talk to socat. ***UNIX:/var/run/docker.sock*** is the address of the UNIX socket where the Docker daemon accepts connections.
In effect, the command asks socat to listen for TCP traffic on port 2345, log it,
and then forward it to the UNIX socket ***/var/run/docker.sock***. Let’s run the following
command from the chapter10/sample03 directory to start both the NGINX
and socat containers:

```bash
\> docker-compose up

Pulling socat (alpine/socat:)...
latest: Pulling from alpine/socat
ff3a5c916c92: Pull complete
abb964a97c4c: Pull complete
Pulling nginx (nginx:alpine)...
alpine: Pulling from library/nginx
e7c96db7181b: Already exists
f0e40e45c95e: Pull complete
Creating sample03_socat_1 ... done
Creating sample03_nginx_1 ... done
Attaching to sample03_socat_1, sample03_nginx_1
```

To make sure everything works fine, you can run the following command from the
Docker client machine with the proper NGINX hostname. It should return a JSON
payload that carries Docker image details:

```bash
\> curl http://nginx-host:8080/v1.39/images/json
```

### 10.7.2 Enabling mTLS at the NGINX server to secure access to Docker APIs

In this section, we’ll see how to secure the APIs exposed by the NGINX server with
mTLS, so that all the Docker APIs will be secured too. To do that, we need to create a
public/private key pair for the NGINX server as well as for the Docker client. The
Docker client uses its key pair to authenticate to the NGINX server.

#### GENERATING KEYS AND CERTIFICATES FOR THE NGINX SERVER AND THE DOCKER CLIENT

Here we introduce a single script to perform all the actions to create keys for the CA,
NGINX server, and Docker client. The CA signs both the NGINX certificate and
Docker client’s certificate. We run OpenSSL in a Docker container to generate keys.
OpenSSL is a commercial-grade toolkit and cryptographic library for TLS, available
for multiple platforms. Refer to appendix G to find more details on OpenSSL and key
generation. To spin up the OpenSSL Docker container, run the following command
from the chapter10/sample03/keys directory:

```bash
\> docker run -it -v $(pwd):/export prabath/openssl
#
```

This ***docker run*** command starts OpenSSL in a Docker container with a bind mount,
which maps the ***chapter10/sample03/keys*** directory (or the current directory, which
is indicated by ***$(pwd))*** from the host filesystem to the ***/export*** directory of the container
filesystem. This bind mount lets you share part of the host filesystem with the
container filesystem. When the OpenSSL container generates certificates, those are
written to the ***/export*** directory of the container filesystem. Because we have a bind
mount, everything inside the ***/export*** directory of the container filesystem is also
accessible from the ***chapter10/sample03/keys*** directory of the host filesystem.

   When you run the ***docker run*** command for the first time, it can take a couple of
minutes to execute and should end with a command prompt where you can execute
this script to create all the keys:

```bash
# sh /export/gen-key.sh
# exit
```

Now, if you look at the ***chapter10/sample03/keys*** directory in the host filesystem,
you’ll find the following set of files. If you want to understand what happens in the
script, check appendix G:

*  ***ca_key.pem and ca_cert.pem in the chapter10/sample03/keys/ca directory***—ca_key.pem
is the private key of the CA, and ca_cert.pem is the public key.
*  ***nginx_key.pem and nginx_cert.pem in the chapter10/sample03/keys/nginx directory***—
nginx_key.pem is the private key of the NGINX server, and nginx_cert.pem is
the public key, which is signed by the CA.
*  ***docker_key.pem and docker_cert.pem in the chapter10/sample03/keys/docker directory***—
docker_key.pem is the private key of the Docker client, and docker_cert.pem is
the public key, which is signed by the CA. The Docker client uses these keys to
authenticate to the NGINX server

#### PROTECTING THE NGINX SERVER WITH MTLS

In this section, we set up NGINX to work with mTLS with the keys we generated in the
previous section. If you’re running the NGINX container from section 10.7.1, stop it
first by pressing Ctrl-C on the terminal that runs the container.

Listing 10.14 shows the content from the nginx-secured.conf file in the ***chapter10/sample03*** directory. This is the same file in listing 10.13 with some new parameters
related to the TLS configuration. The parameter ***ssl_certificate*** instructs NGINX to look for the server certificate at the ***/etc/nginx/nginx_cert.pem*** location in the container filesystem.
```conf
events {}
http {
    server {
        listen                   8443 ssl;
        server_name              nginx.ecomm.com;
        ssl_certificate          /etc/nginx/nginx_cert.pem;
        ssl_certificate_key      /etc/nginx/nginx_key.pem;
        ssl_protocols            TLSv1.2;
        ssl_verify_client        on;
        ssl_client_certificate  /etc/nginx/ca_cert.pem;
        location / {
            proxy_pass         http://socat:2345/;
            proxy_redirect     off;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Host $server_name;
        }
    }
}
```

Because we keep all the key files in the host filesystem in the updated docker-compose
configuration file (listing 10.15), we have a new set of bind mounts. From the host filesystem,
we map sample03/keys/nginx/nginx_cert.pem to the /etc/nginx/nginx_cert
.pem file in the container filesystem. In the same way, we have a bind mount for the
private key (***ssl_certificate_key***) of the NGINX server. To enable mTLS, we set
the value of ***ssl_verify_client*** to on as in listing 10.14, and the ***ssl_client_certificate*** parameter points to a file that carries the public keys of all trusted
CAs. In other words, we allow any client to access the Docker API if the client brings a
certificate issued by a trusted CA.

Now, we need to update the docker-compose configuration to use the new ***nginx-secured.conf*** file. The following listing shows the updated docker-compose configuration,
which is also available in the ***chapter10/sample03/docker-compose-secured.yaml*** file.
```yaml
version: '3'
services:
 nginx:
  image: nginx:alpine
  volumes:
  - ./nginx-secured.conf:/etc/nginx/nginx.conf
  - ./keys/nginx/nginx_cert.pem:/etc/nginx/nginx_cert.pem
  - ./keys/nginx/nginx_key.pem:/etc/nginx/nginx_key.pem
  - ./keys/ca/ca_cert.pem:/etc/nginx/ca_cert.pem
  ports:
  - "8443:8443"
  depends_on:
  - "socat"
 socat:
  image: alpine/socat
  volumes:
  - /var/run/docker.sock:/var/run/docker.sock
  ports:
  - "2345:2345"
  command: TCP-L:2345,fork,reuseaddr,bind=socat UNIX:/var/run/docker.sock
```

* **Page 259**, Let’s run the following command to start both the secured NGINX and socat containers
from the ***chapter10/sample03*** directory. This command points to the docker-compose-secured.yaml file, which carries the new Docker Compose configuration:
```bash
\> docker-compose -f docker-compose-secured.yaml up
```

To make sure everything works fine, you can run the following command from the
***chapter10/sample03*** directory of the Docker client machine with the proper NGINX
hostname. Here we use the ***–k*** option to instruct curl to ignore any HTTPS server certificate
validation. Still, this command will fail because we’ve now secured all Docker
APIs with mTLS:
```bash
\> curl –k https://nginx-host:8443/v1.39/images/json
```

The following command shows how to use curl with the proper client-side certificates.
Here, we use the key pair that we generated for the Docker client. This should return
a JSON payload that carries the Docker image details:
```bash
\> curl --cacert keys/ca/ca_cert.pem --cert keys/nginx/nginx_cert.pem \
--key keys/nginx/nginx_key.pem \
--resolve 'nginx.ecomm.com:8443:10.0.0.128' \
https://nginx.ecomm.com:8443/v1.39/images/json
```

The ***--cacert*** argument in the command points to the public key of the CA, and the
***--cert*** and ***--key*** parameters point to the public key and the private key, respectively,
that we generated in the previous section for the Docker client. In the API endpoint,
the hostname we use must match the CN of the certificate we use for NGINX;
otherwise, certificate validation fails. Then again, because we don’t have a DNS entry
for this hostname, we instruct curl to resolve it to the IP address 10.0.0.128 by using
the ***--resolve*** argument; you probably can use 127.0.0.1 as the IP address if you run
curl from the same machine where the Docker daemon runs.

### CONFIGURING THE DOCKER CLIENT TO TALK TO THE SECURED DOCKER DAEMON

In this section, we configure the Docker client to talk to the Docker daemon via the
secured NGINX server. The following command instructs the Docker client to use
***nginx.ecomm.com*** as the Docker host and ***8443*** as the port:

```bash
\> export DOCKER_HOST=nginx.ecomm.com:8443
```

Because we haven’t set up ***nginx.ecomm.com*** in a DNS server, we need to update the
/etc/hosts file of the machine, which runs the Docker client, with a hostname-to-IPaddress
mapping. If you run both the Docker daemon and the client on the same
machine, you can use 127.0.0.1 as the IP address of the Docker daemon:

```
10.0.0.128 nginx.ecomm.com
```

Now run the following Docker client command from the same terminal where you
exported the ***DOCKER_HOST*** environment variable. The ***tlsverify*** argument
instructs the Docker client to use TLS to connect to the Docker daemon and verify the
remote certificate. The ***tlskey*** and ***tlscert*** arguments point to the private key and
the public key of the Docker client, respectively. These are the keys that we generated
in the previous section. The ***tlscacert*** argument points to the public key of the CA:
```bash
\> docker --tlsverify --tlskey keys/docker/docker_key.pem \
--tlscert keys/docker/docker_cert.pem \
--tlscacert keys/ca/ca_cert.pem images
```

If you want to make the command look simple, we can replace the default keys that
come with the Docker client, with the ones we generated. Replace the following:

*   ~/.docker/key.pem with keys/docker/docker_key.pem
*   ~/.docker/cert.pem with keys/docker/docker_cert.pem
*   ~/.docker/ca.pem with keys/ca/ca_cert.pem

Now you can run your Docker client command as shown here:

```bash
\> docker --tlsverify images
```
