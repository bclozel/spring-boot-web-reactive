package sample.web.reactive;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReactiveSampleApplicationTests {

	private WebClient webClient;

	@LocalServerPort
	private int port;

	@Before
	public void setup() {
		this.webClient = WebClient.create("http://localhost:" + this.port);
	}

	@Test
	public void homeController() {

		Mono<BootStarter> result = this.webClient
				.get().uri("/")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.then(response -> response.bodyToMono(BootStarter.class));

		ScriptedSubscriber.<BootStarter>create()
				.consumeNextWith(starter -> {
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-web-reactive");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Web Reactive");
				})
				.expectComplete()
				.verify(result);
	}

	@Test
	public void starters() {
		Flux<BootStarter> result = this.webClient
				.get().uri("/starters")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.flatMap(response -> response.bodyToFlux(BootStarter.class));

		ScriptedSubscriber.<BootStarter>create()
				.consumeNextWith(starter -> {
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-web-reactive");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Web Reactive");
				})
				.consumeNextWith(starter -> {
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-web");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Web");
				})
				.consumeNextWith(starter -> {
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-websocket");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Websocket");
				})
				.expectComplete()
				.verify(result);
	}

	@Test
	public void customArgument() throws Exception {
		Mono<String> result = this.webClient
				.get().uri("/custom-arg?content=custom-value")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.then(response -> response.body(toMono(String.class)));

		ScriptedSubscriber.<String>create()
				.consumeNextWith(content -> {
					assertThat(content).contains("custom-value");
				})
				.expectComplete()
				.verify(result);
	}

	@Test
	public void staticResources() throws Exception {
		Mono<String> result = this.webClient
				.get().uri("/static/spring.txt")
				.accept(MediaType.TEXT_PLAIN)
				.exchange()
				.then(response -> response.body(toMono(String.class)));

		ScriptedSubscriber.<String>create()
				.consumeNextWith(content -> {
					assertThat(content).contains("Spring Framework");
				})
				.expectComplete()
				.verify(result);
	}

}