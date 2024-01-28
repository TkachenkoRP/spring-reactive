package org.example.springreact.service;

import lombok.RequiredArgsConstructor;
import org.example.springreact.entity.UserEntity;
import org.example.springreact.repository.UserRepository;
import org.example.springreact.utils.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Flux<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public Mono<UserEntity> findById(String id) {
        return userRepository.findById(id);
    }

    public Mono<UserEntity> save(UserEntity user) {
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Mono<UserEntity> update(String id, UserEntity user) {
        return findById(id).flatMap(userForUpdate -> {
            BeanUtils.copyNonNullProperties(user, userForUpdate);
            userForUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(userForUpdate);
        });
    }

    public Mono<Void> deleteById(String id) {
        return userRepository.deleteById(id);
    }

    public Mono<UserEntity> findByName(String userName) {
        return userRepository.findByUsername(userName)
                .switchIfEmpty(Mono.error(new RuntimeException("Username not found!")));
    }
}
