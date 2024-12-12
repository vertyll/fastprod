package com.vertyll.fastprod.common.response;

import java.time.LocalDateTime;

public interface IResponse<T> {
    T getData();
    String getMessage();
    LocalDateTime getTimestamp();
}