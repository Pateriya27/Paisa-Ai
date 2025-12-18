package com.paisa.dto;

import com.paisa.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private String id;
    private String email;
    private String name;
    private User.Role role;
    private LocalDateTime createdAt;
}

