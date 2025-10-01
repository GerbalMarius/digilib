package org.digilib.library.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "authors")
@Data
public final class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
}
