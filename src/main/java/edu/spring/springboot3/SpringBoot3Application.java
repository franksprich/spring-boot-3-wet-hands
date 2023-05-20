package edu.spring.springboot3;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@SpringBootApplication
public class SpringBoot3Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3Application.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener(CustomerRepository repository) {
        return event -> repository.findAll().forEach(System.out::println);
    }

}

@RestController
@ResponseBody
class CustomerHttpController {

    private final CustomerRepository repository;
    private final ObservationRegistry registry;

    CustomerHttpController(CustomerRepository repository, ObservationRegistry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    @GetMapping("/customer")
    Iterable<Customer> allCustomer() {
        return this.repository.findAll();
    }

    @GetMapping("/customer/{name}")
    Iterable<Customer> byName(@PathVariable String name) {
        Assert.state(Character.isUpperCase(name.charAt(0)), "the name must be upper case!");
//        return this.repository.findByName(name);

        // Observability with Spring Boot 3
        // Reference to add metrics
        // USE programmatic approach:
        // https://youtu.be/4QtW1KVZJRI?t=1217
        // https://micrometer.io/
        // http://localhost:8080/actuator/metrics
        // http://localhost:8080/actuator/metrics/by-name
        // OR
        // USE Aspects and Annotations like: https://www.baeldung.com/spring-boot-3-observability
        return Observation
                .createNotStarted("by-name", this.registry)
                .observe(() -> repository.findByName(name));
    }
}

record Customer(@Id Integer id, String name) {
}

interface CustomerRepository extends CrudRepository<Customer, Integer> {
    Iterable<Customer> findByName(String name);
}

/**
 * {@code @Link:} <a href="https://www.rfc-editor.org/rfc/rfc7807">Problem Details for HTTP APIs</a>
 */
@ControllerAdvice
class ErrorHandlingControllerAdvice {

    @ExceptionHandler
    public ProblemDetail handle(IllegalStateException isa, HttpServletRequest request) {
        request.getHeaderNames().asIterator().forEachRemaining(System.out::println);
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        pd.setDetail(isa.getMessage());
        return pd;
    }

}
