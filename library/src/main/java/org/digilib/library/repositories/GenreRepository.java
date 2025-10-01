package org.digilib.library.repositories;


import org.digilib.library.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findById(long id);
}
