package com.pwdmgr.exception;

import com.pwdmgr.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author zhongge
 * @since 2026-06-10
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 结果码
     */
    private final ResultCode resultCode;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.resultCode = ResultCode.FAIL;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.resultCode = ResultCode.FAIL;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
    }
}