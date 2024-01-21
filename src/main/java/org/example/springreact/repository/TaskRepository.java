package org.example.springreact.repository;

import org.example.springreact.entity.TaskEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends ReactiveMongoRepository<TaskEntity, String> {
}
