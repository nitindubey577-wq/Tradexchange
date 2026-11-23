package com.tradexchange.engine.events;

import com.tradexchange.engine.EngineOrder;

public class OrderCancelledEvent {
    private final EngineOrder order;

    public OrderCancelledEvent(EngineOrder order) {
        this.order = order;
    }

    public EngineOrder getOrder() {
        return order;
    }
}
