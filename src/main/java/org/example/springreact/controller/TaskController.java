package org.example.springreact.controller;

import lombok.RequiredArgsConstructor;
import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.mapper.TaskMapper;
import org.example.springreact.security.AppUserPrincipal;
import org.example.springreact.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @GetMapping
    public Flux<TaskResponse> getAllTasks() {
        return taskService.findAll().map(taskMapper::entityToResponse);
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskResponse>> getById(@PathVariable String id) {
        return taskService.findById(id)
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public Mono<ResponseEntity<TaskResponse>> createTask(@RequestBody UpsertTaskRequest request,
                                                         @AuthenticationPrincipal Mono<AppUserPrincipal> principal) {
        return principal.flatMap(p -> taskService.save(taskMapper.requestToEntity(request), p.getUserId())
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id}")
    public Mono<ResponseEntity<TaskResponse>> updateTask(@PathVariable String id, @RequestBody UpsertTaskRequest request) {
        return taskService.update(id, taskMapper.requestToEntity(request))
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id) {
        return taskService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @PostMapping("/{id}/observe")
    public Mono<ResponseEntity<TaskResponse>> addObserver(@PathVariable String id,
                                                          @AuthenticationPrincipal Mono<AppUserPrincipal> principal) {
        return principal.flatMap(p -> taskService.addObserver(id, p.getUserId())
                .map(taskMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()));
    }
}
