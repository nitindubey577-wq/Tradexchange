package com.tradexchange.engine.events;

import com.tradexchange.engine.EngineOrder;

public class OrderPlacedEvent {
    private final EngineOrder order;

    public OrderPlacedEvent(EngineOrder order) {
        this.order = order;
    }

    public EngineOrder getOrder() {
        return order;
    }
}
