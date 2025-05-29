package com.spring.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.identity.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {}
