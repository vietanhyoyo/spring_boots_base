package com.vanh.demo_spring.repository;

import com.vanh.demo_spring.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
