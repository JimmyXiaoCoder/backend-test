package com.codetest.common;

public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    PARAM_MISSING(410, "缺少必要参数"),
    PARAM_INVALID(411, "参数格式不合法"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_EXISTED(1002, "用户已存在"),
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    CART_ITEM_NOT_FOUND(2002, "购物车商品不存在"),
    STOCK_NOT_ENOUGH(2003, "库存不足"),
    FEIGN_CALL_FAILED(3001, "服务调用失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
