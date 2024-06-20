package de.envite.lecture.graalvm.utilizer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;

    public LoadTestController (LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @PostMapping
    public ResponseEntity<String> runLoadTest(@RequestBody LoadTestConfig config) {
        long startTime = System.currentTimeMillis();

        loadTestService.runLoadTest(config);

        int totalBookSent = config.getConcurrentUsers() * config.getNumberOfRequests() * config.getNumberOfBooks();
        long endTime = System.currentTimeMillis();
        String result = "Time taken: " + (endTime - startTime) + "ms, Books sent: " + totalBookSent;
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopLoadTest() {
        loadTestService.stopLoadTest();
        return ResponseEntity.ok("Load test stopped.");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getLoadTestStatus() {
        return ResponseEntity.ok(loadTestService.getLoadTestStatus());
    }

}
