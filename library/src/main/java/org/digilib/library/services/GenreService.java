package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.models.Genre;
import org.digilib.library.repositories.BookRepository;
import org.digilib.library.repositories.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    public Page<Genre> findAll(PageRequest pageable) {
        return genreRepository.findAll(pageable);
    }
}
