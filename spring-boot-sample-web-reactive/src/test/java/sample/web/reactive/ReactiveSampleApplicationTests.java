package sample.web.reactive;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.reactive.ClientRequest;
import org.springframework.web.client.reactive.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.codec.BodyExtractors.toMono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReactiveSampleApplicationTests {

	private WebClient webClient;

	@LocalServerPort
	private int port;

	@Before
	public void setup() {
		this.webClient = WebClient.create(new ReactorClientHttpConnector());
	}

	@Test
	public void homeController() {

		ClientRequest<Void> request = ClientRequest.GET("http://localhost:{port}/", this.port)
				.accept(MediaType.APPLICATION_JSON).build();

		Mono<BootStarter> result = this.webClient
				.exchange(request)
				.then(response -> response.body(toMono(BootStarter.class)));

		ScriptedSubscriber.<BootStarter>create()
				.consumeNextWith(starter -> {
					assertThat(starter.getId()).isEqualTo("spring-boot-starter-web-reactive");
					assertThat(starter.getLabel()).isEqualTo("Spring Boot Web Reactive");
				})
				.expectComplete()
				.verify(result);
	}

	@Test
	public void customArgument() throws Exception {
		ClientRequest<Void> request = ClientRequest
				.GET("http://localhost:{port}/custom-arg?content=custom-value", this.port)
				.accept(MediaType.APPLICATION_JSON).build();

		Mono<String> result = this.webClient
				.exchange(request)
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
		ClientRequest<Void> request = ClientRequest
				.GET("http://localhost:{port}/static/spring.txt", this.port)
				.accept(MediaType.TEXT_PLAIN).build();

		Mono<String> result = this.webClient
				.exchange(request)
				.then(response -> response.body(toMono(String.class)));

		ScriptedSubscriber.<String>create()
				.consumeNextWith(content -> {
					assertThat(content).contains("Spring Framework");
				})
				.expectComplete()
				.verify(result);
	}

}