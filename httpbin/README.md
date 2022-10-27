# HttpBin

HttpBin application that implements (large parts of) [httpbin(1)](https://httpbin.org/) HTTP request & response service.

## Running

Execute this command to run this sample:

```bash
./gradlew run
```

Then, navigate to [http://localhost:8080/](http://localhost:8080/) to see the sample home page.

## Endpoints


| Location                         | Description                                                                                        |
|----------------------------------|----------------------------------------------------------------------------------------------------|
| /                                | HTML page describing the service                                                                   |
| /postman                         | Downloads postman collection for httpbin                                                           |
| /ip                              | Returns Origin IP.                                                                                 |
| /user-agent                      | Returns user-agent.                                                                                |
| /headers                         | Returns header dict.                                                                               |
| /get                             | Returns GET data.                                                                                  |
| /post                            | Returns POST data.                                                                                 |
| /forms/post                      | HTML form that submits to /post                                                                    |
| /patch                           | Returns PATCH data.                                                                                |
| /put                             | Returns PUT data.                                                                                  |
| /delete                          | Returns DELETE data                                                                                |
| /encoding/utf8                   | Returns page containing UTF-8 data.                                                                |
| /status/:code                    | Returns given HTTP Status code.                                                                    |
| /html                            | Renders an HTML Page.                                                                              |
| /robots.txt                      | Returns some robots.txt rules.                                                                     |
| /deny                            | Denied by robots.txt file.                                                                         |
| /cache                           | Returns 200 unless an If-Modified-Since or If-None-Match header is provided,when it returns a 304. |
| /cache/:n                        | Sets a Cache-Control header for n seconds.                                                         |
| /links/:n                        | Returns page containing n HTML links.                                                              |
| /image                           | Returns page containing an image based on sent Accept header.                                      |
| /image/png                       | Returns page containing a PNG image.                                                               |
| /image/jpeg                      | Returns page containing a JPEG image.                                                              |
| /image/webp                      | Returns page containing a WEBP image.                                                              |
| /image/svg                       | Returns page containing a SVG image.                                                               |
| /xml                             | Returns some XML                                                                                   |
| /encoding/utf8                   | Returns page containing UTF-8 data.                                                                |
| /gzip                            | Returns gzip-encoded data.                                                                         |
| /deflate                         | Returns deflate-encoded data.                                                                      |
| /throw                           | Returns HTTP 500 server error                                                                      |
| /someInvalidEndpoint             | Returns a customized HTTP 404 json error                                                           |
| /cookies                         | Returns the cookies                                                                                |
| /cookies/set?name=value          | Set new cookies                                                                                    |
| /cookies/delete?name             | Delete specified cookies                                                                           |
| /redirect/:n                     | Redirect n times                                                                                   |
| /redirect-to?url=                | Redirect to an URL                                                                                 |
| /delay/:n                        | Delays responding for n seconds.                                                                   |
| /stream/:n                       | Streams n lines.                                                                                   |
| /cache/:n                        | Sets a Cache-Control header for n seconds.                                                         |
| /bytes/:n                        | Generates n random bytes of binary data                                                            |
| /basic-auth + Authorization      | Challenges HTTPBasic Auth.                                                                         |
| /basic-auth/:user/:passwd        | Challenges HTTPBasic Auth.                                                                         |
| /hidden-basic-auth/:user/:passwd | 404'd BasicAuth.                                                                                   |
