package com.tradexchange.engine.events;

import com.tradexchange.engine.EngineOrder;

public class OrderPartiallyFilledEvent {
    private final EngineOrder order;

    public OrderPartiallyFilledEvent(EngineOrder order) {
        this.order = order;
    }

    public EngineOrder getOrder() {
        return order;
    }
}
