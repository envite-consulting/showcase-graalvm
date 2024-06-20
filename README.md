# Lecture Tutorial GraalVM

Goals of this tutorial:

* Compare GraalVM native image and JVM using a basic SpringBoot application
* Examine differences in size, execution time, and resource consumption
* Determine scenarios where GraalVM native image is more advantageous than JVM and vice versa

## Prerequisites

Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/) on Windows 
or [Docker Engine](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/linux/) on Linux.

Download and install [Python 3.11)](https://www.python.org/downloads/) (or later).

Because the two SpringBoot applications "graalvm-demo-book" and "graalvm-demo-book-utilizer" are built inside of Docker, 
there is no need to install GraalVM or Java locally.

## Project Structure

```bash
lecture-graalvm
├── graalvm-demo-book
│   ├── Main Application
│   └── Insert books into a MongoDB
│
├── graalvm-demo-book-utilizer
│   ├── Utilizer Application
│   └── Create a load-test of the Main Application
│
├── load-test
│   ├── Python Scripts
│   └── Run multiple load-tests and evalute the results
│
└── monitoring
    ├── Grafana and Prometheus config
    └── Preconfigured Grafana dashboard with container monitoring
```

## The Book Demo Application

The [Project Structure](#project-structure) outlines the two key applications: graalvm-demo-book and graalvm-demo-book-utilizer.

The graalvm-demo-book application manages books and provides Rest endpoints to add, delete and get books. 
This is the application we use for the comparison.

The graalvm-demo-book-utilizer application creates multiple load tests for the graalvm-demo-book app.

The components of a book are as follows:

| Name      | Type    | Description            |
|-----------|---------|------------------------|
| id        | String  | Unique ID for the Book |
| title     | String  | The book's title       |
| author    | String  | The book's author      |
| pageCount | Integer | The book's page count  |

A simplified diagram of the main components.

```bash
+-------------------+       +-----------------------+
|                   |       |                       |
| graalvm-demo-book |<------| graalvm-demo-utilizer |
|                   |       |                       | 
|    POST /books    |       |    POST /load-test    |   
|    insert books   |       |    - configure url,   |
|                   |       |       endpoint        |
+---------|---------+       |    - set numBooks,    |
          |                 |       numRequests,    |
          |                 |       numUsers        |
          v                 |                       |
 +-----------------+        +-----------------------+
 |                 |
 |    MongoDB      |
 |   Store books   |
 |                 |
 +-----------------+
```


## 1. Demo Application with JVM

First, we have start with a normal JVM and look how our demo application behaves. 

### 1.1 Build Start JVM demo app

Prepare folder structure (Linux only)
```bash
mkdir target && chmod -R g+rX,o+rX target
chmod -R g+rX,o+rX monitoring
```

Pull all images, e.g. mongo db
```bash
docker compose pull --ignore-buildable
```

Build JVM image:
```bash
docker compose build graalvm-demo-book-jvm
```

Dockerfile: [graalvm-demo-book/Dockerfile.jvm](graalvm-demo-book/Dockerfile.jvm)
* Docker multi-stage build
* SpringBoot layered image

Run app and mongodb
```bash
docker compose up -d graalvm-demo-book-jvm 
```

Show running containers
```bash
docker compose ps
```

View start log and find the startup time of the application
```bash
docker compose logs -f graalvm-demo-book-jvm
```

### 1.2 Play with Book API

On Windows use Git console or another bash like terminal to run the following `curl` commands.
Alternatively you can install VS Code Plugin "Rest client" or ItelliJ's "Services" und run the http requests via [requests.http](requests.http)

Create a new book
```bash
time curl -v -XPOST -d'{"id":"978-3-8477-1359-3","title":"Nils Holgerssons wunderbare Reise durch Schweden","author":"Selma Lagerlöf","pageCount":704}' -H'Content-Type: application/json; charset=utf-8' http://localhost:8080/books
```
How long did the first request take?


If you want, you can play a little bit with the API.

```bash
curl http://localhost:8080/books/978-3-8477-1359-3
```

```bash
curl http://localhost:8080/books
```

```bash
curl -v -XPOST http://localhost:8080/books/bulk \
-H 'Content-Type: application/json; charset=utf-8' \
--data-binary @- << EOF
[
  {"id":"978-0-345-40946-1","title":"The Demon-Haunted World","author":"Carl Sagan, Ann Druyan","pageCount":480},
  {"id":"978-0-345-53943-4","title":"Cosmos","author":"Carl Sagan","pageCount":432}
]
EOF
```

```bash
curl http://localhost:8080/books/bulk/978-0-345-40946-1,978-0-345-53943-4
```

### 1.3 Discussion

What did you discover? Is all good or do you think there are some issues especially related to Cloud?

## 2. GraalVM Native Image

What is GraalVM?

* AOT: Ahead-of-time compilation
* Low Memory Footprint
* Self-contained executables
* Reduced startup time

--> How does it solve our problem? Compilation to native binary moves stuff from runtime to compile time.

### 2.1 Build Start demo app build with GraalVM native image

Build native image:
```bash
docker compose build graalvm-demo-book-native
```

Dockerfile: [graalvm-demo-book/Dockerfile.native](graalvm-demo-book/Dockerfile.native)
* Docker multi-stage build
* ./mvnw -Pnative,musl native:compile # what happens here?

Run app and mongodb
```bash
docker compose up -d graalvm-demo-book-native 
```

Show running containers
```bash
docker compose ps
```

View start log and find the startup time of the application
```bash
docker compose logs -f graalvm-demo-book-native
```

### 2.2 Execute Request against Book API

Create a new book
```bash
time curl -v -XPOST -d'{"id":"978-3-8477-1359-3","title":"Nils Holgerssons wunderbare Reise durch Schweden","author":"Selma Lagerlöf","pageCount":704}' -H'Content-Type: application/json; charset=utf-8' http://localhost:8084/books
```
How long did the first request take?

```bash
curl http://localhost:8084/books/978-3-8477-1359-3
```

### 2.3 Discussion

What did you discover? What is better now?


## 4. Is GraalVM really always better?

### 4.1 Docker Image Layers

[Dive](https://github.com/wagoodman/dive) is a tool for exploring a docker image and layer contents.

Let's use it to analyze our two Docker images.

JVM:
```bash
docker run --rm -it \
    -v /var/run/docker.sock:/var/run/docker.sock \
    wagoodman/dive:latest graalvm-demo-book-jvm:0.1.0
```

Native:
```bash
docker run --rm -it \
    -v /var/run/docker.sock:/var/run/docker.sock \
    wagoodman/dive:latest graalvm-demo-book-native:0.1.0
```

Which differences do you see? Do you see problems, which one is better?


### 4.2 Analysis of resource consumption and behaviour over long run

#### Stop Application

Stop all running containers
```bash
docker compose down -v
```

#### Start Monitoring

```bash
docker compose up -d prometheus cadvisor grafana
```

Open Grafana: http://localhost:3000/d/edonzk2655t6oc/lecture-graalvm

Start the two containers again
```bash
docker compose up -d graalvm-demo-book-jvm graalvm-demo-book-native
```

Which differences do you see on the Grafana Dashboard?


#### Run utilizer

On Windows use Git console or another bash like terminal to run the following `curl` commands.
Alternatively you can install VS Code Plugin "Rest client" or ItelliJ's "Services" und run the http requests via [requests_load-test.http](requests_load-test.http).

These are the parameters of graalvm-demo-book-utilizer

| Name              | Type    | Description                                                     |
|-------------------|---------|-----------------------------------------------------------------|
| webClientUrl      | String  | The URL of the service to be load-tested                        |
| webClientEndpoint | String  | The endpoint of the service                                     |
| numberOfBooks     | Integer | The number of books                                             |
| numberOfRequests  | Integer | The number of requests                                          |
| concurrentUsers   | Integer | The number of simulated concurrent users via parallel threading |

Each simulated user (via parallel threading) will send the specified number of requests (numberOfRequests).
```bash
User
├── Request-1
│   └── numberOfBooks books
│    
├── Request-2
│   └── numberOfBooks books
...
└── Request-n
```
The total number of books that are written to the mongoDB are: concurrentUsers * numberOfRequests * numberOfBooks.


Build the utilizer:
```bash
docker compose build graalvm-demo-book-utilizer
```

Start the utilizer
```bash
docker compose up -d graalvm-demo-book-utilizer
```

Run a simple load-test on graalvm-demo-book-jvm
```bash
curl -v -XPOST http://localhost:8085/load-test \
-H 'Content-Type: application/json; charset=utf-8' \
--data-binary @- << EOF
{
    "webClientUrl": "http://graalvm-demo-book-jvm:8080",
    "webClientEndpoint": "/books/bulk",
    "numberOfBooks": 20,
    "numberOfRequests": 10,
    "concurrentUsers": 1
}
EOF
```

Run the same load-test on graalvm-demo-book-native
```bash
curl -v -XPOST http://localhost:8085/load-test \
-H 'Content-Type: application/json; charset=utf-8' \
--data-binary @- << EOF
{
    "webClientUrl": "http://graalvm-demo-book-native:8080",
    "webClientEndpoint": "/books/bulk",
    "numberOfBooks": 20,
    "numberOfRequests": 10,
    "concurrentUsers": 1
}
EOF
```

Try changing the parameters and compare the results.

Which differences do you see?

#### Run python load-test

Create Python environment and install requirements (Windows)
```bash
python -m 'venv' .venv
.venv/Scripts/activate
pip install -r ./load-test/requirements.txt
```

Create Python environment and install requirements (Linux)
```bash
python -m 'venv' .venv
source .venv/bin/activate
pip install -r ./load-test/requirements.txt
```

Before running the load test, we should restart the containers. (Why?)
```bash
docker compose down -v graalvm-demo-book-jvm graalvm-demo-book-native prometheus
docker compose up -d prometheus
```

Wait some seconds and then start the book apps:
```bash
docker compose up -d graalvm-demo-book-jvm graalvm-demo-book-native
```

Hint: On Linux, if you have a CPU with performance and efficience cores, you can pin the containers to a specific CPU-set in the [compose.yaml](compose.yaml) file.
For example if you have a Intel CPU with 4 performance cores and you want to ensure that the load test runs on them, set cpuset to "0-7".
```yaml
graalvm-demo-book-jvm:
    container_name: graalvm-demo-book-jvm
    image: graalvm-demo-book-jvm:0.1.0
    cpuset: "0-7"
```


Run load-test
```bash
python ./load-test/send_requests.py --urls "jvm,native" --runs 150
```

Evaluate the load-test
```bash
python ./load-test/requests_eval.py
```

Which differences do you see? During and after the load-test?

For which kind of workload would you use which variant?

### 4.3 Further Discussion

* JVM vs. GraalVM native: advantages and disadvantages
* In relation to Cloud Computing?

Additional Topics:
* JVM Optimizatios: Jlink, CDS, AOT
* GraalVM AOT, JIT
