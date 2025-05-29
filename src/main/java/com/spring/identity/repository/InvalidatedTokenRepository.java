package com.spring.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.identity.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
