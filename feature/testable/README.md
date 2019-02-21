# Testable

Sample project for [Ktor](https://ktor.io) demonstrating tests for Ktor applications.

## Running

Execute this command in the repository's root directory to run this sample:

```bash
./gradlew :testable:run
```
 
And navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page
with a simple text string.

## Writing tests

The simple test code looks like this:

```kotlin
   fun testRequests() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Hello from Ktor Testable sample application", response.content)
        }
    }
```  

> See [ApplicationTest.kt](test/ApplicationTest.kt)

## Running tests

To run the tests:

```bash
./gradlew :testable:test
```
