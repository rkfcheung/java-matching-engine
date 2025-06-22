package com.rkfcheung.trading.common;

public sealed interface Result permits Result.Ok, Result.Err {

    default boolean isOk() {
        return this instanceof Ok;
    }

    default boolean isErr() {
        return this instanceof Err;
    }

    record Ok<T>(T value) implements Result {
    }

    record Err<E>(E error) implements Result {
    }
}
