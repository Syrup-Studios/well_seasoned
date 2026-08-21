# Build from source

Use the included Gradle wrapper. The toolchain resolver can download the
required JDK when needed.

```bash
./gradlew :1.21.1-fabric:buildAndCollect \
  :1.21.1-neoforge:buildAndCollect
```

The JARs and source JARs are written to:

```text
build/libs/<mod-version>/
```

To run a development client, use one of these commands:

```bash
./gradlew :1.21.1-fabric:runClient
./gradlew :1.21.1-neoforge:runClient
```
