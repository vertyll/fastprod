package com.vertyll.fastprod.common.response;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BaseResponseTest {

    @SuperBuilder
    @NoArgsConstructor
    private static class TestBaseResponse<T> extends BaseResponse<T> {
    }

    @Test
    void builderShouldSetAllFields() {
        // given
        String testData = "test";
        String testMessage = "message";
        LocalDateTime testTime = LocalDateTime.now();

        // when
        BaseResponse<String> response = TestBaseResponse.<String>builder()
                .data(testData)
                .message(testMessage)
                .timestamp(testTime)
                .build();

        // then
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertEquals(testTime, response.getTimestamp());
    }

    @Test
    void shouldCreateEmptyResponse() {
        // when
        LocalDateTime now = LocalDateTime.now();
        BaseResponse<String> response = TestBaseResponse.<String>builder()
                .timestamp(now)
                .build();

        // then
        assertNull(response.getData());
        assertNull(response.getMessage());
        assertEquals(now, response.getTimestamp());
    }

    @Test
    void noArgsConstructorShouldSetDefaultTimestamp() {
        // when
        TestBaseResponse<String> response = new TestBaseResponse<>();

        // then
        assertNull(response.getData());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }
}