package com.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class OrderKafkaMessageHelper {

    public <T> CompletableFuture<SendResult<String, T>> addLoggingCallbacks(
            CompletableFuture<SendResult<String, T>> future,
            String responseTopicName,
            T requestAvroModel,
            String orderId,
            String requestAvroModelName
    ) {
        return future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Error while sending {} message {} to topic {}",
                        requestAvroModelName,
                        String.valueOf(requestAvroModel),
                        responseTopicName,
                        ex
                );
                return;
            }

            RecordMetadata metadata = result.getRecordMetadata();
            log.info(
                    "Received successful response from Kafka for order id: {} Topic: {} Partition: {} Offset: {} Timestamp: {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp()
            );
        });
    }

}
