package com.vanh.demo_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vanh.demo_spring.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {}
