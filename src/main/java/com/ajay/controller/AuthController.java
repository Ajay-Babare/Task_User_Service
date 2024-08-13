package com.ajay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.ajay.config.JwtProvider;
import com.ajay.model.User;
import com.ajay.repository.UserRepository;
import com.ajay.request.LoginRequest;
import com.ajay.response.AuthResponse;
import com.ajay.service.CustomerUserServiceImplementation;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private CustomerUserServiceImplementation customUserDetails;
	
	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> createUserHandler(
			@RequestBody User user) throws Exception{
		String email = user.getEmail();
		String fullname = user.getFullname();
		String password = user.getPassword();
		
		String role = user.getRole();
		
		User isEmailExist = userRepository.findByEmail(email);
		if(isEmailExist != null) {
			throw new Exception("Email Is Already Used With Another Account");
		}
		
		//create new user
		User createUser = new User();
		createUser.setEmail(email);
		createUser.setFullname(fullname);
		createUser.setRole(role);
		createUser.setPassword(passwordEncoder.encode(password));
		
		User savedUser = userRepository.save(createUser);
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String token = JwtProvider.generateToken(authentication);
		
		AuthResponse authResponse = new AuthResponse();
		authResponse.setJwt(token);
		authResponse.setMessage("Register Sucess");
		authResponse.setStatus(true);
		
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
		
	}
	
	@PostMapping("/signin")
	public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest){
		
		String username = loginRequest.getEmail();
		String password = loginRequest.getPassword();
		
		System.out.println(username+"--------"+password);
		
		Authentication authentication = authenticate(username, password);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		String token = JwtProvider.generateToken(authentication);
		AuthResponse authResponse = new AuthResponse();
		
		authResponse.setMessage("Login Success");
		authResponse.setJwt(token);
		authResponse.setStatus(true);
		
		return new ResponseEntity<>(authResponse,HttpStatus.OK);
		
	}
	
	private Authentication authenticate(String username, String password) {
		UserDetails userDetails = customUserDetails.loadUserByUsername(username);
		
		System.out.println("sign in userDetails - "+userDetails);
		
		if(userDetails == null) {
			System.out.println("sign in userDetails - null "+userDetails);
			throw new BadCredentialsException("Invalid username or password");
			
		}
		if(!passwordEncoder.matches(password, userDetails.getPassword())) {
			System.out.println("sign in userDetails - password not match "+userDetails);
			throw new BadCredentialsException("Invalid username or password");
		}
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}
