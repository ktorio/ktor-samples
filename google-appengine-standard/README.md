# google-appengine-standard

Sample project for [Ktor](http://ktor.io) running under [Google Appengine](https://cloud.google.com/appengine/)
standard infrastructure. 

## Prerequisites

* [Java SDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later
* [Apache Maven](https://maven.apache.org)
* [Google Cloud SDK](https://cloud.google.com/sdk/docs/)

## Running

Run this project under local dev mode with:

```
mvn appengine:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the home page.  

## Deploying

Use Google Cloud SDK to create application similarly to 
[Google Appengine for Java Quickstart](https://cloud.google.com/appengine/docs/standard/java/quickstart):

```
gcloud init
gcloud auth application-default login
gcloud components install app-engine-java
gcloud components update  
```                                

Then deploy your application with:

```
mvn appengine:deploy
```