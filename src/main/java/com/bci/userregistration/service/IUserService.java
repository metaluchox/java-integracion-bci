package com.bci.userregistration.service;

import java.util.List;

import com.bci.userregistration.dto.UserRegistrationRequest;
import com.bci.userregistration.dto.UserResponse;

public interface IUserService {
	List<UserResponse> findAll();
    UserResponse registerUser(UserRegistrationRequest request);
}
