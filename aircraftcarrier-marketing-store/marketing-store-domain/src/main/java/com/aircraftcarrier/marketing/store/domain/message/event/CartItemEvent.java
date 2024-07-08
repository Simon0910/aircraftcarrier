package com.aircraftcarrier.marketing.store.domain.message.event;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class CartItemEvent implements Serializable {
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    // orderId
    private String orderId;

    private String itemId;
    private int quantity;

    public CartItemEvent() {

    }

    public CartItemEvent(String orderId, String itemId, int quantity) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItemEvent{" + "itemId='" + itemId + '\'' + ", quantity=" + quantity + '}';
    }
}
