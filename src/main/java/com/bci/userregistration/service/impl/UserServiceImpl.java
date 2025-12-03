package com.bci.userregistration.service.impl;

import com.bci.userregistration.dto.PhoneDTO;
import com.bci.userregistration.dto.UserRegistrationRequest;
import com.bci.userregistration.dto.UserResponse;
import com.bci.userregistration.entity.Phone;
import com.bci.userregistration.entity.User;
import com.bci.userregistration.exception.DuplicateEmailException;
import com.bci.userregistration.exception.ValidationException;
import com.bci.userregistration.repository.UserRepository;
import com.bci.userregistration.service.JwtService;
import com.bci.userregistration.service.IUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
	
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${validation.email.pattern}")
    private String emailPattern;

    @Value("${validation.password.pattern}")
    private String passwordPattern;

    @Value("${validation.password.message}")
    private String passwordMessage;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("El correo ya registrado");
        }
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(request.getEmail(), userId);
        User user = buildUser(request, userId, token);
        addPhonesToUser(user, request);

        User savedUser = userRepository.save(user);

        return buildUserResponse(savedUser);
    }

    private User buildUser(UserRegistrationRequest request, UUID userId, String token) {
        return User.builder()
        		.id(userId)
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .token(token)
                .isActive(true)
                .phones(new ArrayList<>())
                .build();
    }

    private void addPhonesToUser(User user, UserRegistrationRequest request) {
        if (request.getPhones() != null && !request.getPhones().isEmpty()) {
            for (PhoneDTO phoneDTO : request.getPhones()) {
                Phone phone = Phone.builder()
                        .number(phoneDTO.getNumber())
                        .citycode(phoneDTO.getCitycode())
                        .contrycode(phoneDTO.getContrycode())
                        .build();
                user.addPhone(phone);
            }
        }
    }
    

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.getIsActive())
                .build();
    }

    
    private void validateEmail(String email) {
        if (!Pattern.matches(emailPattern, email)) {
            throw new ValidationException("El formato del correo es inválido");
        }
    }

    private void validatePassword(String password) {
        if (!Pattern.matches(passwordPattern, password)) {
            throw new ValidationException(passwordMessage);
        }
    }
}
