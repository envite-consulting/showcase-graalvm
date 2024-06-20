# Considerations and Findings

## Optimization Goals

* Designtime
    * Build duration
* Runtime
    * Startup duration
        * Image fetch duration
        * Application startup
    * Performance
    * Memory footprint

Those goals cannot always be achieved at the same time. For example, reduction of duration of startup may impact overall performance of the application.

Therefore, we have to optimize depending on the use case, e.g. "short-lived (FaaS)" vs. "long-lived".

## Cloud Requirements

For serverless services, you typically have to define a fixed CPU and Memory allocation (sometimes you just define memory, however, implicitly this also assigns CPU).
The consequence is, that you cannot allow your application to burst in the beginning (get more CPU and memory in the beginning for fast startup).
You pay for the entire lifespan for the Memory and CPU you have allocated. So, therefore it is very important for serverless workloads, that the CPU and memory requirements somehow the same over the entire lifespan.
If you need more, you should scale up horizontally. 

This is especially important for FaaS deployment, where for new requests a new function is spawned. You need millisecond startup time and not multiple seconds.
But also for long-lived container workloads, fast startup (meaning not much more CPU and memory requirements to start) can be important to react quick to an increase in load.

## Image Size

The native Docker image is smaller than the JVM Docker image. However, the JVM Docker image can be layered which provides important benefits.

This means, that if we change one line of code, we have to redistribute the entire native Image (101 MB).
For the layered JVM image, we have only to redistribute the layer with the actual application, which is 20 kb.

In addition to redistribution als sharing of layers between multiple applications is important.
If multiple Docker images all use the same JRE layer, it will be downloaded only once.

The Image which has been optimized with JLink has basically the same size as the native image, but it has the important advantage, that it still is layered.
However, it is not as good as the image with the standard VM, because JVM layer contains an optimized JVM with fits the needs of the application.
What we could do is to generate a optimized JRE which fits for a set of applications and create our own base image.

Open Question: Does GraalVM also supports layered native image by make use of shared libraries?
There is an open issue for this: https://github.com/oracle/graal/issues/7626
However, this may be difficult, because GraalVM also optimizes the entire binary, which is not possible if we want to re-use parts of the compiled JVM.

JVM Image:
* ...
* 165 MB RUN ..... # install JRE
* ...
*  31 MB COPY --chown=appuser:appuser /usr/src/lecture-graalvm/dependencies/ ./
* ...
*  20 kB COPY --chown=appuser:appuser /usr/src/lecture-graalvm/application/ ./

JVM Jlink Optimized Image
* ...
* 63 MB COPY /usr/src/lecture-graalvm/target/jre /opt/java/openjdk # copy optimized JRE
* ...
* 31 MB COPY --chown=appuser:appuser /usr/src/lecture-graalvm/dependencies/ ./
* ...
* 20 kB COPY --chown=appuser:appuser /usr/src/lecture-graalvm/application/ ./

Native Image:
* 101 MB  COPY --chown=1000:1000 --chmod=555 /usr/src/lecture-graalvm/target/graalvm-demo-book

## Startup

The startup duration of the native image is much faster than of the JVM and JVM (Jlink) image.

This is especially important for FaaS deployment, where for new requests a new function is spawned.

However, short startup times are also helpful to reduce the CPU requirements during startup.
To reduce startup time, often the CPU requests are increased. The consequence is, that during runtime, this is often not required anymore.
A good alternative is to only increase the limits in order to allow the application to burst in the beginning.
However, this is typically not possible for serverless deployments (FaaS and CaaS). There, most often a fixed sizing must be specified.
So, summarized for serverless use cases (short- and long-running), a fast startup is important.

## Buildtime

ToDo

## JIT vs AOT

ToDo

## Challenges with Reflection

https://www.marcobehler.com/guides/graalvm-aot-jit

## GraalVM Community vs GraalVM Oracle

ToDO

## GraalVM Native Optimizations

Which kind of configurations are available for building a native image? e.g. which GC should be used, ...?

