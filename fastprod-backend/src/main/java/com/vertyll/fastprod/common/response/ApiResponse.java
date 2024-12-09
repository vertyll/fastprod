package com.vertyll.fastprod.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ResponseEntity<ApiResponse<T>> buildResponse(T data, String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .build();

        return new ResponseEntity<>(response, status);
    }
}