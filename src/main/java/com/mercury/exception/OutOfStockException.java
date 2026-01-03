package com.mercury.exception;

import java.math.BigInteger;

public class OutOfStockException extends RuntimeException {
    private final BigInteger itemId;
    private final int requestedQty;

    public OutOfStockException(BigInteger itemId, int requestedQty) {
        super("Out of stock for itemId=" + itemId + ", requestedQty=" + requestedQty);
        this.itemId = itemId;
        this.requestedQty = requestedQty;
    }

    public BigInteger getItemId() { return itemId; }
    public int getRequestedQty() { return requestedQty; }
}