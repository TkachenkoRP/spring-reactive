package org.example.springreact.mapper;

import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    TaskEntity requestToEntity(UpsertTaskRequest request);

    TaskResponse entityToResponse(TaskEntity entity);
}
