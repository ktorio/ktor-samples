# Version diff

A diff tool for maven artifacts written using the [Ktor](https://ktor.io) client.

## Running

Execute this command in a sample directory:

```bash
./gradlew run --args="1.6.8 2.2.2"
```

* `1.6.8` and `2.2.2` are the versions of the Maven artifact to compare.
* By default, the repository URL is https://repo.maven.apache.org/maven2/io/ktor.
  If you want to use a repository other than the default, provide it explicitly as the third argument:
  ```bash
./gradlew run --args="2.3.13 3.0.0 https://oss.sonatype.org/content/repositories/releases/io/ktor"
```