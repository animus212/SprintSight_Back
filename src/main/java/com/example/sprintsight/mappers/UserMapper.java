package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.RegisterRequest;
import com.example.sprintsight.dtos.requests.UpdateUserRequest;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.entities.User;
import org.mapstruct.*;

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
}
