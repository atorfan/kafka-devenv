package org.atorfan.consumer.kafka;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class ConsumerWithKafkaShould {

    private static final long SECONDS_FOR_RETRY_UNTIL_KAFKA_IS_READY = 20;
    private static final long SECONDS_FOR_RETRY_UNTIL_OTHER_COST_IS_PERSISTED = 5;

    private static final long WAIT_FOR_RETRY_MILLISECONDS = 200;

    private static final int NEVER = 0;
    private static final int ONE_TIME = 1;

    @Container
    public static ComposeContainer environment = initializeKafkaContainer();

    private static ComposeContainer initializeKafkaContainer() {
        var kafkaContainer = new ComposeContainer(
                new File("../../docker/docker-compose.yml")
                )
            .withLocalCompose(true);
//        kafkaContainer.start();

//        System.setProperty("spring.embedded.kafka.brokers", "PLAINTEXT://localhost:9092");

        return kafkaContainer;
    }

    @Value(value = "testing.kafka-env")
    private String topic;

    @Autowired
    private KafkaTemplate<UUID, Object> kafkaProducerTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerRegistry;

    @SpyBean
    private UseCaseService useCaseService;

    @AfterEach
    void cleanup() {
//        kafkaListenerRegistry.getListenerContainers().forEach(Lifecycle::start);
    }

    @Test
    void givenMessageWhenPublishedInTopicThenIsConsumedAndUseCaseIsCalled() throws Exception {
        final UUID id = UUID.randomUUID();

//        retryUntilKafkaIsReady(() ->
        kafkaProducerTemplate.send(
            topic,
            id,
            new Object()
//            )
        );

//        assertSaveErrorHandlerIsCalled(NEVER);
        assertUseCaseIsCalled(id);
    }

    private void assertUseCaseIsCalled(UUID id) {
        await()
            .atMost(1, SECONDS)
            .untilAsserted(() -> verify(useCaseService).execute(eq(id), any())
        );
    }

    private void retryUntilKafkaIsReady(Runnable action) throws Exception {
        retryUntilActionWorks(
            action,
            SECONDS_FOR_RETRY_UNTIL_KAFKA_IS_READY,
            "Kafka broker not ready after %s seconds !!!".formatted(SECONDS_FOR_RETRY_UNTIL_KAFKA_IS_READY)
        );
    }

    private void retryUntilActionWorks(Runnable action, long seconds, String errorMessage) throws Exception {
        long milliseconds = seconds * 1000;
        do {
            try {
                action.run();
                return;
            } catch (Exception ignored) {}
            Thread.sleep(WAIT_FOR_RETRY_MILLISECONDS);

        } while ((milliseconds -= WAIT_FOR_RETRY_MILLISECONDS) > 0);

        throw new RuntimeException(errorMessage);
    }
}
