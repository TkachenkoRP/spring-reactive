package org.example.springreact.mapper;

import org.example.springreact.dto.UpsertUserRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserEntity requestToEntity(UpsertUserRequest request);

    UserResponse entityToResponse(UserEntity entity);
}
