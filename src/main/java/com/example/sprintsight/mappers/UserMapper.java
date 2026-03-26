package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.requests.UpdateUserPatchRequest;
import com.example.sprintsight.dtos.requests.UpdateUserPutRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromPut(UpdateUserPutRequest request, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromPatch(UpdateUserPatchRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);
}
