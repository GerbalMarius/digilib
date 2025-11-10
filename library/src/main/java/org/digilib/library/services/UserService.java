package org.digilib.library.services;

import lombok.RequiredArgsConstructor;
import org.digilib.library.errors.exceptions.DuplicateEmailException;
import org.digilib.library.errors.exceptions.ResourceNotFoundException;
import org.digilib.library.models.Role;
import org.digilib.library.models.User;
import org.digilib.library.models.dto.auth.RegisterDto;
import org.digilib.library.models.dto.user.UserData;
import org.digilib.library.models.dto.user.UserUpdate;
import org.digilib.library.repositories.RoleRepository;
import org.digilib.library.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.digilib.library.utils.Params.setIfPresent;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserData signupUser(RegisterDto registerData, List<String> roleNames){

        if (userRepository.existsByEmail(registerData.email())) {
            throw new DuplicateEmailException("User with email " + registerData.email() + " already exists.");
        }

        List<Role> actualRoles = roleRepository.findAllByNameIn(roleNames);
        User user = User.builder()
                .email(registerData.email())
                .firstName(registerData.firstName())
                .lastName(registerData.lastName())
                .password(passwordEncoder.encode(registerData.password().trim()))
                .roles(actualRoles)
                .build();

        return UserData.wrapUser(userRepository.save(user));
    }

    public Page<UserData> findAll(long currentUserId, Pageable pageable){
        return userRepository.findAllByIdNot(currentUserId, pageable)
                .map(UserData::wrapUser);
    }

    public void disableUser(long userId, long currentUserId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, userId));

        if(user.getId() == currentUserId) {
            throw new IllegalArgumentException("The current user cannot disable himself");
        }

        userRepository.updateDisabled(user.getId(), true);

    }

    public void enableUser(long userId, long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, userId));

        if(user.getId() == currentUserId) {
            throw new IllegalArgumentException("The current user cannot enable himself");
        }

        userRepository.updateDisabled(user.getId(), false);
    }

    public UserData updateUser(long userId, UserUpdate userUpdate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of(User.class, userId));


        setIfPresent(userUpdate.firstName(), String::trim, user::setFirstName);
        setIfPresent(userUpdate.lastName(), String::trim, user::setLastName);
        setIfPresent(userUpdate.email(), String::trim, user::setEmail);
        setIfPresent(userUpdate.password(), String::trim, password -> user.setPassword(passwordEncoder.encode(password)));

        return UserData.wrapUser(userRepository.save(user));
    }
}
