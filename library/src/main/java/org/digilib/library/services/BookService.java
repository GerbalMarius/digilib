package org.digilib.library.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Book;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.BookCreateView;
import org.digilib.library.models.dto.BookUpdateView;
import org.digilib.library.repositories.AuthorRepository;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.digilib.library.utils.Params.setIfPresent;

@Service
@RequiredArgsConstructor
public  class BookService {
    
    private final BookRepository bookRepository;
    
    private final GenreRepository genreRepository;

    private final AuthorRepository authorRepository;


    public Book createBookFrom(BookCreateView createData) {

        Long genreId = createData.genreId();
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> ResourceNotFoundException.of(Genre.class, genreId));


        List<Author> authors = authorRepository.findAllById(createData.authorIds());

        if (authors.isEmpty() || authors.size() != createData.authorIds().size()) {
            throw ResourceNotFoundException.of(Author.class, createData.authorIds());
        }

        Book book = Book.builder()
                .isbn(createData.isbn())
                .title(createData.title())
                .summary(createData.summary())
                .imageUrl(createData.imageUrl())
                .pageCount(createData.pageCount())
                .publicationDate(createData.publicationDate())
                .language(createData.language())
                .edition(createData.edition())
                .genre(genre)
                .authors(authors)
                .build();

        return bookRepository.save(book);
    }

    public Book updateBookFrom(Book book, BookUpdateView updateData) {

        setIfPresent(updateData.title(), String::trim, book::setTitle);
        setIfPresent(updateData.summary(), String::trim, book::setSummary);
        setIfPresent(updateData.imageUrl(), String::trim, book::setImageUrl);
        setIfPresent(updateData.pageCount(), book::setPageCount);
        setIfPresent(updateData.publicationDate(), book::setPublicationDate);
        setIfPresent(updateData.language(), String::trim, book::setLanguage);
        setIfPresent(updateData.edition(), String::trim, book::setEdition);


        if (updateData.authorIds() != null) {
            List<Long> authorIds = updateData.authorIds();
            var authors = authorRepository.findAllById(authorIds);

            if (authors.size() != authorIds.size()) {
                throw ResourceNotFoundException.of(Author.class, authorIds);
            }

            book.setAuthors(authors);
        }

        return bookRepository.save(book);
    }

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Transactional
    public void deleteByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> ResourceNotFoundException.of(Book.class, isbn));

        bookRepository.delete(book);
    }
}
