# Google Appengine Standard

Sample project for [Ktor](https://ktor.io) running under [Google App Engine](https://cloud.google.com/appengine/)
standard infrastructure. 

## Prerequisites

* [Java SDK 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or later
* [Apache Maven](https://maven.apache.org)
* [Google Cloud SDK](https://cloud.google.com/sdk/docs/)

## Running

Run this project under local dev mode with:

```
gradle appengineRun
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.  

## Deploying

Use Google Cloud SDK to create application similarly to 
[Google App Engine for Java Quickstart](https://cloud.google.com/appengine/docs/standard/java/quickstart):

Install all the Google Cloud components and login into your account:

```
gcloud init
gcloud components install app-engine-java
gcloud components update  
gcloud auth application-default login
```

Create project and application:

```
gcloud projects create <unique-project-id> --set-as-default
gcloud app create
```                                

Then deploy your application with:

```
gradle appengineDeploy
```

You can checkout deployed version of this sample application at
https://ktor-sample.appspot.com


## Converting to Google App Engine

You'll need to remove the `deployment` block from `application.conf`, otherwise when running on Google App Engine the `Servlet` will not get it's environment configured correctly causing the Google Cloud API's to fail.

