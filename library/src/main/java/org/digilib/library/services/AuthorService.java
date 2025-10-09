package org.digilib.library.services;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Author;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.AuthorCreateView;
import org.digilib.library.models.dto.AuthorData;
import org.digilib.library.models.dto.AuthorUpdateView;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.repositories.AuthorRepository;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

import static org.digilib.library.utils.Params.setIfPresent;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    private final Validator validator;

    public Page<AuthorData> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorData::wrapAuthor);
    }

    public AuthorData findById(long authorId) {

        return authorRepository.findById(authorId)
                .map(AuthorData::wrapAuthor)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));
    }

    public Page<BookData> findBooksByAuthor(long authorId,  Pageable pageable) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));

        return bookRepository.findAllByAuthors_Id(author.getId(), pageable)
                .map(BookData::wrapBook);
    }

    public void deleteById(long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));

        authorRepository.delete(author);

    }

    public AuthorData findAuthorByGenre(long authorId, Genre genre) {
        return authorRepository.findByIdAndGenre(authorId, genre)
                .map(AuthorData::wrapAuthor)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));
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

    @Transactional
    public AuthorData update(long authorId, AuthorUpdateView authorUpdateView) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of(Author.class, authorId));

        LocalDate deathDate = authorUpdateView.getDeathDate();

        authorUpdateView.setExistingBirthDate(author.getBirthDate());

        Set<ConstraintViolation<AuthorUpdateView>> violations = validator.validate(authorUpdateView);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        setIfPresent(authorUpdateView.getFirstName(), String::trim, author::setFirstName);
        setIfPresent(authorUpdateView.getLastName(), String::trim, author::setLastName);
        setIfPresent(deathDate, author::setDeathDate);



        Author saved = authorRepository.save(author);

        return AuthorData.wrapAuthor(saved);
    }
}
