package com.vertyll.fastprod.role;

import com.vertyll.fastprod.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
public class Role extends BaseEntity {

    @Getter
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

}