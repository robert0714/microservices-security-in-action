## Chapter 10: Conquering container security with Docker (sample01)

[https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample01](https://github.com/microservices-security-in-action/samples/tree/master/chapter10/sample01)

### 10.1 Running the security token service on Docker

* **Page 231**, To build the STS project and create a Docker image, run the first two commands
in listing 10.1 from the chapter10/sample01 directory. Then run the third command
in listing 10.1 to spin up the STS Docker container from the image you built. (Make
sure that you have no other services running on port 8443 on your local machine.)
These are standard commands that you’d use in any Docker project (section E.4 of
appendix E explains these in detail in case you’re not familiar with Docker).

```bash
\> mvn clean package
\> docker build -t com.manning.mss.ch10.sample01:v1 .
\> docker run -p 8443:8443 com.manning.mss.ch10.sample01:v1
```

Now let’s test the STS with the following curl command. This is the same curl command
we used in section 7.6:
```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token  |jq "."
```

In this command, the client ID of the web application is applicationid, and the client
secret (which is hardcoded in the STS) is applicationsecret. If everything works
fine, the STS returns an OAuth 2.0 access token, which is a JWT (or a JWS, to be precise):
```json
{
"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NTEzMTI
zNzYsInVzZXJfbmFtZSI6InBldGVyIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0
aSI6IjRkMmJiNjQ4LTQ2MWQtNGVlYy1hZTljLTVlYWUxZjA4ZTJhMiIsImNsaWVudF9pZCI6I
mFwcGxpY2F0aW9uaWQiLCJzY29wZSI6WyJmb28iXX0.tr4yUmGLtsH7q9Ge2i7gxyTsOOa0RS
0Yoc2uBuAW5OVIKZcVsIITWV3bDN0FVHBzimpAPy33tvicFROhBFoVThqKXzzG00SkURN5bnQ
4uFLAP0NpZ6BuDjvVmwXNXrQp2lVXl4lQ4eTvuyZozjUSCXzCI1LNw5EFFi22J73g1_mRm2jdEhBp1TvMaRKLBDk2hzIDVKzu5oj_
gODBFm3a1S-IJjYoCimIm2igcesXkhipRJtjNcrJSegB
bGgyXHVak2gB7I07ryVwl_Re5yX4sV9x6xNwCxc_DgP9hHLzPM8yz_K97jlT6Rr1XZBlVeyjf
Ks_XIXgU5qizRm9mt5xg",
"token_type":"bearer",
"refresh_token":"",
"expires_in":5999,
"scope":"foo",
"jti":"4d2bb648-461d-4eec-ae9c-5eae1f08e2a2"
}
```

### 10.2 Managing secrets in a Docker container

* **Page 232**, We’ve already published the insecure STS Docker image we created in section 10.1
to the Docker Hub as ***prabath/insecure-sts-ch10:v1***, and it’s available to the
public. Anyone can execute the following docker run command to fetch the insecure
STS image and run it locally. (If you’re new to Docker, appendix E teaches you
how to publish a Docker image to Docker Hub.)

```bash
\> docker run -d  --name sts prabath/insecure-sts-ch10:v1

34839d0c9e3b32b5f4fa6a8b4f7f52c134ed3e198ad739b722ca556903e74fbc
```
Once the container starts, we can use the following command to connect to the container
with the container ID and access the running shell. (Use the full container ID
from the output of the previous docker run command.)

```bash
\> docker exec -it 34839d0c9e3b32b5f4fa6a8b4f7f52c134… sh
#
```
or 

```bash
\> docker exec -it sts sh
#
```
Now we’re on the container’s shell and have direct access to its filesystem. Let’s first
list all the files under the root of the container filesystem:

```bash
/ # ls
bin
com.manning.mss.ch10.sample01-1.0.0.jar
dev
etc
home
lib
media
mnt
opt
proc
root
run
sbin
srv
sys
tmp
usr
var
```

Now we can unzip the com.manning.mss.ch10.sample01-1.0.0.jar file and find all the
secrets in the application.properties file:

```bash
/ # jar -xvf com.manning.mss.ch10.sample01-1.0.0.jar
/ # vi BOOT-INF/classes/application.properties
```

The command displays the content of the application.properties file, which includes
the credentials to access the private key that’s used by the STS to sign the JWTs it
issues, as shown in the following listing.

```properties 
server.port: 8443

# Keeps the private and public keys of the service to  use in TLS communications
server.ssl.key-store: /opt/keystore.jks

server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
spring.security.oauth.jwt: true
spring.security.oauth.jwt.keystore.password: springboot
spring.security.oauth.jwt.keystore.alias: jwtkey

# Keeps the private key,which is used by the STS to sign the JWTs it issues
spring.security.oauth.jwt.keystore.name: /opt/jwt.jks
```

### 10.2.1 Externalizing secrets from Docker images


* **Page 233**, Let’s create a directory called config under ***chapter10/sample01*** and ***move*** (not just copy) the application.properties file from the ***chapter10/sample01/src/main/resources/*** directory to the new directory (***chapter10/sample01/config***). 

The sample you downloaded from the GitHub already has the config directory; probably you can
delete it and create a new one. Then, let’s run the following two commands in listing
10.3 from the ***chapter10/sample01*** directory to build a new JAR file without the ***application.properties*** file and create a Docker image. This new Docker image won’t have the two keystores (***keystore.jks*** and ***jwt.jks***) and the ***application.properties*** file in it.

```bash
\> mvn clean pacakge
[INFO] BUILD SUCCESS

\> docker build -t com.manning.mss.ch10.sample01:v2 -f Dockerfile-2 .
Sending build context to Docker daemon  28.63MB
Step 1/4 : FROM openjdk:8-jdk-alpine
8-jdk-alpine: Pulling from library/openjdk
e7c96db7181b: Already exists
f910a506b6cb: Already exists
c2274a1a0e27: Already exists
Digest: sha256:94792824df2df33402f201713f932b58cb9de94a0cd524164a0f2283343547b3
Status: Downloaded newer image for openjdk:8-jdk-alpine
 ---> a3562aa0b991
Step 2/4 : ADD target/com.manning.mss.ch10.sample01-1.0.0.jar com.manning.mss.ch10.sample01-1.0.0.jar
 ---> 8e7079d30538
Step 3/4 : ENV SPRING_CONFIG_LOCATION=/opt/application.properties
 ---> Running in a0a44244b6bc
Removing intermediate container a0a44244b6bc
 ---> 4913423b1734
Step 4/4 : ENTRYPOINT ["java", "-jar", "com.manning.mss.ch10.sample01-1.0.0.jar"]
 ---> Running in 953e091d9453
Removing intermediate container 953e091d9453
 ---> 2de0f79076eb
Successfully built 2de0f79076eb
Successfully tagged com.manning.mss.ch10.sample01:v2
```

You may notice a difference in this command from the command we ran in listing
10.1 to create a Docker image. In listing 10.3, we pass an extra argument, called ***-f***,
with the value ***Dockerfile-2***. This is how we can instruct Docker to use a custom file
as the manifest to create a Docker image instead of looking for a file with the name
Dockerfile Let’s have a look at the content of ***Dockerfile-2***, as shown in the following listing.
```dockerfile
FROM openjdk:8-jdk-alpine
ADD target/com.manning.mss.ch10.sample01-1.0.0.jar com.manning.mss.ch10.sample01-1.0.0.jar
ENV SPRING_CONFIG_LOCATION=/opt/application.properties
ENTRYPOINT ["java", "-jar", "com.manning.mss.ch10.sample01-1.0.0.jar"]
```

The third line instructs Docker to create an environment variable called ***SPRING_CONFIG_LOCATION*** and point it to the ***/opt/application.properties*** file. The process running inside the container reads this environment variable to find the location of the ***application.properties*** file; then it looks for the file under the ***/opt*** directory of the container filesystem. Finally, the fourth line tells Docker the entry point to the container, or which process to run when we start the container.


* **Page 234**, Let’s run the commands in the following listing from the ***chapter10/sample01*** directory to spin up a container from the Docker image we just created. If you’ve carefully looked into the command (listing 10.3) we used to build the Docker image, we tagged it this time with ***v2***, so we need to use the image in the following listing with the ***v2*** tag.

```bash
export JKS_SOURCE="$(pwd)/keystores/keystore.jks"
export JKS_TARGET="/opt/keystore.jks"
export JWT_SOURCE="$(pwd)/keystores/jwt.jks"
export JWT_TARGET="/opt/jwt.jks"
export APP_SOURCE="$(pwd)/config/application.properties"
export APP_TARGET="/opt/application.properties"

docker run -p 8443:8443 \
--mount type=bind,source="$JKS_SOURCE",target="$JKS_TARGET" \
--mount type=bind,source="$JWT_SOURCE",target="$JWT_TARGET" \
--mount type=bind,source="$APP_SOURCE",target="$APP_TARGET" \
com.manning.mss.ch10.sample01:v2
```

Once we start the container successfully, we see the following logs printed on the terminal:
```bash
INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8443 (https) with context path ''
INFO 1 --- [main] c.m.mss.ch10.sample01.TokenService       : Started TokenService in 7.514 seconds (JVM running for 8.404)
```

Now let’s test the STS with the following curl command. This is the same curl command
we used in section 10.1.2:
```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token  |jq "."
```

### 10.2.2 Passing secrets as environment variables

Once we externalize the configuration files and keystores from the Docker image, no
one will be able to find any secrets in it. But we still have secrets hardcoded in a configuration file that we keep in the host filesystem. Anyone who has access to the host filesystem will be able to find those. In this section, let’s see how to remove the secrets
from the configuration file (***application.properties***) and pass them to the container as
arguments at runtime.

Let’s copy the content from the ***chapter10/sample01/application.properties*** file
and replace the content in the ***chapter10/sample01/config/application.properties***
file with it. The following listing shows the updated content of the ***chapter10/sample01/config/application.properties*** file.
```properties
server.port: 8443
server.ssl.key-store: /opt/keystore.jks
server.ssl.key-store-password: ${KEYSTORE_SECRET}
server.ssl.keyAlias: spring
spring.security.oauth.jwt: true
spring.security.oauth.jwt.keystore.password: ${JWT_KEYSTORE_SECRET}
spring.security.oauth.jwt.keystore.alias: jwtkey
spring.security.oauth.jwt.keystore.name: /opt/jwt.jks
server.ssl.client-auth:want
```

* **Page 236**, Here, we’ve removed all the secrets from the application.properties file and replaced them with two placeholders: ***${KEYSTORE_SECRET}*** and ***${JWT_KEYSTORE_SECRET}.*** Because our change is only in a file we’ve already externalized from the Docker image, we don’t need to build a new image. Let’s spin up a container of the STS Docker image with the command in the following listing (run from the ***chapter10/sample01*** directory) with the updated ***application.properties*** file.

```bash
export JKS_SOURCE="$(pwd)/keystores/keystore.jks"
export JKS_TARGET="/opt/keystore.jks"
export JWT_SOURCE="$(pwd)/keystores/jwt.jks"
export JWT_TARGET="/opt/jwt.jks"
export APP_SOURCE="$(pwd)/config/application.properties"
export APP_TARGET="/opt/application.properties"

docker run -p 8443:8443 \
--mount type=bind,source="$JKS_SOURCE",target="$JKS_TARGET" \
--mount type=bind,source="$JWT_SOURCE",target="$JWT_TARGET" \
--mount type=bind,source="$APP_SOURCE",target="$APP_TARGET" \
-e KEYSTORE_SECRET=springboot \
-e JWT_KEYSTORE_SECRET=springboot \
com.manning.mss.ch10.sample01:v2
```

Here we pass the values corresponding to the placeholders we kept in the application.
properties file as an argument to the ***docker run*** command under the name ***–e***.
Once we start the container successfully, we’ll see the following logs printed on the terminal:
```bash
INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8443 (https) with context path ''
INFO 1 --- [main] c.m.mss.ch10.sample01.TokenService       : Started TokenService in 7.226 seconds (JVM running for 8.17
```

Now let’s test the STS with the following curl command. This is the same curl command
we used in section 10.2.1:
```bash
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=foo" \
https://localhost:8443/oauth/token   
```

### 10.3 Using Docker Content Trust to sign and verify Docker images
#### 10.3.3 Generating keys

* **Page 238**, Let’s use the following command to generate keys for signing with DCT. 
The ***docker trust key generate*** command generates a ***public/private key pair*** with a key ID and stores the corresponding public key in the filesystem under the same directory where
you ran the command. You can find the corresponding private key under ***~/.docker/trust/private*** directory. The key ID is generated by the system and is mapped to the given name of the signer (in this example, ***prabath*** is the name of the signer). Also, while generating the key, you will be asked to enter a passphrase and will need to know the passphrase when you use the generated key later:

```bash
\> docker trust key generate prabath

Generating key for prabath...
Enter passphrase for new prabath key with ID 1a60acb:XXXXX
Repeat passphrase for new prabath key with ID 1a60acb: XXXXX
Successfully generated and loaded private key. Corresponding public key available: /Users/prabathsiriwardana/dct/prabath.pub
```

The key generated by the command is called a ***delegation*** key, which you can find under
the same location you ran the command. Since we use ***prabath*** as the signer in the
command, the generated key carries the name ***prabath.pub***.

Next, we need to associate the public key of the delegation key with a Docker repository
(don’t get it confused with a Docker registry; if you’re new to Docker, see appendix
E for the definition). Run the following command from the same location that you ran
the previous one to associate the public key of the delegation key with the ***prabath/insecure-sts-ch10*** repository. You should use your own repository (instead of ***prabath/insecure-sts-ch10***) in the following command, with your own key
(instead of ***prabath.pub*** ) and your own signer name (instead of ***prabath***).
We’ve already created this repository in Docker Hub with the image we built in section 10.1.

If you get a 401 error response when you run the following command, that means you have not logged into the Docker Hub—and you can use the ***docker login***  command to log in. 

When we run the following command for the first time, it generates two more key pairs: the ***root key pair*** and the ***target key pair***, and during the key generation process, for each key pair you will be asked to enter a passphrase:

```bash
docker trust signer add --key prabath.pub prabath \
prabath/insecure-sts-ch10

Adding signer "prabath" to prabath/insecure-sts-ch10...
could not find necessary signing keys, at least one of these keys must be available: 3a23435884932908069c39fd68f4c44c378d3688008b2bcb877a9221ab2ed9a6

Failed to add signer to: prabath/insecure-sts-ch10
```

The ***–-key*** argument takes the public key (***prabath.pub***) of the delegation key as
the value and then the name of the signer (***prabath***). Finally, at the end of the command,
you can specify one or more repositories delimited by a space. DCT generates a
target key pair for each repository. Because we specify only one repository in the command,
it generates only one target key pair. The root key signs each of these target
keys. Target keys are also known as ***repository keys***. All the generated private keys corresponding
to the root, target, and delegation keys in the previous code example are, by
default, available in the ***~/.docker/trust/private*** directory. The following shows the
scrambled private keys:

```bash
-----BEGIN ENCRYPTED PRIVATE KEY-----
role: root
MIHuMEkGCSqGSIb3DQEFDTA8MBsGCSqGSIb3DQEFDDAOBAgwNkfrd4OJDQICCAAw
==
-----END ENCRYPTED PRIVATE KEY-----
-----BEGIN ENCRYPTED PRIVATE KEY-----
gun: docker.io/prabath/manning-sts
role: targets
MIHuMEkGCSqGSIb3DQEFDTA8MBsGCSqGSIb3DQEFDDAOBAhs5CaEbLT65gICCAAw
==
-----END ENCRYPTED PRIVATE KEY-----
-----BEGIN ENCRYPTED PRIVATE KEY-----
role: prabath
MIHuMEkGCSqGSIb3DQEFDTA8MBsGCSqGSIb3DQEFDDAOBAiX8J+5px9aogICCAAw
==
-----END ENCRYPTED PRIVATE KEY-----
```

### 10.3.4 Signing with DCT

* **Page 240**,Let’s use the following command to sign the ***prabath/insecure-sts-ch10:v1***
Docker image with the delegation key that we generated in the previous section under
the name ***prabath***. This is, in fact, the signer’s key and you should use your own
image (instead of ***prabath/insecure-sts-ch10:v1***) in the following command.
Also please note that here we are signing a Docker image with a tag, not a repository:

```bash
\> docker trust sign prabath/insecure-sts-ch10:v1

Signing and pushing trust data for local image
prabath/insecure-sts-ch10:v1, may overwrite remote trust data
The push refers to repository [docker.io/prabath/insecure-sts-ch10]
be39ecbbf21c: Layer already exists
4c6899b75fdb: Layer already exists
744b4cd8cf79: Layer already exists
503e53e365f3: Layer already exists
latest: digest:
sha256:a3186dadb017be1fef8ead32eedf8db8b99a69af25db97955d74a0941a5fb502
size: 1159
Signing and pushing trust metadata
Enter passphrase for prabath key with ID 706043c: XXXXX
Successfully signed docker.io/prabath/insecure-sts-ch10:v1
```
If you failed as the below:

```bash
\> docker trust sign prabath/insecure-sts-ch10:v1

Signing and pushing trust metadata for prabath/insecure-sts-ch10:v1
failed to sign docker.io/prabath/insecure-sts-ch10:v1: no valid signing keys for delegation roles
```

Now we can use the following command to publish the signed Docker image to
Docker Hub:
```bash
\> docker push prabath/insecure-sts-ch10:v1
The push refers to repository [docker.io/prabath/insecure-sts-ch10]
be39ecbbf21c: Layer already exists
4c6899b75fdb: Layer already exists
744b4cd8cf79: Layer already exists
503e53e365f3: Layer already exists
latest: digest:
sha256:a3186dadb017be1fef8ead32eedf8db8b99a69af25db97955d74a0941a5fb502
size: 1159
Signing and pushing trust metadata
Enter passphrase for prabath key with ID 706043c:
Passphrase incorrect. Please retry.
Enter passphrase for prabath key with ID 706043c:
Successfully signed docker.io/prabath/insecure-sts-ch10:v1
```
Once we publish the signed image to Docker Hub, we can use the following command to inspect the trust data associated with it:
```bash
\> docker trust inspect --pretty prabath/insecure-sts-ch10:v1

Signatures for prabath/insecure-sts-ch10:v1
SIGNED TAG   DIGEST                     SIGNERS
v1           0f99bb308437528da436c13369 prabath

List of signers and their keys for prabath/insecure-sts-ch10:v1

SIGNER      KEYS
prabath     706043cc4ae3

Administrative keys for prabath/insecure-sts-ch10:v1

Repository  Key
44f0da3f488ff4d4870b6a635be2af60bcef78ac15ccb88d91223c9a5c3d31ef


Root Key
5824a2be3b4ffe4703dfae2032255d3cbf434aa8d1839a2e4e205d92628fb247
```

#### 10.3.5 Signature verification with DCT
Out-of-the-box content trust is disabled on the Docker client side. To enable it, we need to set the ***DOCKER_CONTENT_TRUST*** environment variable to ***1***, as shown in the
following command:
```bash
\> export DOCKER_CONTENT_TRUST=1
```

Once content trust is enabled, the Docker client makes sure all the ***push, build,create, pull***, and ***run*** Docker commands are executed only against signed images.
The following command shows what happens if we try to run an unsigned Docker image:
```bash
\> docker run prabath/insecure-sts-ch10:v2

docker: Error: remote trust data does not exist for docker.io/prabath/insecure-sts-ch10:v2: notary.docker.io does not have trust data for docker.io/prabath/insecure-sts-ch10:v2.
```

To disable content trust, we can override the value of the ***DOCKER_CONTENT_TRUST*** environment variable to be empty, as shown in the following command:

```bash
\> export DOCKER_CONTENT_TRUST=
```

#### 10.3.6 Types of keys used in DCT
DCT uses five types of keys: the root key, the target key, the delegation key, the timestamp key, and the snapshot key. So far, we know about only the root, the target, and the delegation keys. The target key is also known as repository key. Figure 10.2 shows the hierarchical relationship among the different types of keys.

The root key, which is also known as the ***offline key***, is the most important key in DCT. It has a long expiration and must be protected with highest security. It’s recommended to keep it offline (that’s how the name ***offline key*** was derived), possibly in a USB or another kind of offline device. A developer or an organization owns the root key and uses it to sign other keys in DCT.

When you sign a Docker image with a delegation key, a set of trust data gets associated with that image, which you can find in your local filesystem in the ***~/.docker/trust/tuf/docker.io/[repository_name]/metadata*** directory. Also, you will find the same set of files in the same location of your filesystem when you pull a signed Docker image. For example, the metadata for ***prabath/insecure-sts-ch10*** is in the ***~/.docker/trust/tuf/docker.io/prabath/insecure-sts-ch10/metadata*** directory. The following shows the list of files available in the metadata directory:

```bash
\> cd ~/.docker/trust/tuf/docker.io/prabath/insecure-sts-ch10/metadata/
\> ls
root.json  snapshot.json  targets  targets.json  timestamp.json
\> ls targets
prabath.json  releases.json 
\> 
```

The root key signs the root.json file, which lists all the valid public keys corresponding to the ***prabath/insecure-sts-ch10*** repository. These public keys include the root key, target key, snapshot key, and timestamp key.

The target key (referred from the root.json file) signs the target.json file, which lists all the valid delegation keys. Inside the target.json file, you will find a reference to the delegation key that we generated before under the name ***prabath***. DCT generates a ***target key*** per each Docker repository, and the root key signs each target key. Once we generate the root key, we need it again only when we generate target keys. A given repository has one target key, but multiple delegation keys.

DCT uses these ***delegation keys*** to sign and push images to repositories. You can use different delegation keys to sign different tags of a given image. If you look at the metadata/target directory, you will find a file named under the delegation key we generated:
***prabath.json***. This file, which is signed by the delegation key, carries the hash of the ***insecure-sts-ch10:v1*** Docker image. If we sign another tag, say ***insecure-sts-ch10:v2 with*** the same delegation key, DTC will update the prabath .json file with the v2 hash.

The ***snapshot key*** (which is referred from the root.json file) generated by DCT signs the snapshot.json file. This file lists all the valid trust metadata files (except timestamp.json), along with the hash of each file.

The ***timestamp key*** (which is referred from the root.json file) signs the timestamp. json file, which carries the hash of the currently valid snapshot.json file. The timestamp key has a short expiration period, and each time DCT generates a new timestamp key, it re-signs the timestamp.json file. DCT introduced the timestamp key to protect client applications from replay attacks, and we discuss in the next section how DCT does that.

### 10.3.7 How DCT protects the client application from replay attacks

An attacker can execute a replay attack by replaying previously valid trust metadata files, which we discussed in section 10.3.6, along with an old Docker image. This old Docker image could have some vulnerabilities, which are fixed by the latest image published to the registry by the publisher. However, because of the replay attack by the attacker, the victim would think they had the latest version of the Docker image, which is also properly signed by its publisher.

When you pull an image from a Docker registry, you also get the trust metadata associated with it, which you can find in your local filesystem in the ***~/.docker/trust/tuf/docker.io/[repository_name]/metadata*** directory. However, if the attacker manages to replay old metadata files to your system, you won’t have access to the latest. As we discussed in section 10.3.6, DCT introduced the timestamp metadata file to fix this issue.

DCT generates a timestamp file, with an updated timestamp and a version, every time the publisher publishes a Docker image to the corresponding repository. The timestamp key signs this timestamp file, and the timestamp file includes the hash of
the snapshot.json file. And, the snapshot.json file includes the hash of the updated (or the new) Docker image.

Whenever a Docker image gets updated at the client side, DCT will download the latest timestamp.json file from the corresponding repository. Then it will validate the signature of the downloaded file (which was replayed by the attacker in this case) and will check whether the version in the downloaded timestamp file is greater than the one in the current timestamp file. If the downloaded file, which is replayed by the attacker, has an older version, DCT will abort the update operation, and will protect the system from the replay attack, which tries to downgrade your Docker image to an older version with vulnerabilities.