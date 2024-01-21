package org.example.springreact.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.springreact.model.TaskStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class TaskEntity {
    @Id
    private String id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private TaskStatus status;
    private String authorId;
    private String assigneeId;
    private Set<String> observerIds;

    @ReadOnlyProperty
    private UserEntity author;
    @ReadOnlyProperty
    private UserEntity assignee;
    @ReadOnlyProperty
    private Set<UserEntity> observers;
}
