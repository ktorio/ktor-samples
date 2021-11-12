# Docker Image Build sample for Ktor project

A demo project which shows how to configure building, running and publishing of Docker image with your application.

## Pre-requirements

Make sure you have [Docker](https://docs.docker.com/engine/install/) installed (you also can use [Podman](https://podman.io/getting-started/installation)
as alternative). Please make sure the Docker daemon is up and running before building an image.

## Building an Image

To build an image to the local Docker registry run:

```shell
./gradlew jibDockerBuild
```


If you are using [Minikube's](https://github.com/kubernetes/minikube) remote Docker daemon, make sure you set up the correct environment variables to point to the remote daemon:
```shell
eval $(minikube docker-env)
./gradlew jibDockerBuild
```

After that you can see `ktor-container-image:0.0.1` in your local image registry. The version of the image is taken from your project configuration.

```shell
docker run -p 8080:8080 ktor-container-image:0.0.1
```

## Build Image to an External Registry

To build an image to an external registry you should run:
```shell
IMAGE=`MY IMAGE PAHT` # gcr.io/my-gcp-project/my-app or aws_account_id.dkr.ecr.region.amazonaws.com/my-app
./gradlew jib  --image=$IMAGE
```
