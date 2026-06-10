package com.tenny.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<T>().setCode(0).setMessage("success").setData(data);
    }

    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<T>().setCode(code).setMessage(message);
    }

    public static ApiResult<Void> success() {
        return new ApiResult<Void>().setCode(0).setMessage("success");
    }
}
