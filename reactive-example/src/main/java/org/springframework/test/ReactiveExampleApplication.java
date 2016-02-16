package org.springframework.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ReactiveWebApplicationContext;

@SpringBootApplication
public class ReactiveExampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ReactiveExampleApplication.class);
		app.setApplicationContextClass(ReactiveWebApplicationContext.class);
		app.run(args);
	}
}
