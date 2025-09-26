package org.digilib.library.repositories;

import lombok.NonNull;
import org.digilib.library.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {
    @NonNull
    Page<Book> findAll(@NonNull Pageable pageable);
}
