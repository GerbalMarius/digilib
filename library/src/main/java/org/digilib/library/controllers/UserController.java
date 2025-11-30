package org.digilib.library.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digilib.library.errors.exceptions.InvalidRequestParamException;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.user.UserData;
import org.digilib.library.models.dto.user.UserUpdate;
import org.digilib.library.services.JwtService;
import org.digilib.library.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.digilib.library.LibraryApplication.PAGE_SIZE;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(@AuthenticationPrincipal User currentUser,
                                                   @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        List<String> roles = jwtService.extractRolesFromToken(token);
        return ResponseEntity.ok(UserData.wrapUser(currentUser, roles));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<UserData>> getAllUsers(@AuthenticationPrincipal User currentUser,
                                                      @RequestParam(name = "page") int pageNumber,
                                                      @RequestParam(name = "sorts") String[] sorts){

        InvalidRequestParamException.notPositivePage(pageNumber);

        InvalidRequestParamException.notValidSorts(sorts, User.class);

        var pageable = PageRequest.of(
                pageNumber - 1,
                PAGE_SIZE,
                Sort.by(sorts)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePrivate())
                .body(userService.findAll(currentUser.getId(), pageable));

    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserData> updateUser(@AuthenticationPrincipal User currentUser,
                                               @PathVariable long id,
                                               @RequestBody @Valid UserUpdate userUpdate) {

        return ResponseEntity.ok(userService.updateUser(currentUser, id, userUpdate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/disable")
    public ResponseEntity<?> disableUserById(@AuthenticationPrincipal User currentUser,
                                             @PathVariable long id) {
        userService.disableUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUserById(@AuthenticationPrincipal User currentUser,
                                            @PathVariable long id) {
        userService.enableUser(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
