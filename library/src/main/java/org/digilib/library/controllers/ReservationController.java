package org.digilib.library.controllers;

import lombok.RequiredArgsConstructor;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.book.LibraryBookData;
import org.digilib.library.services.BookCopyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final BookCopyService bookCopyService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/books/{copyId}/reserve")
    public ResponseEntity<LibraryBookData> reserveBookCopy(@PathVariable long copyId,
                                                           @AuthenticationPrincipal User currentUser) {


        LibraryBookData reserved = bookCopyService.reserveCopy(copyId, currentUser.getEmail());

        return ResponseEntity.ok(reserved);
    }
}