package org.digilib.library.repositories;


import org.digilib.library.models.BookCopy;
import org.digilib.library.models.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy,Long> {

    Page<BookCopy> findAllByLibrary(Library library, Pageable pageable);

    Optional<BookCopy> findBookCopyByIdAndLibrary(long id, Library library);

    @Query(value = """
            SELECT barcode FROM book_copies \
            WHERE library_id = :libraryId \
            ORDER BY barcode DESC LIMIT 1
            """,
            nativeQuery = true)
    Optional<String> findMaxBarcodeByLibraryId(@Param("libraryId") long libraryId);

    @Query("""
        select bc
        from BookCopy bc
        join fetch bc.library l
        where bc.book.isbn = :isbn
        """)
    List<BookCopy> findByBookIsbnWithLibrary(@Param("isbn") String isbn);
}
