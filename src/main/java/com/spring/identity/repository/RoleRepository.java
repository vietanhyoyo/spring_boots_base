package com.spring.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.identity.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
