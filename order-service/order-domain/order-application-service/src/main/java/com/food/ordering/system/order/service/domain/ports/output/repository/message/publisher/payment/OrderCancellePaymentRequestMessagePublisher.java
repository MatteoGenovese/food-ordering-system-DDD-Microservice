package com.food.ordering.system.order.service.domain.ports.output.repository.message.publisher.payment;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;

public interface OrderCancellePaymentRequestMessagePublisher extends DomainEventPublisher<OrderCancelledEvent> {

}
