package com.chaean.teamchatsa.domain.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGeneratorTest {

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Test
	void encyptPassword() {
		String rawPassword = "1234";
		String encrypted = encoder.encode(rawPassword);
		System.out.println("Encrypted password: " + encrypted);
	}

}
