package org.example.springreact.controller;

import org.example.springreact.AbstractTest;
import org.example.springreact.dto.UpsertUserRequest;
import org.example.springreact.dto.UserResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest extends AbstractTest {

    @Test
    public void whenGetAllUsers_thenReturnListOfUsers() {
        var expectedData = List.of(
                new UserResponse(FIRST_USER_ID, "Name 1", "mail1@m.ru"),
                new UserResponse(SECOND_USER_ID, "Name 2", "mail2@m.ru")
        );

        webTestClient.get().uri("/api/functions/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(2)
                .contains(expectedData.toArray(UserResponse[]::new));
    }

    @Test
    public void whenGetUserById_thenReturnUserById() {
        var expectedData = new UserResponse(FIRST_USER_ID, "Name 1", "mail1@m.ru");

        webTestClient.get().uri("/api/functions/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenCreateUser_thenReturnNewUser() {
        StepVerifier.create(userRepository.count())
                .expectNext(2L)
                .expectComplete()
                .verify();

        UpsertUserRequest request = new UpsertUserRequest();
        request.setUsername("New User");
        request.setEmail("mail3@m.ru");

        webTestClient.post().uri("/api/functions/users")
                .body(Mono.just(request), UpsertUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals("New User", response.getUsername());
                    assertEquals("mail3@m.ru", response.getEmail());
                });

        StepVerifier.create(userRepository.count())
                .expectNext(3L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateUser_thenReturnUpdatedUser() {
        var expectedData = new UserResponse(FIRST_USER_ID, "New User Name", "New_mail1@m.ru");

        UpsertUserRequest request = new UpsertUserRequest();
        request.setUsername("New User Name");
        request.setEmail("New_mail1@m.ru");

        webTestClient.put().uri("/api/functions/users/{id}", FIRST_USER_ID)
                .body(Mono.just(request), UpsertUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertEquals("New User Name", response.getUsername());
                    assertEquals("New_mail1@m.ru", response.getEmail());
                });

        webTestClient.get().uri("/api/functions/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenDeleteById_thenRemoveUserFromDatabase() {
        webTestClient.delete().uri("/api/functions/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(userRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();
    }
}
