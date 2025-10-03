package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libraries", indexes = {
        @Index(name = "idx_libraries_id", columnList = "id", unique = true),
        @Index(name = "idx_libraries_name", columnList = "name",  unique = true)
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

    @Column(name = "address", length = 400, nullable = false)
    private String address;

    @Column(name = "phone_number", length = 30, nullable = false)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @OneToMany(mappedBy = "library", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private List<BookCopy> bookCopies = new ArrayList<>();


    public Library() {}
}
