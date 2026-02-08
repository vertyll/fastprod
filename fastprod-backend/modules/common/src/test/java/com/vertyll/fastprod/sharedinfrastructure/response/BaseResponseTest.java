package com.vertyll.fastprod.sharedinfrastructure.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

class BaseResponseTest {

    @SuperBuilder
    @NoArgsConstructor
    private static class TestBaseResponse<T> extends BaseResponse<T> {}

    @Test
    void builderShouldSetAllFields() {
        // given
        String testData = "test";
        String testMessage = "message";
        LocalDateTime testTime = LocalDateTime.now(ZoneOffset.UTC);

        // when
        BaseResponse<String> response =
                TestBaseResponse.<String>builder()
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
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        BaseResponse<String> response = TestBaseResponse.<String>builder().timestamp(now).build();

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
