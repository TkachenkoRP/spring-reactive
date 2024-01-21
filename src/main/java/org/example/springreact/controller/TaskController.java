package org.example.springreact.controller;

import lombok.RequiredArgsConstructor;
import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.mapper.TaskMapper;
import org.example.springreact.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @GetMapping
    public Flux<TaskResponse> getAllTasks() {
        return taskService.findAll().map(taskMapper::entityToResponse);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskResponse>> getById(@PathVariable String id) {
        return taskService.findById(id)
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<TaskResponse>> createTask(@RequestBody UpsertTaskRequest request) {
        return taskService.save(taskMapper.requestToEntity(request))
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}")
    public Mono<ResponseEntity<TaskResponse>> updateTask(@PathVariable String id, @RequestBody UpsertTaskRequest request) {
        return taskService.update(id, taskMapper.requestToEntity(request))
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id) {
        return taskService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/{id}/addObserver/{idObserver}")
    public Mono<ResponseEntity<TaskResponse>> addObserver(@PathVariable String id, @PathVariable String idObserver) {
        return taskService.addObserver(id, idObserver)
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
