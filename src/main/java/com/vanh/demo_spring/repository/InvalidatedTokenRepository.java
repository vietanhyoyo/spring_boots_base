package com.vanh.demo_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vanh.demo_spring.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
