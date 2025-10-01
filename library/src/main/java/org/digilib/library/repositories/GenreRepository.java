package org.digilib.library.repositories;


import org.digilib.library.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Genre findByTitle(String title);
}
