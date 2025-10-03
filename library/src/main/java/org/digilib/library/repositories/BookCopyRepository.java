package org.digilib.library.repositories;


import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Genre;
import org.digilib.library.models.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy,Long> {

    Page<BookCopy> findAllByLibrary(Library library, Pageable pageable);

    Page<BookCopy> findAllByBookGenre(Genre bookGenre, Pageable pageable);
}
