package org.example.springreact;

import org.example.springreact.entity.TaskEntity;
import org.example.springreact.entity.UserEntity;
import org.example.springreact.model.RoleType;
import org.example.springreact.model.TaskStatus;
import org.example.springreact.repository.TaskRepository;
import org.example.springreact.repository.UserRepository;
import org.example.springreact.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

import java.util.*;

@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
public class AbstractTest {
    protected static String FIRST_USER_ID = UUID.randomUUID().toString();
    protected static String SECOND_USER_ID = UUID.randomUUID().toString();
    protected static String FIRST_TASK_ID = UUID.randomUUID().toString();
    protected static String SECOND_TASK_ID = UUID.randomUUID().toString();
    protected static String FIRST_USER_AUTHORIZATION = "Basic " + Base64.getEncoder().encodeToString("Name 1:111".getBytes());
    protected static String SECOND_USER_AUTHORIZATION = "Basic " + Base64.getEncoder().encodeToString("Name 2:222".getBytes());

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.8")
            .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {

        TaskEntity firstTask = new TaskEntity();
        firstTask.setId(FIRST_TASK_ID);
        firstTask.setName("Task 1");
        firstTask.setDescription("Description 1");
        firstTask.setStatus(TaskStatus.IN_PROGRESS);
        firstTask.setAuthorId(FIRST_USER_ID);
        firstTask.setAssigneeId(SECOND_USER_ID);
        firstTask.setObserverIds(new HashSet<>());

        TaskEntity secondTask = new TaskEntity();
        secondTask.setId(SECOND_TASK_ID);
        secondTask.setName("Task 2");
        secondTask.setDescription("Description 2");
        secondTask.setStatus(TaskStatus.TODO);
        secondTask.setAuthorId(SECOND_USER_ID);
        secondTask.setAssigneeId(FIRST_USER_ID);
        secondTask.setObserverIds(Set.of(FIRST_USER_ID, SECOND_USER_ID));

        userRepository.saveAll(
                Flux.just(
                        new UserEntity(FIRST_USER_ID, "Name 1", "mail1@m.ru", passwordEncoder.encode("111"), Set.of(RoleType.ROLE_USER)),
                        new UserEntity(SECOND_USER_ID, "Name 2", "mail2@m.ru", passwordEncoder.encode("222"), Set.of(RoleType.ROLE_MANAGER))
                )
        ).thenMany(
                taskRepository.saveAll(
                        Flux.just(
                                firstTask,
                                secondTask
                        )
                )
        ).blockLast();
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll()
                .then(taskRepository.deleteAll())
                .block();
    }
}
