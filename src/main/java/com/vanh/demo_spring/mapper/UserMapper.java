package com.vanh.demo_spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.vanh.demo_spring.dto.request.UserCreationRequest;
import com.vanh.demo_spring.dto.request.UserUpdateRequest;
import com.vanh.demo_spring.dto.response.UserResponse;
import com.vanh.demo_spring.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
