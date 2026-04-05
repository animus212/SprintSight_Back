package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.responses.UserSummaryResponse;
import com.example.sprintsight.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userRole", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromPut(UpdateUserRequest request, @MappingTarget User user);

    @InheritConfiguration(name = "updateUserFromPut")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromPatch(UpdateUserRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);

    UserSummaryResponse toUserSummaryResponse(User user);
}
