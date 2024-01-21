package org.example.springreact.dto;

import lombok.Data;
import org.example.springreact.model.TaskStatus;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@Data
public class TaskResponse {
    private String id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private TaskStatus status;

    private UserResponse author;
    private UserResponse assignee;
    private Set<UserResponse> observers;
}
