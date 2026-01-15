package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.repository.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class CancelOrderKafkaMessagePublisher implements
        OrderCancelledPaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderKafkaMessageHelper orderKafkaMessageHelper;

    public CancelOrderKafkaMessagePublisher(
            OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
            OrderKafkaMessageHelper orderKafkaMessageHelper
    ) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    @Override
    public void publish(OrderCancelledEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received OrderCancelledEvent for orderId={}", orderId);

        try {
            PaymentRequestAvroModel paymentRequestAvroModel =
                    orderMessagingDataMapper.orderCancelledEventToPaymentRequestAvroModel(domainEvent);

            CompletableFuture<SendResult<String, PaymentRequestAvroModel>>
                    future =
                    kafkaProducer.send(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            orderId,
                            paymentRequestAvroModel
                    );

            orderKafkaMessageHelper.addLoggingCallbacks(
                    future,
                    orderServiceConfigData.getPaymentResponseTopicName(),
                    paymentRequestAvroModel,
                    orderId,
                    "PaymentRequestAvroModel"
            );

            log.info("PaymentRequestAvroModel send triggered to Kafka for orderId={}",
                     paymentRequestAvroModel.getOrderId());

        } catch (Exception e) {
            log.error("Error while processing OrderCancelledEvent for orderId={}", orderId, e);
        }
    }

}
