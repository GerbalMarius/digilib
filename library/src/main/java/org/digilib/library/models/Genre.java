package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "title", length = 30, nullable = false)
    private String title;

    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private List<Book> books = new ArrayList<>();

    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private List<Author> authors = new ArrayList<>();


    public Genre() {}

}
