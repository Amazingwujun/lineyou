package com.jun.lineyou.exception;

/**
 * @author Jun
 * @date 2020-07-01 14:19
 */
public class HttpException extends RuntimeException {

    public HttpException(String message) {
        super(message);
    }

    public HttpException() {
        super();
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }
}
