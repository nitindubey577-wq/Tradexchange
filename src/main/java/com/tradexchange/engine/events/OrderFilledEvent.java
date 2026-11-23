package com.tradexchange.engine.events;

import com.tradexchange.engine.EngineOrder;

public class OrderFilledEvent {
    private final EngineOrder order;

    public OrderFilledEvent(EngineOrder order) {
        this.order = order;
    }

    public EngineOrder getOrder() {
        return order;
    }
}
