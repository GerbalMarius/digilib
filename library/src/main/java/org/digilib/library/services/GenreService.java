package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.ResourceNotFoundException;
import org.digilib.library.models.Genre;
import org.digilib.library.models.dto.BookData;
import org.digilib.library.models.dto.GenreCreateView;
import org.digilib.library.models.dto.GenreData;
import org.digilib.library.models.dto.GenreUpdateView;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;


    public List<GenreData> findAll(Sort sort) {
        return genreRepository.findAll(sort)
                .stream()
                .map(GenreData::wrapGenre)
                .toList();
    }

    public GenreData findGenreById(long id) {
        return genreRepository.findById(id)
                .map(GenreData::wrapGenre)
                .orElseThrow(() -> ResourceNotFoundException.of(Genre.class, id));
    }

    public Page<BookData> findBooksByGenreId(long id, Pageable pageable) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Genre.class, id));

        return bookRepository.findAllByGenre(genre, pageable)
                .map(BookData::wrapBook);
    }

    public GenreData createGenre(GenreCreateView genreCreateData) {
        Genre created = Genre.builder()
                .title(genreCreateData.title())
                .build();

        Genre saved = genreRepository.save(created);

        return GenreData.wrapGenre(saved);
    }

    public GenreData updateGenre(long id, GenreUpdateView genreUpdateData) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(Genre.class, id));

        genre.setTitle(genreUpdateData.title());
        genreRepository.save(genre);
        return GenreData.wrapGenre(genre);
    }
}
