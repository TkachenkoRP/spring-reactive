package org.example.springreact.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springreact.dto.UpsertUserRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.mapper.UserMapper;
import org.example.springreact.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;
    private final UserMapper userMapper;

    public Mono<ServerResponse> getAll(ServerRequest request) {
        Flux<UserResponse> users = userService.findAll().map(userMapper::entityToResponse);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users, UserResponse.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UserResponse> user = userService.findById(id).map(userMapper::entityToResponse);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return user.flatMap(u -> ServerResponse.ok().bodyValue(u))
                .switchIfEmpty(notFound);
    }


    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<UpsertUserRequest> user = request.bodyToMono(UpsertUserRequest.class);
        Mono<UserResponse> createdUser = user
                .map(userMapper::requestToEntity)
                .flatMap(userService::save)
                .map(userMapper::entityToResponse);
        return ServerResponse.ok().body(createdUser, UserResponse.class);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UpsertUserRequest> user = request.bodyToMono(UpsertUserRequest.class);
        Mono<UserResponse> updatedUser = user.flatMap(req -> userService.update(id, userMapper.requestToEntity(req)))
                .map(userMapper::entityToResponse);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return updatedUser.flatMap(u -> ServerResponse.ok().bodyValue(u))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Mono<Void> delete = userService.deleteById(request.pathVariable("id"));
        return delete.then(ServerResponse.noContent().build());
    }
}
