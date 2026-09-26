package com.vertyll.fastprod.sharedinfrastructure.entity;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseEntityTest {

    private static final class TestEntity extends BaseEntity {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @Test
    void class_ShouldHaveRequiredAnnotations() {
        // given
        Class<?> clazz = BaseEntity.class;

        // then
        assertTrue(clazz.isAnnotationPresent(MappedSuperclass.class));
        assertTrue(clazz.isAnnotationPresent(EntityListeners.class));
        EntityListeners entityListeners = clazz.getAnnotation(EntityListeners.class);
        assertArrayEquals(
            new Class[] {
                AuditingEntityListener.class
            },
            entityListeners.value()
        );
    }

    @Test
    void auditFields_ShouldBeReadable() {
        // given
        TestEntity entity = new TestEntity();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String user = "testUser";

        // when
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
        ReflectionTestUtils.setField(entity, "createdBy", user);
        ReflectionTestUtils.setField(entity, "updatedBy", user);

        // then
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals(user, entity.getCreatedBy());
        assertEquals(user, entity.getUpdatedBy());
    }

    @Test
    void id_ShouldBeReadable() {
        // given
        TestEntity entity = new TestEntity();
        Long id = 1L;

        // when
        ReflectionTestUtils.setField(entity, "id", id);

        // then
        assertEquals(id, entity.getId());
    }
}
