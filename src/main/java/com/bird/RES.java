package com.bird;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RES<T> {

    private int code;
    private T data;
    private String message;

    public RES(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public RES(int code, String message) {
        this(code, null, message);
    }

    public static <T> RES<T> of(int code, T data, String message) {
        return new RES<T>(code, data, message);
    }

    public static <T> RES<T> of(int code, String message) {
        return new RES<T>(code, message);
    }
}
