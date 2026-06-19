package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.UserRequest;
import com.example.sprintsight.dtos.responses.AdminUserResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.responses.UserSummaryResponse;
import com.example.sprintsight.entities.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "password",  ignore = true)
    @Mapping(target = "userRole",  ignore = true)
    @Mapping(target = "enabled",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "enabled",   ignore = true)
    @Mapping(target = "password",  ignore = true)
    @Mapping(target = "userRole",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    void updateUserFromRequest(UserRequest request, @MappingTarget User user);

    UserResponse toUserResponse(User user);

    AdminUserResponse toAdminUserResponse(User user);

    UserSummaryResponse toUserSummaryResponse(User user);
}
