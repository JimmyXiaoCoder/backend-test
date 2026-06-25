package com.codetest.common;

public class ResultUtils {

    private ResultUtils() {}

    public static <T> Result<T> success(T data) { return Result.ok(data); }
    public static <T> Result<T> success() { return Result.ok(); }

    public static <T> Result<T> error(ResultCode code) { return Result.fail(code); }
    public static <T> Result<T> error(ResultCode code, String msg) { return Result.fail(code, msg); }
    public static <T> Result<T> error(String msg) { return Result.fail(ResultCode.INTERNAL_ERROR.getCode(), msg); }
    public static <T> Result<T> error() { return Result.fail(ResultCode.INTERNAL_ERROR); }
}
