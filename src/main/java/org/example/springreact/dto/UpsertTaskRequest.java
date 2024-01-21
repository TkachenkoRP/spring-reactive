package org.example.springreact.dto;

import lombok.Data;
import org.example.springreact.model.TaskStatus;

@Data
public class UpsertTaskRequest {
    private String name;
    private String description;
    private TaskStatus status;
    private String authorId;
    private String assigneeId;
}
