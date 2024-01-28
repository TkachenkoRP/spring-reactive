package org.example.springreact.controller;

import lombok.RequiredArgsConstructor;
import org.example.springreact.dto.UpsertUserRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.mapper.UserMapper;
import org.example.springreact.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @GetMapping
    public Flux<UserResponse> getAllUsers() {
        return userService.findAll().map(userMapper::entityToResponse);
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getById(@PathVariable String id) {
        return userService.findById(id)
                .map(userMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> createUser(@RequestBody UpsertUserRequest request) {
        return userService.save(userMapper.requestToEntity(request))
                .map(userMapper::entityToResponse)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable String id, @RequestBody UpsertUserRequest request) {
        return userService.update(id, userMapper.requestToEntity(request))
                .map(userMapper::entityToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userService.deleteById(id).then(Mono.just(ResponseEntity.noContent().build()));
    }
}
