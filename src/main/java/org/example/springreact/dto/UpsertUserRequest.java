package org.example.springreact.dto;

import lombok.Data;
import org.example.springreact.model.RoleType;

import java.util.Set;


@Data
public class UpsertUserRequest {
    private String username;
    private String password;
    private String email;
    private Set<RoleType> roles;
}
