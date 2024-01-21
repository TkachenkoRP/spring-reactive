package org.example.springreact.dto;

import lombok.Data;


@Data
public class UpsertUserRequest {
    private String username;
    private String email;
}
