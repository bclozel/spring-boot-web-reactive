package org.springframework.test;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.web.client.reactive.HttpRequestBuilders.*;
import static reactor.core.test.TestSubscriber.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorHttpClientRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.reactive.WebClient;
import org.springframework.web.client.reactive.WebResponseExtractors;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ReactiveExampleApplicationTests {

	WebClient webClient;

	@Before
	public void setup() {
		this.webClient = new WebClient(new ReactorHttpClientRequestFactory());
	}

	@Test
	public void homeController() {

		Mono<ResponseEntity<BootStarter>> result = this.webClient
				.perform(get("http://localhost:8080/").accept(MediaType.APPLICATION_JSON))
				.extract(WebResponseExtractors.response(BootStarter.class));

		subscribe(result).awaitAndAssertNextValuesWith(
				response -> {
					assertThat(response.getStatusCode().value()).isEqualTo(200);
					assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);

					BootStarter starter = response.getBody();
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-reactive-web");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Reactive Web");
		}).assertComplete();
	}

}
