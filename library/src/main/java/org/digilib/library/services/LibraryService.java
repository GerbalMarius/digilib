package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.models.dto.LibraryData;
import org.digilib.library.repositories.LibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryService {
    private final LibraryRepository libraryRepository;


    public Page<LibraryData> findAll(Pageable pageable) {
        return libraryRepository.findAll(pageable)
                .map(LibraryData::wrapLibrary);
    }

}
