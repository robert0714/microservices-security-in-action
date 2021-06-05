## Chapter 10: Conquering container security with Docker (sample02)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample02](https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample02)

### 10.4 Running the Order Processing microservice on Docker

* **Page 244**, First, let’s build the project from the ***chapter10/sample02*** directory with the following Maven command. If everything goes well, you should see the BUILD SUCCESS message at the end:

```bash
\> mvn clean package
[INFO] BUILD SUCCESS
```

Now, let’s run the following command to build a Docker image for the Order Processing microservice from the ***chapter10/sample02*** directory. This command uses the Dockerfile manifest, which is inside the ***chapter10/sample02*** directory:

```bash
\> docker build -t com.manning.mss.ch10.sample02:v1 .
```

Before we proceed further, let’s revisit our use case. As illustrated in figure 10.3, we try to invoke the Order Processing microservice from a token issued by the STS. The client application has to get a token from the STS and then pass it to the Order Processing microservice. Next, the Order Processing microservice talks to the STS to get its public key, which corresponds to the private key used by the STS to sign the token it issued.
This is the only communication that happens between the Order Processing microservice and the STS. If you check the application.properties file in the ***chapter10/sample02/config*** directory, you’ll find a property called ***security.oauth2.resource.jwt.keyUri***, which points to the STS.

To enable direct communication between the containers running the Order Processing microservice and the STS, we need to create a user-defined network. When two Docker containers are in the same user-defined network, they can talk to each other by using the container name. The following command creates a user-defined network called ***manning-network***. (If you’re new to Docker, appendix E provides more Docker networking options.)
```bash
\> docker network create manning-network
06d1307dc12d01f890d74cb76b5e5a16ba75c2e8490c718a67f7d6a02c802e91
```

Now let’s spin up the STS from the ***chapter10/sample01*** directory with the commands in the following listing, which attach it to the manning-network we just created.

```bash
export JKS_SOURCE="$(pwd)/keystores/keystore.jks"
export JKS_TARGET="/opt/keystore.jks"
export JWT_SOURCE="$(pwd)/keystores/jwt.jks"
export JWT_TARGET="/opt/jwt.jks"
export APP_SOURCE="$(pwd)/config/application.properties"
export APP_TARGET="/opt/application.properties"

docker run -p 8443:8443 \
--name sts --net manning-network \
--mount type=bind,source="$JKS_SOURCE",target="$JKS_TARGET" \
--mount type=bind,source="$JWT_SOURCE",target="$JWT_TARGET" \
--mount type=bind,source="$APP_SOURCE",target="$APP_TARGET" \
-e KEYSTORE_SECRET=springboot \
-e JWT_KEYSTORE_SECRET=springboot \
com.manning.mss.ch10.sample01:v2
```

Here we use the ***–-net*** argument to specify the name of the network, and the ***–-name*** argument to specify the name of the container. This container is now accessible using the container name by any container in the same network. Also, the command uses the STS image we published to Docker Hub in section 10.2. Make sure that your  ***–-mount*** arguments in the previous command point to the correct file locations. If you run the command from chapter10/sample01, it should work just fine.

Next, let’s spin up the Order Processing microservice from the image we created at the beginning of this section. Execute the commands in the following listing from within the ***chapter10/sample02*** directory. 

```bash
export JKS_SOURCE="$(pwd)/keystores/keystore.jks"
export JKS_TARGET="/opt/keystore.jks"
export TRUST_SOURCE="$(pwd)/keystores/trust-store.jks"
export TRUST_TARGET="/opt/trust-store.jks"
export APP_SOURCE="$(pwd)/config/application.properties"
export APP_TARGET="/opt/application.properties"

docker run -p 9443:9443 \
--net manning-network \
--mount type=bind,source="$JKS_SOURCE",target="$JKS_TARGET" \
--mount type=bind,source="$TRUST_SOURCE",target="$TRUST_TARGET" \
--mount type=bind,source="$APP_SOURCE",target="$APP_TARGET" \
-e KEYSTORE_SECRET=springboot \
-e TRUSTSTORE_SECRET=springboot \
com.manning.mss.ch10.sample02:v1
```
ps. You can notice the case is like chapter7/sample02 .

We pass the keystore.jks, trust-store.jks, and application.properties files as ***–-mount*** arguments. If you look at the application.properties file in the ***chapter10/sample02/config*** directory, you’ll find a property called ***security.oauth2.resource.jwt.keyUri***, which points to the endpoint ***https://sts:8443/oauth/token_key*** with the hostname of the STS container (sts).

To invoke the Order Processing microservice with proper security, you need to get a JWT from the STS using the following curl command. For clarity, we removed the long JWT in the response and replaced it with the value ***jwt_access_token***:

```bash
curl -v -X POST --basic -u applicationid:applicationsecret \
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

Now let’s invoke the Order Processing microservice with the JWT you got from this
curl command. Using the following ***curl*** command, set the same JWT we got from
the STS, in the HTTP Authorization Bearer header and invoke the Order Processing
microservice. Because the JWT is a little lengthy, you can use a small trick when using the curl command. First, export the JWT to an environment variable (TOKEN). Then use that environment variable in your request to the Order Processing microservice:

```bash
\> export TOKEN=jwt_access_token
\> curl -k -H "Authorization: Bearer $TOKEN" \
https://localhost:9443/orders/11
```

see the below
```json
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

## 10.5 Running containers with limited privileges
In any operating system, there’s a super user or an administrator who can basically do anything. This user is called ***root*** in most of the Linux distributions. Traditionally, in the Linux kernel, there are two types of processes: privileged processes and unprivileged processes. Any ***privileged process*** runs with the special user ID 0, which belongs to the root user. Any process carrying a nonzero user ID is an ***unprivileged process***. When performing a task, a privileged process bypasses all the kernel-level permission checks, while all the unprivileged processes are subjected to a permission check. This approach gives too much power to the root user; in any case, too much power is dangerous.

All Docker containers, by default, run as the root user. Does that mean anyone having
access to the container can do anything to the host filesystem from a container? In
appendix E (section E.13.4), we discuss how Docker provides process isolation with six
namespaces. In Linux, a namespace partitions kernel resources so that each running
process has its own independent view of those resources. The mount namespace (one
of the six namespaces) helps isolate one container’s view of the filesystem from other
containers, as well from the host filesystem.

Each container sees its own /usr, /var, /home, /opt, and /dev directories. Any
change you make as the root user within a container remains inside the container filesystem.
But when you use a volume (see appendix E, section E.12), which maps a location
in the container filesystem to the host filesystem, the root user can be destructive.
Also, an attacker having access to a container running as root can use root privileges
to install tools within the container and use those tools to find any vulnerability in
other services in the network. In the following sections, we explore the options available
to run a container as an unprivileged process.

### 10.5.1 Running a container with a nonroot user
There are two approaches to running a container with a nonroot user. One way is to use the flag ***--user*** (or ***–u***) in the ***docker run*** command. The other way is to define the user you want to run the container in the Dockerfile itself. 


* **Page 248**,Let’s see how the first approach works. In the following command, we start a Docker container from the ***prabath/insecure-sts-ch10:v1*** image that we’ve already published to Docker Hub:
```bash
\> docker run --name insecure-sts prabath/insecure-sts-ch10:v1
```

Let the container run, and use the following command from a different terminal to connect to the filesystem of the running container (***insecure-sts*** is the name of the container we started in the previous command):
```bash
\> docker exec -it insecure-sts sh
#
```

Now you’re connected to the container filesystem. You can try out any available commands in Alpine Linux there. The id command gives you the user ID (***uid***) and the group ID (***gid***) of the user who runs the container:
```bash
# id
uid=0(root) gid=0(root)
```

Let’s exit from the container, and remove ***insecure-sts*** with the following command run from a different terminal. The –f option in the command removes the container forcefully, even if it is not stopped:

```bash
\> docker rm –f insecure-sts
```

The following command runs ***insecure-sts*** from the ***prabath/insecure-stsch10:v1*** image with the ***–-user*** flag. This flag instructs Docker to run the container with the user having the user ID ***1000*** and the group ID ***800***:
```bash
\> docker run --name insecure-sts --user 1000:800 \
prabath/insecure-sts-ch10:v1
```

Again, let the container run and use the following command from a different terminal to connect to the filesystem of the running container to find the user ID (***uid***) and the group ID (***gid***) of the user who runs the container:
```bash
\> docker exec -it insecure-sts sh
# id
uid=1000 gid=800
```
The second approach to run a container as a nonroot user is to define the user we want to run the container in the Dockerfile itself. This is a good approach if you’re the developer who builds the Docker images, but it won’t help if you’re just the user.

The first approach helps in such a case. In the following listing, let’s have a look at the Dockerfile we used in section 10.1. You can find the source code related to this sample inside the ***chapter10/sample01*** directory.
```dockerfile
FROM openjdk:8-jdk-alpine
ADD target/com.manning.mss.ch10.sample01-1.0.0.jar \
com.manning.mss.ch10.sample01-1.0.0.jar
ENV SPRING_CONFIG_LOCATION=/application.properties
ENTRYPOINT ["java", "-jar", "com.manning.mss.ch10.sample01-1.0.0.jar"]
```

In the code, there’s no instruction to define a user to run this container in the Dockerfile. In such a case, Docker looks for the base image, which is ***openjdk:8-jdkalpine***.You can use the following ***docker inspect*** command to find out the details
of a Docker image. It produces a lengthy output, but if you look for the User element under the ***ContainerConfig*** element, you can find out who the user is:

```bash
\> docker inspect openjdk:8-jdk-alpine
[
    {
        "ContainerConfig": {
            User": ""
        }
    }
]
```
According to the output, even the base image (***openjdk:8-jdk-alpine***) doesn’t instruct Docker to run the corresponding container as a nonroot user. In such a case, by default, Docker uses the root user to run the container. To fix that, we need to update our Dockerfile with the ***USER*** instruction, which asks Docker to run the corresponding container as a user with the user ID ***1000***.
```dockerfile
FROM openjdk:8-jdk-alpine
ADD target/com.manning.mss.ch10.sample01-1.0.0.jar \
com.manning.mss.ch10.sample01-1.0.0.jar
ENV SPRING_CONFIG_LOCATION=/application.properties
USER 1000
ENTRYPOINT ["java", "-jar", "com.manning.mss.ch10.sample01-1.0.0.jar"]
```

### 10.5.2 Dropping capabilities from the root user
Linux kernel 2.2 introduced a new feature called ***capabilities***, which categorizes all the privileged operations a root user can perform. For example, the ***cap_chown*** capability
lets a user execute the ***chown*** operation, which can be used to change the user ID (***uid***) and/or group ID (***gid***) of a file. All these capabilities can be independently enabled or disabled on the root user. This approach lets you start a Docker container as the root user, but with a limited set of privileges.

Let’s use the Docker image we created in section 10.1 to experiment with this approach. The following command starts a Docker container from the ***prabath/insecure-sts-ch10:v1*** image, which we already published to Docker Hub:
```bash
\> docker run --name insecure-sts prabath/insecure-sts-ch10:v1
```

Let the container run, and use the following command (as in section 10.5.1) from a different terminal to connect to the filesystem of the running container to find the user ID (***uid***) and the group ID (***gid***) of the user who runs the container:
```bash
\> docker exec -it insecure-sts sh
# id
uid=0(root) gid=0(root)
```

To find out which capabilities the root user has on the system, we need to run a tool called getpcaps, which comes as part of the libcap package. Because the default distribution of Alpine Linux does not have this tool, we’ll use the Alpine package manager (apk) to install libcap with the following command. Because we’re still inside the container filesystem, this installation has no impact on the host filesystem:
```bash
# apk add libcap
fetch http://dl-cdn.alpinelinux.org/alpine/v3.9/main/x86_64/APKINDEX.tar.gz
fetch http://dl-cdn.alpinelinux.org/alpine/v3.9/community/x86_64/
APKINDEX.tar.gz
(1/1) Installing libcap (2.26-r0)
Executing busybox-1.29.3-r10.trigger
OK: 103 MiB in 55 packages
```


Once the installation completes successfully, we can use the following command to
find out the capabilities associated with the root user:
```bash
# getpcaps root

Capabilities for `root': = cap_chown,cap_dac_override,cap_fowner,cap_fsetid,cap_kill,cap_setgid,cap_setuid,cap_setpcap,cap_net_bind_service,cap_net_raw,cap_sys_chroot,cap_mknod,cap_audit_write,cap_setfcap+eip
```

Let’s remove insecure-sts with the following command run from a different terminal:
```bash
\> docker rm -f insecure-sts
```

The following command runs the ***insecure-sts*** container from the ***prabath/insecure-sts-ch10:v1*** image, with the ***–-cap-drop*** flag. This flag instructs
Docker to drop the ***chown*** capability from the root user who runs the container. The Linux kernel prefixes all capability constants with ***cap_***; for example,   **cap_chown***,***cap_kill***, ***cap_setuid***, and so on. Docker capability constants aren’t prefixed with cap_ but otherwise match the kernel’s constants; for example, ***chown*** instead of ***cap_chown***:
```bash
\> docker run --name insecure-sts --cap-drop chown \
prabath/insecure-sts-ch10:v1
```

Let the container run, and use the following command from a different terminal to
connect to the filesystem of the running container:
```bash
\> docker exec -it insecure-sts sh
```

Because we started a new container, and because the container filesystem is immutable, we need to install libcap again using the following command:
```bash
# apk add libcap
```

If you check the capabilities of the root user again, you’ll see that the cap_chown
capability is missing:
```bash
# getpcaps root
Capabilities for `root': = cap_dac_override,cap_fowner,cap_fsetid,cap_kill,cap_setgid,cap_setuid,cap_setpcap,cap_net_bind_service,cap_net_raw,cap_sys_chroot,cap_mknod,cap_audit_write,cap_setfcap+eip
```


One main benefit of capabilities is that you don’t need to know the user who runs the container. The capabilities you define in the docker run command are applicable to
any user who runs the container. 

   Just as we dropped some capabilities in the ***docker run*** command, we can also add those. The following command drops all the capabilities and adds only one
capability:
```bash
\> docker run --name insecure-sts --cap-drop ALL \
--cap-add audit_write prabath/insecure-sts-ch10:v1
```

## 10.6 Running Docker Bench for security
***Docker Bench for Security*** is a script that checks a Docker deployment for common, wellknown
best practices as defined by the Center for Internet Security (CIS) in the Docker Community Edition Benchmark document (https://downloads.cisecurity.org). It’s maintained as an open source project in the Git repository: https://github.com/docker/docker-bench-security.

This script can be executed either by itself or as a Docker container. The following command uses the second approach, where we run Docker Bench for Security with the Docker image ***docker/docker-bench-security***. It checks the Docker host configuration, Docker daemon configuration, all the container images available in the host machine, and container runtimes for possible vulnerabilities. Here we’ve truncated the output to show you only the important areas covered by the Docker for Security Bench at a high level:

```bash
\> docker run -it --net host --pid host \
--cap-add audit_control -v /var/lib:/var/lib \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /etc:/etc --label docker_bench_security \
docker/docker-bench-security
```
Apart from Docker Bench for Security, a few other alternatives can scan Docker images for known vulnerabilities. Clair is one such open source project (https://github.com/quay/clair) backed by CoreOS (and now RedHat). Anchore (https://github.com/anchore/anchore-engine) is another popular open source project for analyzing vulnerabilities in containers.

