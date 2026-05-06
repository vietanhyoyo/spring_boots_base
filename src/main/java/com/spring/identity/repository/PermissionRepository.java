package com.spring.identity.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.identity.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    List<Permission> findAllByNameIn(Collection<String> names);
}
