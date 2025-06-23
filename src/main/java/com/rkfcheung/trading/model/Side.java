package com.rkfcheung.trading.model;

public enum Side {
    BID, ASK;

    public Side flip() {
        return switch (this) {
            case BID -> ASK;
            case ASK -> BID;
        };
    }
}
