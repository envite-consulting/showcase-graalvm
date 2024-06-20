# Lecture Utilizer

## Build
The following sections show commands to build. 
All commands are completely independent of each other.

### JVM
Hint: In order to build the Jar locally, JDK 21 or higher is required.
Alternatively, use the Docker image builds, which are independent of your local JDK.

Build Jar
```bash
mvn clean package
```

Build Docker Image with Dockerfile
```bash
docker build -t graalvm-utilizer:0.1.0 -f Dockerfile.util .
```

Build Docker Image for another platform with Dockerfile
```bash
docker build --platform linux/arm64 -t graalvm-utilizer:0.1.0 -f Dockerfile.util .
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
Or without having to build the Image first
```bash
docker compose up --build -d
```

Shutdown
```bash
docker compose down -v
```

## API
### Start Load Test
```bash
curl -X POST http://localhost:8056/load-test \
     -H "Content-Type: application/json" \
     -d '{
            "webClientUrl": "http://graalvm-demo-book-jvm:8080",
            "webClientEndpoint": "/books/bulk",
            "numberOfBooks": 10,
            "numberOfRequests": 5,
            "concurrentUsers": 2
         }'
```

### Parameters Description

**webClientUrl**\
The url of the webclient you want to perform the load-test on.\
Type: String

**webClientEndpoint**\
The endpoint of the webclient you want to perform the load-test on.\
Type: String

**numberOfBooks**\
The number of books to send per request.\
Type: int

**numberOfRequests**\
The number of requests to send.\
Type: int

**concurrentUsers**\
The number of simulated Users.\
Type: int

### Stop Load Test
You can stop the load test like this:
```bash
curl -X POST "http://localhost:8085/load-test/stop"
```

### Get Load Test Status
You can get the status of the Load Test with:
```bash
curl -X GET "http://localhost:8085/load-test/status"
```

## Project Structure
```bash
graalvm-demo-book-utilizer
├── src
│   ├── main
│   │   ├── java/com/example/lectureutilizer
│   │   │   ├── Book.java
│   │   │   ├── LectureUtilizerApplication.java
│   │   │   ├── LoadTestConfig.java
│   │   │   ├── LoadTestController.java
│   │   │   ├── LoadTestService.java
│   │   │   ├── LoadTestStatus.java
│   │   │   └── WebClientConfig.java
│   │   │    
│   │   └── resources
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   │
│   └── test/java/com/example/lectureutilizer
│       └── LectureUtilizerApplicationTests.java
│
├── target
│   ├── ...
│   ├── graalvm-utilizer-VERSION.jar
│   └── application.log
│   
└── pom.xml
```
