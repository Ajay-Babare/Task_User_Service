package com.ajay.service;

import java.util.List;

import com.ajay.model.User;

public interface UserService {

	public User getUserProfile(String jwt);
	
	public List<User> getAllUsers();
}
