package de.envite.lecture.graalvm.utilizer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class LoadTestConfig {
    // Getter and Setter
    private String webClientUrl;
    private String webClientEndpoint;
    private int numberOfBooks;
    private int numberOfRequests;
    private int concurrentUsers;
}
