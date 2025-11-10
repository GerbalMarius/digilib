package org.digilib.library.models.dto.author;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.digilib.library.validators.death_date.DeathDate;


import java.time.LocalDate;

@Setter
@Getter
@DeathDate
public class AuthorUpdateView {


    private LocalDate birthDate;
    private LocalDate deathDate;
    private String firstName;
    private String lastName;


    @JsonIgnore
    private LocalDate existingBirthDate;

    public AuthorUpdateView() {}

}

