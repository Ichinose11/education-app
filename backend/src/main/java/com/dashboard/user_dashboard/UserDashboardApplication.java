package com.dashboard.user_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.dashboard.user_dashboard.repository")
public class UserDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserDashboardApplication.class, args);
	}

}
