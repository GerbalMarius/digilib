package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_roles_id", columnList = "id", unique = true),
        @Index(name = "idx_roles_name", columnList = "name", unique = true)
})
@Data
@AllArgsConstructor
@Builder
public final class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", unique = true, nullable = false, length = 60)
    private String name;


    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();

    public Role(){}
}
