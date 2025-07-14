package com.toeicify.toeic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(name = "role_id", nullable = false, length = 64)
    private String roleId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;
}

