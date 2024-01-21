package org.example.springreact.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.mapper.TaskMapper;
import org.example.springreact.service.TaskService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskHandler {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<TaskResponse> tasks = taskService.findAll().map(taskMapper::entityToResponse);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tasks, TaskResponse.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<TaskResponse> task = taskService.findById(id).map(taskMapper::entityToResponse);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return task.flatMap(t -> ServerResponse.ok().bodyValue(t))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<UpsertTaskRequest> task = request.bodyToMono(UpsertTaskRequest.class);
        Mono<TaskResponse> createdTask = task
                .map(taskMapper::requestToEntity)
                .flatMap(taskService::save)
                .map(taskMapper::entityToResponse);
        return ServerResponse.ok().body(createdTask, TaskResponse.class);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UpsertTaskRequest> task = request.bodyToMono(UpsertTaskRequest.class);
        Mono<TaskResponse> updateTask = task.flatMap(req -> taskService.update(id, taskMapper.requestToEntity(req)))
                .map(taskMapper::entityToResponse);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return updateTask.flatMap(t -> ServerResponse.ok().bodyValue(t))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Mono<Void> delete = taskService.deleteById(request.pathVariable("id"));
        return delete.then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> addObserver(ServerRequest request) {
        String id = request.pathVariable("id");
        String idObserver = request.pathVariable("idObserver");
        Mono<TaskResponse> task = taskService.addObserver(id, idObserver)
                .map(taskMapper::entityToResponse);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return task.flatMap(t -> ServerResponse.ok().bodyValue(t))
                .switchIfEmpty(notFound);
    }
}
