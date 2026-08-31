# Build from source

Use the included Gradle wrapper. The toolchain resolver can download the
required JDK when needed. Gradle itself runs on Java 21 or newer; the project
daemon configuration selects Java 21 automatically. The 1.20.1 game targets
compile and run with Java 17.

```bash
./gradlew :1.20.1-fabric:buildAndCollect \
  :1.20.1-forge:buildAndCollect \
  :1.21.1-fabric:buildAndCollect \
  :1.21.1-neoforge:buildAndCollect
```

The JARs and source JARs are written to:

```text
build/libs/<mod-version>/
```

To run a development client, use one of these commands:

```bash
./gradlew :1.20.1-fabric:runClient
./gradlew :1.20.1-forge:runClient
./gradlew :1.21.1-fabric:runClient
./gradlew :1.21.1-neoforge:runClient
```

The 1.20.1 targets use Fabric Loader/Fabric API and Forge respectively; the
1.21.1 targets use Fabric and NeoForge respectively.

The build command validates compilation and packaging for every supported
target. Runtime checks should use the corresponding `runClient` task; a
dedicated server also requires accepting Minecraft's EULA in its generated
`run/eula.txt` file.
