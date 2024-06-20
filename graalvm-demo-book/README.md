# Lecture GraalVM

## Build

The following sections show commands to build for JVM and Native. All commands are completely independent of each other.

### JVM

Hint: In order to build the Jar locally, JDK 21 or higher is required.
Alternatively, use the Docker image builds, which are independent of your local JDK.

Build Jar
```bash
./mvnw clean package
```

Build Docker Image with Dockerfile
```bash
docker build -t graalvm-demo-book-jvm:0.1.0 -f Dockerfile.jvm .
```

Build Docker Image for another platform with Dockerfile
```bash
docker build --platform linux/arm64 -t graalvm-demo-book-jvm-arm64:0.1.0 -f Dockerfile.jvm .
```

#### JLink Optimized

jlink is a tool that generates a custom Java runtime image that contains only the platform modules that are required for a given application (https://www.baeldung.com/jlink). Also see: https://adoptium.net/blog/2021/10/jlink-to-produce-own-runtime/

Build Docker Image with Dockerfile
```bash
docker build -t graalvm-demo-book-jvm-jlink:0.1.0 -f Dockerfile.util.jvm-jlink .
```

### Native (GraalVM)

Hint: In order to build the native binary locally, GraalVM or GraalVM Community 21 or higher is required.
ToDo: Write whats needed for Linux and Windows.

The recommended approach to build it is to use the Docker image builds, which are independent of your local GraalVM and host OS.

Download:
  * GraalVM Community 21: https://github.com/graalvm/graalvm-ce-builds/releases/tag/jdk-21.0.2
  * GraalVM Oracle 21: https://www.graalvm.org/downloads/

Build native binary
```bash
./mvnw -Pnative clean native:compile
```

Build statically linked native binary (only works if [musl toolchain is available](https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/))
```bash
./mvnw -Pnative,musl clean native:compile
```

Build Docker Image with Dockerfile
```bash
docker build -t graalvm-demo-book-native:0.1.0 -f Dockerfile.util.native .
```

Build Docker Image for another platform with Dockerfile
```bash
docker build --platform linux/arm64 -t graalvm-demo-book-native-arm64:0.1.0 -f Dockerfile.util.native .
```

Also see:

* https://www.graalvm.org/latest/guides/
* https://www.graalvm.org/latest/docs/getting-started/container-images/
* https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/
* https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#native-image.developing-your-first-application
* https://graalvm.github.io/native-build-tools/latest/maven-plugin.html

### Further alternative optimizations

* CRaC
  * https://wiki.openjdk.org/display/crac
  * https://bell-sw.com/libericajdk-with-crac/
* CDS: 
  * https://adoptium.net/blog/2022/10/a-short-exploration-of-java-class-pre-initialization/
  * https://docs.spring.io/spring-framework/reference/integration/cds.html
  * https://medium.com/@theboreddev/make-spring-better-with-class-data-sharing-cds-311bfbe128bb
  * https://bell-sw.com/blog/how-to-use-cds-with-spring-boot-applications/
  * https://spring.io/blog/2023/12/04/cds-with-spring-framework-6-1
  * -> CDS allows to preload classes and share them between multiple VMs.
* AOT 
  * https://www.baeldung.com/ahead-of-time-compilation
  * https://www.baeldung.com/spring-6-ahead-of-time-optimizations
  * https://docs.spring.io/spring-framework/reference/core/aot.html 
* JIT
  * https://www.baeldung.com/graal-java-jit-compiler 
  * https://ionutbalosin.com/2024/02/jvm-performance-comparison-for-jdk-21/
* JLink
  * https://adoptium.net/blog/2021/10/jlink-to-produce-own-runtime/
  * https://www.baeldung.com/jlink
* GC
  * https://wiki.openjdk.org/display/shenandoah/Main
  * https://entwickler.de/java/wer-bringt-den-mull-raus
  * https://www.baeldung.com/jvm-epsilon-gc-garbage-collector

Alternative base images for JVM:
* Bellsoft
  * https://bell-sw.com/pages/supported-configurations/
  * https://bell-sw.com/libericajdk-containers/

Further reads:
* https://www.marcobehler.com/guides/graalvm-aot-jit
* https://entwickler.de/java/fast-and-furious-001
* https://spring.io/blog/2023/10/16/runtime-efficiency-with-spring
* https://openjdk.org/projects/leyden/
* https://github.com/openjdk/leyden/tree/premain/test/hotspot/jtreg/premain/javac_new_workflow
* https://www.youtube.com/watch?v=O1Oz2-AXKKM
* https://bell-sw.com/blog/spring-boot-with-graalvm-native-image-performance-compatibility-migration/
* https://ionutbalosin.com/2024/02/jvm-performance-comparison-for-jdk-21/
* https://medium.com/graalvm/welcome-graalvm-for-jdk-22-8a48849f054c

#### CDS

Do multiple training runs:
```bash
java -Dspring.aot.enabled=true -Dspring.context.exit=onRefresh -Dspring.main.lazy-initialization=false -XX:DumpLoadedClassList=/tmp/dist.classlist -server "org.springframework.boot.loader.launch.JarLauncher"
```

Hint: You can do multiple runs and then merge the class list

Create AppCDS archive:
```bash
java -Xshare:dump -XX:SharedClassListFile=/tmp/list.txt -XX:SharedArchiveFile=/tmp/dist.jsa
```

## Run locally

Pull and Build
```bash
docker compose pull --ignore-buildable
docker compose build
```

Start
```bash
docker compose up -d
```

* API endpoint JVM app: http://localhost:8080
* API endpoint Native app: http://localhost:8081

Shutdown
```bash
docker compose down -v
```

## API

```bash
curl -v -XPOST -d'{"id":"978-3-8477-1359-3","title":"Nils Holgerssons wunderbare Reise durch Schweden","author":"Selma Lagerlöf","pageCount":704}' -H'Content-Type: application/json; charset=utf-8' http://localhost:8081/books
```

```bash
curl http://localhost:8081/books/978-3-8477-1359-3
```

```bash
curl http://localhost:8081/books
```

```bash
curl -v -XPOST http://localhost:8081/books/bulk \
-H 'Content-Type: application/json; charset=utf-8' \
--data-binary @- << EOF
[
  {"id":"978-0-345-40946-1","title":"The Demon-Haunted World","author":"Carl Sagan, Ann Druyan","pageCount":480},
  {"id":"978-0-345-53943-4","title":"Cosmos","author":"Carl Sagan","pageCount":432}
]
EOF
```

```bash
curl http://localhost:8081/books/bulk/978-0-345-40946-1,978-0-345-53943-4
```



