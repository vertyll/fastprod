package com.vertyll.fastprod.common.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

class BaseEntityTest {

    private static final class TestEntity extends BaseEntity {
        @Serial private static final long serialVersionUID = 1L;
    }

    @Test
    void class_ShouldHaveRequiredAnnotations() {
        // given
        Class<?> clazz = BaseEntity.class;

        // then
        assertTrue(clazz.isAnnotationPresent(MappedSuperclass.class));
        assertTrue(clazz.isAnnotationPresent(EntityListeners.class));
        EntityListeners entityListeners = clazz.getAnnotation(EntityListeners.class);
        assertArrayEquals(new Class[] {AuditingEntityListener.class}, entityListeners.value());
    }

    @Test
    void auditFields_ShouldBeSettableAndGettable() {
        // given
        TestEntity entity = new TestEntity();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String user = "testUser";

        // when
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(user);
        entity.setUpdatedBy(user);

        // then
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals(user, entity.getCreatedBy());
        assertEquals(user, entity.getUpdatedBy());
    }

    @Test
    void id_ShouldBeSettableAndGettable() {
        // given
        TestEntity entity = new TestEntity();
        Long id = 1L;

        // when
        entity.setId(id);

        // then
        assertEquals(id, entity.getId());
    }
}
