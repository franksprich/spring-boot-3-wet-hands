# Spring Boot 3

## Use Native GraalVM Image
The project is based on a talk of [Josh Long](https://youtu.be/4QtW1KVZJRI?t=2016)

### Build
1. An instance of GraalVM must be present on the system (Ubuntu 22.04)
    1. Use [SDKMAN](https://sdkman.io/install) to install [GraalVM](https://www.graalvm.org/22.1/release-notes/22_2/): 
       1. Terminal: `sdk list java`
       2. Terminal: `sdk install java 22.3.r19-grl` which install the SDK and make it the default
    2. Add it to Intellij's SDKs
2. Build the native image with Gradle:
   1. Terminal: `./gradlew nativeCompile`

### Execute
There are three ways to execute the previously built image:
1. Navigate to `build/native/nativeCompile` and execute native image `./spring-boot-3`
2. Add a new `Run Configuration` to execute the shell script `spring-boot-3`. **ATTENTION**: Let the `Interpreter path` empty.
3. `Ctrl+Alt+S` -> `Tools` -> `External Tools`


## Observability
**PRECONDITION**: The `Spring Actuator` dependency must be added and activated in `application.yaml`  
Observability is based on the project: [Micrometer](https://micrometer.io/)

There are two ways to add Observability and Metrics to an REST API:
1. Programmatically added like: 
    ```java
        @GetMapping("/customer/{name}")
        Iterable<Customer> byName(@PathVariable String name) {
            ...
            return Observation
                    .createNotStarted("by-name", this.registry)
                    .observe(() -> repository.findByName(name));
    ```
    
2. Use Annotation to add Observability see: [Baeldung: Observability with Spring Boot 3](https://www.baeldung.com/spring-boot-3-observability)

For this example one can find the metrics under `http://localhost:8080/actuator/metrics/by-name`
