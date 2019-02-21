# Json client

Sample project for [Ktor](https://ktor.io) HTTP client with JSON support feature. 

It runs against either one of the following server-side samples:
* [gson](../gson/README.md) &mdash; using [Gson](https://github.com/google/gson).
* [jackson](../jackson/README.md) &mdash; using [Jackson](https://github.com/FasterXML/jackson).

## Running

First run one of the server-side samples.

Then run this project with:

```
./gradlew :json-client:run
```

The resulting output is:

```text
Requesting model...
Fetching items for 'root'...
Received: Item(key=A, value=Apache)
Received: Item(key=B, value=Bing)
```