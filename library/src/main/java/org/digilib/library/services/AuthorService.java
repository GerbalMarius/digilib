package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.AuthorCreateView;
import org.digilib.library.models.dto.AuthorData;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.repositories.AuthorRepository;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    public Page<AuthorData> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorData::wrapAuthor);
    }

    public AuthorData findById(long id) {

        return authorRepository.findById(id)
                .map(AuthorData::wrapAuthor)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, id));
    }

    public Page<BookData> findBooksByAuthor(long authorId,  Pageable pageable) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));

        return bookRepository.findAllByAuthors_Id(author.getId(), pageable)
                .map(BookData::wrapBook);
    }

    public void deleteById(long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, id));

        authorRepository.delete(author);

    }

    public AuthorData createFrom(AuthorCreateView authorData) {
        Genre genre = genreRepository.findById(authorData.genreId())
                .orElseThrow(() -> ResourceNotFoundException.of(Genre.class, authorData.genreId()));

        Author author = Author.builder()
                .firstName(authorData.firstName())
                .lastName(authorData.lastName())
                .birthDate(authorData.birthDate())
                .deathDate(authorData.deathDate())
                .genre(genre)
                .build();

        authorRepository.save(author);
        return AuthorData.wrapAuthor(author);
    }
}
