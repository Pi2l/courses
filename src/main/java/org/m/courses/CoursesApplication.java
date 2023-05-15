package org.m.courses;

import org.m.courses.model.Role;
import org.m.courses.model.SpringUser;
import org.m.courses.model.User;
import org.m.courses.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@SpringBootApplication
public class CoursesApplication {

	private final UserService userService;
	public CoursesApplication(UserService userService) {
		this.userService = userService;
	}

	public static void main(String[] args) {
		SpringApplication.run(CoursesApplication.class, args);
	}

	@Bean
	CommandLineRunner runner() {
		return (args -> {
			User admin = new User();
			admin.setFirstName("firstName");
			admin.setLastName("lastName");
			admin.setPhoneNumber("398477937");
			admin.setLogin("admin");
			admin.setPassword("admin");
			admin.setRole(Role.ADMIN);

			loginAs( admin );
			userService.create( admin );
			logout();
		});
	}

	private void loginAs(User user) {
		SpringUser springUser = new SpringUser( user );
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				springUser, user.getLogin(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())) );

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void logout() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
