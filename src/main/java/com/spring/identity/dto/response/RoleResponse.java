package com.spring.identity.dto.response;

import java.util.Set;

import com.spring.identity.entity.RoleResourcePermission;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<RoleResourcePermission> resourcePermissions;
}
