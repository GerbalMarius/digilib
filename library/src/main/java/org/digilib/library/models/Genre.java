package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "genres", indexes = {
        @Index(name = "idx_genre_id", columnList = "id", unique = true),
        @Index(name = "idx_genre_title", columnList = "title", unique = true)
})
@Data
@AllArgsConstructor
@Builder
public final class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "title", length = 80, nullable = false)
    private String title;

    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Book> books = new ArrayList<>();


    public Genre() {}

}
