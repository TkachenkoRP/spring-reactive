package org.example.springreact.routing;

import org.example.springreact.AbstractTest;
import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskRoutingTest extends AbstractTest {
    @Test
    public void whenGetAllTasksWithoutRole_thenReturnError() {
        webTestClient.get().uri("/api/functions/tasks")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenGetAllTasksWithRole_thenReturnListOfTasks() {
        webTestClient.get().uri("/api/functions/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskResponse.class)
                .hasSize(2);
    }

    @Test
    public void whenGetTaskByIdWithoutRole_thenReturnError() {
        webTestClient.get().uri("/api/functions/tasks/{id}", SECOND_TASK_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenGetTaskByIdWithRole_thenReturnTaskById() {
        webTestClient.get().uri("/api/functions/tasks/{id}", SECOND_TASK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assertEquals(SECOND_TASK_ID, response.getId());
                    assertEquals("Task 2", response.getName());
                    assertEquals("Description 2", response.getDescription());
                    assertEquals(TaskStatus.TODO, response.getStatus());
                    assertInstanceOf(UserResponse.class, response.getAuthor());
                    assertEquals(SECOND_USER_ID, response.getAuthor().getId());
                    assertInstanceOf(UserResponse.class, response.getAssignee());
                    assertEquals(FIRST_USER_ID, response.getAssignee().getId());
                    assertNotNull(response.getObservers());
                    assertEquals(2, response.getObservers().size());
                    assertThat(response.getObservers(), containsInAnyOrder(
                            hasProperty("id", is(FIRST_USER_ID)),
                            hasProperty("id", is(SECOND_USER_ID))
                    ));
                });
    }

    @Test
    public void whenCreateTaskWithoutRole_thenReturnError() {
        webTestClient.post().uri("/api/functions/tasks")
                .body(Mono.just(new UpsertTaskRequest()), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void whenCreateTaskWithAuthorizationWrongRole_thenReturnNewTask() {
        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("New Task");
        request.setDescription("Description 3");
        request.setStatus(TaskStatus.DONE);
        request.setAssigneeId(FIRST_USER_ID);

        webTestClient.post().uri("/api/functions/tasks")
                .header("Authorization", FIRST_USER_AUTHORIZATION)
                .body(Mono.just(request), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void whenCreateTaskWithAuthorization_thenReturnNewTask() {
        StepVerifier.create(taskRepository.count())
                .expectNext(2L)
                .expectComplete()
                .verify();

        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("New Task");
        request.setDescription("Description 3");
        request.setStatus(TaskStatus.DONE);
        request.setAssigneeId(FIRST_USER_ID);

        webTestClient.post().uri("/api/functions/tasks")
                .header("Authorization", SECOND_USER_AUTHORIZATION)
                .body(Mono.just(request), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals("New Task", response.getName());
                    assertEquals("Description 3", response.getDescription());
                    assertNotNull(response.getCreatedAt());
                    assertNotNull(response.getUpdatedAt());
                    assertEquals(TaskStatus.DONE, response.getStatus());
                    assertInstanceOf(UserResponse.class, response.getAuthor());
                    assertEquals(SECOND_USER_ID, response.getAuthor().getId());
                    assertInstanceOf(UserResponse.class, response.getAssignee());
                    assertEquals(FIRST_USER_ID, response.getAssignee().getId());
                });

        StepVerifier.create(taskRepository.count())
                .expectNext(3L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateTaskWithoutRole_thenReturnError() {
        webTestClient.put().uri("/api/functions/tasks/{id}", SECOND_TASK_ID)
                .body(Mono.just(new UpsertTaskRequest()), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenUpdateTaskWithWrongRole_thenReturnError() {
        webTestClient.put().uri("/api/functions/tasks/{id}", SECOND_TASK_ID)
                .body(Mono.just(new UpsertTaskRequest()), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void whenUpdateTaskWithRole_thenReturnUpdatedTask() {
        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("New Task Name");
        request.setStatus(TaskStatus.DONE);

        webTestClient.put().uri("/api/functions/tasks/{id}", SECOND_TASK_ID)
                .body(Mono.just(request), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assertEquals(SECOND_TASK_ID, response.getId());
                    assertEquals("New Task Name", response.getName());
                    assertEquals(TaskStatus.DONE, response.getStatus());
                    assertNotNull(response.getUpdatedAt());
                });
    }

    @Test
    public void whenDeleteByIdWithoutRole_thenReturnError() {
        webTestClient.delete().uri("/api/functions/tasks/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void whenDeleteByIdWithWrongRole_thenReturnError() {
        webTestClient.delete().uri("/api/functions/tasks/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void whenDeleteByIdWithRole_thenRemoveTaskFromDatabase() {
        webTestClient.delete().uri("/api/functions/tasks/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(taskRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenAddObserverWithoutRole_thenReturnError() {
        webTestClient.post().uri("/api/functions/tasks/{id}/observe", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void whenAddObserverWithAuthorization_thenReturnTask() {
        webTestClient.post().uri("/api/functions/tasks/{id}/observe", FIRST_TASK_ID)
                .header("Authorization", FIRST_USER_AUTHORIZATION)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assertEquals(FIRST_TASK_ID, response.getId());
                    assertNotNull(response.getObservers());
                    assertEquals(1, response.getObservers().size());
                    assertThat(response.getObservers(), containsInAnyOrder(
                            hasProperty("id", is(FIRST_USER_ID))
                    ));
                });
    }
}
