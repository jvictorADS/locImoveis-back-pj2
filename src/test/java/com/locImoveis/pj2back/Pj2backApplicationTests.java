package com.locImoveis.pj2back;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Pj2backApplicationTests {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Test
	void contextLoads() {
	}

	@Test
	void protectedEndpointRequiresAuthentication() throws IOException, InterruptedException {
		HttpResponse<String> response = httpClient.send(
				HttpRequest.newBuilder(uri("/imoveis/1")).GET().build(),
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(response.statusCode()).isEqualTo(401);
	}

	@Test
	void publicRegisterEndpointValidatesPayload() throws IOException, InterruptedException {
		HttpResponse<String> response = httpClient.send(
				HttpRequest.newBuilder(uri("/auth/register"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{}"))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(response.statusCode()).isEqualTo(422);
	}

	@Test
	void loginEndpointGeneratesToken() throws IOException, InterruptedException {
		String email = "token.user@example.com";
		String senha = "Senha@123";

		HttpResponse<String> registerResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/auth/register"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("""
								{
								  "nomeCompleto": "Token User",
								  "cpf": "12345678901",
								  "telefone": "11999999999",
								  "email": "%s",
								  "senha": "%s",
								  "tipoUsuario": "LOCADOR"
								}
								""".formatted(email, senha)))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(registerResponse.statusCode()).isEqualTo(201);

		HttpResponse<String> loginResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/auth/login"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("""
								{
								  "email": "%s",
								  "senha": "%s"
								}
								""".formatted(email, senha)))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(loginResponse.statusCode()).isEqualTo(200);
		assertThat(loginResponse.body()).contains("\"token\":\"");
	}

	private URI uri(String path) {
		return URI.create("http://localhost:" + port + path);
	}
}
