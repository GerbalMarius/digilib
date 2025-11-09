package org.digilib.library.controllers;

import lombok.extern.slf4j.Slf4j;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.auth.UserData;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserData.wrapUser(user));
    }
}
