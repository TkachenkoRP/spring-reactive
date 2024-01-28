package org.example.springreact.service;

import lombok.RequiredArgsConstructor;
import org.example.springreact.entity.TaskEntity;
import org.example.springreact.entity.UserEntity;
import org.example.springreact.repository.TaskRepository;
import org.example.springreact.utils.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;

    public Flux<TaskEntity> findAll() {
        return taskRepository.findAll()
                .flatMap(this::populateTaskWithUsers);
    }

    public Mono<TaskEntity> findById(String id) {
        return taskRepository.findById(id)
                .flatMap(this::populateTaskWithUsers);
    }

    public Mono<TaskEntity> save(TaskEntity task, String authorId) {
        task.setId(UUID.randomUUID().toString());
        task.setAuthorId(authorId);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setObserverIds(new HashSet<>());

        return populateTaskWithUsers(task)
                .flatMap(taskRepository::save);
    }

    public Mono<TaskEntity> update(String id, TaskEntity task) {
        return findById(id).flatMap(taskForUpdate -> {

            BeanUtils.copyNonNullProperties(task, taskForUpdate);
            taskForUpdate.setUpdatedAt(Instant.now());

            return populateTaskWithUsers(taskForUpdate)
                    .flatMap(taskRepository::save);
        });
    }

    public Mono<Void> deleteById(String id) {
        return taskRepository.deleteById(id);
    }

    public Mono<TaskEntity> addObserver(String id, String idObserver) {
        return findById(id)
                .flatMap(task -> {
                    if (task.getObserverIds() == null) {
                        task.setObserverIds(new HashSet<>());
                    }
                    task.getObserverIds().add(idObserver);
                    task.setUpdatedAt(Instant.now());
                    return userService.findById(idObserver)
                            .flatMap(observer -> {
                                if (task.getObservers() == null) {
                                    task.setObservers(new HashSet<>());
                                }
                                task.getObservers().add(observer);
                                return populateTaskWithUsers(task)
                                        .flatMap(taskRepository::save);
                            });
                });
    }

    private Mono<TaskEntity> populateTaskWithUsers(TaskEntity task) {
        Mono<UserEntity> authorMono = userService.findById(task.getAuthorId());
        Mono<UserEntity> assigneeMono = userService.findById(task.getAssigneeId());
        Flux<UserEntity> observerMonos = Flux.empty();
        if (task.getObserverIds() != null) {
            observerMonos = Flux.fromIterable(task.getObserverIds())
                    .flatMap(userService::findById);
        }

        return Mono.zip(authorMono, assigneeMono, observerMonos.collect(Collectors.toSet()))
                .map(tuple -> {
                    task.setAuthor(tuple.getT1());
                    task.setAssignee(tuple.getT2());
                    task.setObservers(tuple.getT3());
                    return task;
                });
    }
}
