package org.example.springreact.controller;

import org.example.springreact.AbstractTest;
import org.example.springreact.dto.UpsertUserRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.model.RoleType;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest extends AbstractTest {
    @Test
    public void whenGetAllUsersWithoutRole_thenReturnError() {
        webTestClient.get().uri("/api/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenGetAllUsersWithRole_thenReturnListOfUsers() {
        var expectedData = List.of(
                new UserResponse(FIRST_USER_ID, "Name 1", "mail1@m.ru", Set.of(RoleType.ROLE_USER)),
                new UserResponse(SECOND_USER_ID, "Name 2", "mail2@m.ru", Set.of(RoleType.ROLE_MANAGER))
        );

        webTestClient.get().uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(2)
                .contains(expectedData.toArray(UserResponse[]::new));
    }

    @Test
    public void whenGetUserByIdWithoutRole_thenReturnError() {
        webTestClient.get().uri("/api/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenGetUserByIdWithRole_thenReturnUserById() {
        var expectedData = new UserResponse(FIRST_USER_ID, "Name 1", "mail1@m.ru", Set.of(RoleType.ROLE_USER));

        webTestClient.get().uri("/api/users/{id}", FIRST_USER_ID)
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
        request.setPassword("111");
        request.setRoles(Set.of(RoleType.ROLE_MANAGER));

        webTestClient.post().uri("/api/users")
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
    public void whenUpdateUserWithoutRole_thenReturnError() {
        webTestClient.get().uri("/api/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenUpdateUserWithRole_thenReturnUpdatedUser() {
        var expectedData = new UserResponse(FIRST_USER_ID, "New User Name", "New_mail1@m.ru", Set.of(RoleType.ROLE_USER));

        UpsertUserRequest request = new UpsertUserRequest();
        request.setUsername("New User Name");
        request.setEmail("New_mail1@m.ru");
        request.setPassword("111");
        request.setRoles(Set.of(RoleType.ROLE_USER));

        webTestClient.put().uri("/api/users/{id}", FIRST_USER_ID)
                .body(Mono.just(request), UpsertUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertEquals("New User Name", response.getUsername());
                    assertEquals("New_mail1@m.ru", response.getEmail());
                });

        webTestClient.get().uri("/api/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenDeleteByIdWithoutRole_thenReturnError() {
        webTestClient.delete().uri("/api/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenDeleteByIdWithRole_thenRemoveUserFromDatabase() {
        webTestClient.delete().uri("/api/users/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(userRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();
    }
}
