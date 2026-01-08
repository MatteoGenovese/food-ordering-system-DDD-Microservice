package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderTrackCommandHandler {

    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderTrackCommandHandler(
            OrderDataMapper orderDataMapper,
            OrderRepository orderRepository
    ) {
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> optionalOrder = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if (optionalOrder.isEmpty()) {
            log.warn("Order Tracking not found with id {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not find order tracking with id " + trackOrderQuery.getOrderTrackingId());
        }
        return orderDataMapper.trackOrderToTrackOrderResponse(optionalOrder.get());
    }

}
