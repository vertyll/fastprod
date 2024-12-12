package com.vertyll.fastprod.common.response;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class IResponseTest {

    private static class TestResponse<T> implements IResponse<T> {
        private final T data;
        private final String message;
        private final LocalDateTime timestamp;

        TestResponse(T data, String message, LocalDateTime timestamp) {
            this.data = data;
            this.message = message;
            this.timestamp = timestamp;
        }

        @Override public T getData() { return data; }
        @Override public String getMessage() { return message; }
        @Override public LocalDateTime getTimestamp() { return timestamp; }
    }

    @Test
    void shouldImplementInterfaceCorrectly() {
        // given
        String testData = "test";
        String testMessage = "message";
        LocalDateTime testTime = LocalDateTime.now();

        // when
        IResponse<String> response = new TestResponse<>(testData, testMessage, testTime);

        // then
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertEquals(testTime, response.getTimestamp());
    }
}