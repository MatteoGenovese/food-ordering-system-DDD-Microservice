package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase>
        implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<SendResult<K, V>> send(String topicName, K key, V message) {
        log.info("Sending message={} to topic={}", message, topicName);

        try {
            CompletableFuture<SendResult<K, V>> future = kafkaTemplate.send(topicName, key, message);

            // Optional logging + error wrapping
            return future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                            "Error on kafka producer with key: {}, message: {} and exception: {}",
                            key, message, ex.getMessage(), ex
                    );
                    throw new KafkaProducerException(
                            "Error on kafka producer with key: " + key + " and message: " + message,
                            ex
                    );
                } else {
                    log.info(
                            "Message sent to topic={}, partition={}, offset={}",
                            topicName,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                }
            });

        } catch (KafkaException e) {
            log.error(
                    "KafkaException on kafka producer with key: {}, message: {} and exception: {}",
                    key, message, e.getMessage(), e
            );
            throw new KafkaProducerException(
                    "KafkaException on kafka producer with key: " + key + " and message: " + message,
                    e
            );
        }
    }

    @PreDestroy
    public void close() {
        log.info("Closing kafka producer!");
        kafkaTemplate.destroy();
    }
}
