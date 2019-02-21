# Gson

Sample project for [Ktor](https://ktor.io) demonstrating content negotiation feature
using [Gson](https://github.com/google/gson).

## Running

Run this project with:

```
./gradlew :gson:run
```
 
Use the following command scripts for testing:

```bash
curl -v --compress --header "Accept: application/json" http://localhost:8080/v1
```

Should respond with something like:

```json
{
  "name": "root",
  "items": [
    {
      "key": "A",
      "value": "Apache"
    },
    {
      "key": "B",
      "value": "Bing"
    }
  ],
  "date": {
    "year": 2018,
    "month": 3,
    "day": 2
  }
}
```

The result is pretty printed, to show off how to configure gson, but it is possible to use the default gson as well

Another test:

```bash
curl -v --compress --header "Accept: application/json" http://localhost:8080/v1/item/A
```
 
Response:

```json
{
  "key": "A",
  "value": "Apache"
}
```
        
## Using HTTP client

You can make requests to this sample server using Ktor HTTP client. 
See [json-client](../json-client/README.md) sample.