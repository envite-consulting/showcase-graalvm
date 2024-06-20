package de.envite.lecture.graalvm.utilizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.core.scheduler.Scheduler;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.Date;
import java.util.List;

@Service
public class LoadTestService {

    private static final Logger logger = LoggerFactory.getLogger(LoadTestService.class);
    private final WebClient webClient;
    private final LoadTestStatus loadTestStatus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
    private ThreadPoolTaskExecutor taskExecutor;

    public LoadTestService (WebClient webClient, LoadTestStatus loadTestStatus) {
        this.webClient = webClient;
        this.loadTestStatus = loadTestStatus;
    }

    private void initThreadPool(LoadTestConfig config) {
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(config.getConcurrentUsers());
        taskExecutor.setMaxPoolSize(config.getConcurrentUsers());
        taskExecutor.setQueueCapacity(1000);
        taskExecutor.setThreadNamePrefix("custom-");
        taskExecutor.initialize();
    }

    private String createLoadTest(LoadTestConfig config) {

        List<Book> books = IntStream.range(0, config.getNumberOfBooks())
                .mapToObj(i -> new Book(null, "Title " + i, "Author " + i, 100 + i))
                .toList();

        return webClient.post()
                .uri(config.getWebClientUrl() + config.getWebClientEndpoint())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(books)
                .retrieve()
                .toBodilessEntity()
                .map(responseEntity -> Objects.requireNonNull(responseEntity.getHeaders().get("Location")).getFirst())
                .doOnNext(response -> {
                    logger.info("Request sent at {}", dateFormat.format(new Date(System.currentTimeMillis())));
                })
                .doOnError(Exception.class, e -> {
                    logger.error("Error response", e);
                })
                .block();
    }

    public void runLoadTest(LoadTestConfig config) {
        loadTestStatus.setStatus(true);
        initThreadPool(config);
        Scheduler schedulerParallel = Schedulers.fromExecutor(taskExecutor);

        Flux.range(1, config.getConcurrentUsers() * config.getNumberOfRequests())
                .parallel(config.getConcurrentUsers())
                .runOn(schedulerParallel)
                .map(requestId -> Map.entry(requestId, createLoadTest(config)))
                //.doOnNext(requestId -> logger.info("Request ID: {}", requestId.getKey()))
                .sequential()
                .blockLast();

        logger.info("Load Test finished successfully.");
        taskExecutor.shutdown();
        loadTestStatus.setStatus(false);
    }

    public void stopLoadTest() {
        loadTestStatus.setStatus(false);
    }

    public String getLoadTestStatus() {
        LoadTestStatus status = new LoadTestStatus();
        return status.isRunning() ?
                "The Load Test is running.":
                "The Load Test is not running.";
    }
}
