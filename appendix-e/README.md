## Appendix E: Docker fundamentals

<img src="../cover.jpeg" style="float: left; width: 100%" />

[Amazon](https://www.amazon.com/Microservices-Security-Action-Prabath-Siriwardena/dp/1617295957/) | [Manning](https://www.manning.com/books/microservices-security-in-action) | [YouTube](https://www.youtube.com/channel/UCoEOYnrqEcANUgbcG-BuhSA) | [Slack](https://bit.ly/microservices-security) | [Notes](../notes.md) | [Supplementary Readings](../supplementary-readings.md)

## Notes

* **Page 414**, formatting issue on the last line of the code block. Should be correct as: 
```yaml
docker-init:
  Version: 0.18.0
  Gitcommit: fec3683
```

* **Page 418**, using docker to build the image.
```bash
docker build -t com.manning.mss.appendixe.sample01 .
```

* **Page 419**, 
```bash
docker run -p  8443:8443  com.manning.mss.appendixe.sample01 
```
* **Page 420**, using curl to test the contianer.
```
\> curl -v -X POST --basic -u applicationid:applicationsecret \
-H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" \
-k -d "grant_type=password&username=peter&password=peter123&scope=bar" \
https://localhost:8443/oauth/token
```

* **Page 427**, using [dive](https://github.com/wagoodman/dive/releases/tag/v0.10.0) to explore each layer in a given Docker image.

1.  Ubuntu/Debian

```bash
wget https://github.com/wagoodman/dive/releases/download/v0.10.0/dive_0.10.0_linux_amd64.deb
sudo apt install ./dive_0.10.0_linux_amd64.deb
```
2.  RHEL/Centos
```bash
curl -OL https://github.com/wagoodman/dive/releases/download/v0.10.0/dive_0.10.0_linux_amd64.rpm
rpm -i dive_0.9.2_linux_amd64.rpm
```
3.  When running you'll need to include the docker socket file:
```bash
docker pull wagoodman/dive:latest

docker run --rm -it \
    -v /var/run/docker.sock:/var/run/docker.sock \
    wagoodman/dive:latest <dive arguments...>
```

4.  Docker for Windows (showing PowerShell compatible line breaks; collapse to a single line for Command Prompt compatibility)
```powershell
docker pull wagoodman/dive:latest

docker run --rm -it `
    -v /var/run/docker.sock:/var/run/docker.sock `
    wagoodman/dive:latest <dive arguments...>
```

5.  depending on the version of docker you are running locally you may need to specify the docker API version as an environment variable:
```bash
   DOCKER_API_VERSION=1.37 dive ...
```
or if you are running with a docker image:
```
docker run --rm -it \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -e DOCKER_API_VERSION=1.37 \
    wagoodman/dive:latest <dive arguments...>
```