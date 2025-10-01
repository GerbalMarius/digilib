package org.digilib.library.repositories;

import lombok.NonNull;
import org.digilib.library.models.Book;
import org.digilib.library.models.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {

    Optional<Book> findByIsbn(@NonNull String isbn);

    Page<Book> findAllByGenre(Genre genre, Pageable pageable);

}
