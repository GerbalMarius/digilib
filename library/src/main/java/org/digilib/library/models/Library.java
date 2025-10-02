package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "libraries", indexes = {
        @Index(name = "idx_libraries_id", columnList = "id", unique = true)
})
@Data
@AllArgsConstructor
@Builder
public final class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name",  nullable = false,  unique = true,  length = 200)
    private String name;

    @Column(name = "address", length = 400)
    private String address;


    public Library() {}
}
