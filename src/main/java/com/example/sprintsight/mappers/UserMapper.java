package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.entities.User;
import org.mapstruct.*;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "password", ignore = true)
    void updateUserFromPut(UpdateUserRequest request, @MappingTarget User user);

    @InheritConfiguration(name = "updateUserFromPut")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromPatch(UpdateUserRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);

    default String map(JsonNullable<String> value) {
        return value == null ? null : value.orElse(null);
    }


    @AfterMapping
    default void handleNullable(UpdateUserRequest request, @MappingTarget User user) {
        if (request.bio() != null && request.bio().isPresent()) {
            user.setBio(request.bio().orElse(null));
        }
        if (request.fullName() != null && request.fullName().isPresent()) {
            user.setFullName(request.fullName().orElse(null));
        }
    }
}
