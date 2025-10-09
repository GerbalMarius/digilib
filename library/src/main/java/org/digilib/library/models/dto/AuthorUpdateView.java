package org.digilib.library.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.digilib.library.validators.DeathAfterBirth;


import java.time.LocalDate;

@Setter
@Getter
@DeathAfterBirth
public class AuthorUpdateView {

    // getters / setters
    // optional fields for PATCH
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String firstName;
    private String lastName;


    @JsonIgnore
    private LocalDate existingBirthDate;

    public AuthorUpdateView() {}

}

