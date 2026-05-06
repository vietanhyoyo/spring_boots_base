package com.spring.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.identity.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, String> {
    Optional<Resource> findByCode(String code);
}
