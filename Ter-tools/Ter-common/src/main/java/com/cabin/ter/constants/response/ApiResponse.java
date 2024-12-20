package com.cabin.ter.constants.response;


import com.cabin.ter.constants.enums.IStatus;
import com.cabin.ter.constants.enums.Status;
import com.cabin.ter.exception.BaseException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;


/**
 * <p>
 * 通用的 API 接口封装
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-04-19 16:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "通用Api状态返回",description = "Common Api Response")
public class ApiResponse implements Serializable {
    private static final long serialVersionUID = 8993485788201922830L;

    /**
     * 状态码
     */
    @Schema(name = "status",description = "通用返回状态码")
    private Integer status;

    /**
     * 返回内容
     */
    @Schema(name = "message",description = "通用返回信息")
    private String message;

    /**
     * 返回数据
     */
    @Schema(name = "data",description = "通用返回数据")
    private Object data;

    /**
     * 构造一个自定义的API返回
     *
     * @param status    状态码
     * @param message 返回内容
     * @param data    返回数据
     * @return ApiResponse
     */
    public static ApiResponse of(Integer status, String message, Object data) {
        return new ApiResponse(status, message, data);
    }

    /**
     * 构造一个成功且不带数据的API返回
     *
     * @return ApiResponse
     */
    public static ApiResponse ofSuccess() {
        return ofSuccess(null);
    }

    /**
     * 构造一个成功且带数据的API返回
     *
     * @param data 返回数据
     * @return ApiResponse
     */
    public static ApiResponse ofSuccess(Object data) {
        return ofStatus(Status.SUCCESS, data);
    }


    /**
     * 构造一个成功且自定义消息的API返回
     *
     * @param message 返回内容
     * @return ApiResponse
     */
    public static ApiResponse ofMessage(String message) {
        return of(Status.SUCCESS.getStatus(), message, null);
    }

    /**
     * 构造一个有状态的API返回
     *
     * @param status 状态 {@link Status}
     * @return ApiResponse
     */
    public static ApiResponse ofStatus(Status status) {
        return ofStatus(status, null);
    }

    /**
     * 构造一个有状态且带数据的API返回
     *
     * @param status 状态 {@link IStatus}
     * @param data   返回数据
     * @return ApiResponse
     */
    public static ApiResponse ofStatus(IStatus status, Object data) {
        return of(status.getStatus(), status.getMessage(), data);
    }

    /**
     * 构造一个异常的API返回
     *
     * @param t   异常
     * @param <T> {@link BaseException} 的子类
     * @return ApiResponse
     */
    public static <T extends BaseException> ApiResponse ofException(T t) {
        return of(t.getCode(), t.getMessage(), t.getData());
    }
}

