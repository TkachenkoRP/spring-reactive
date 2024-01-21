package org.example.springreact.controller;

import org.example.springreact.AbstractTest;
import org.example.springreact.dto.TaskResponse;
import org.example.springreact.dto.UpsertTaskRequest;
import org.example.springreact.dto.UserResponse;
import org.example.springreact.model.TaskStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskControllerTest extends AbstractTest {

    @Test
    public void whenGetAllTasks_thenReturnListOfTasks() {
        webTestClient.get().uri("/api/functions/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskResponse.class)
                .hasSize(2);
    }

    @Test
    public void whenGetTaskById_thenReturnTaskById() {
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
    public void whenCreateTask_thenReturnNewTask() {
        StepVerifier.create(taskRepository.count())
                .expectNext(2L)
                .expectComplete()
                .verify();

        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("New Task");
        request.setDescription("Description 3");
        request.setStatus(TaskStatus.DONE);
        request.setAuthorId(FIRST_USER_ID);
        request.setAssigneeId(SECOND_USER_ID);

        webTestClient.post().uri("/api/functions/tasks")
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
                    assertEquals(FIRST_USER_ID, response.getAuthor().getId());
                    assertInstanceOf(UserResponse.class, response.getAssignee());
                    assertEquals(SECOND_USER_ID, response.getAssignee().getId());
                });

        StepVerifier.create(taskRepository.count())
                .expectNext(3L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateTask_thenReturnUpdatedTask() {
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
    public void whenDeleteById_thenRemoveTaskFromDatabase() {
        webTestClient.delete().uri("/api/functions/tasks/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(taskRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenAddObserver_thenReturnTask() {
        webTestClient.post().uri("/api/functions/tasks/{id}/addObserver/{idObserver}", FIRST_TASK_ID, FIRST_USER_ID)
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
