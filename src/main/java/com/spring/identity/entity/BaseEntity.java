package com.spring.identity.entity;

import java.util.Date;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;
import lombok.experimental.FieldDefaults;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    Date createdAt;

    @LastModifiedDate
    Date updatedAt;

    Date deletedAt;
}
