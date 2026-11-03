package com.locImoveis.pj2back;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Pj2backApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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

	@Test
	void tenantRegistrationPersistsAndAppearsInLandlordListing() throws IOException, InterruptedException {
		String suffix = UUID.randomUUID().toString().replace("-", "");
		String landlordEmail = "landlord-" + suffix + "@example.com";
		String tenantEmail = "tenant-" + suffix + "@example.com";
		String landlordPassword = "Senha@123";

		HttpResponse<String> registerResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/auth/register"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("""
								{
								  "nomeCompleto": "Proprietário Teste",
								  "cpf": "%s",
								  "telefone": "11999999999",
								  "email": "%s",
								  "senha": "%s",
								  "tipoUsuario": "LOCADOR"
								}
								""".formatted(suffix.substring(0, 11), landlordEmail, landlordPassword)))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertThat(registerResponse.statusCode()).isEqualTo(201);

		HttpResponse<String> loginResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/auth/login"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("""
								{"email":"%s","senha":"%s"}
								""".formatted(landlordEmail, landlordPassword)))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertThat(loginResponse.statusCode()).isEqualTo(200);
		String token = loginResponse.body().replaceAll("^.*\"token\":\"([^\"]+)\".*$", "$1");

		HttpResponse<String> tenantResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/usuarios"))
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + token)
						.POST(HttpRequest.BodyPublishers.ofString("""
								{
								  "nomeCompleto": "Locatário Teste",
								  "cpf": "%s",
								  "telefone": "11888888888",
								  "email": "%s",
								  "tipoUsuario": "LOCATARIO"
								}
								""".formatted(suffix.substring(11, 22), tenantEmail)))
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertThat(tenantResponse.statusCode()).isEqualTo(201);
		String tenantId = tenantResponse.body().replaceAll("^.*\"idUsuario\":(\\d+).*$", "$1");
		int associationCount = jdbcTemplate.queryForObject("""
				select count(*)
				from locador_locatarios ll
				join usuarios locador on locador.id_usuario = ll.locador_id
				join usuarios locatario on locatario.id_usuario = ll.locatario_id
				where locador.email = ? and locatario.email = ?
				""", Integer.class, landlordEmail, tenantEmail);
		assertThat(associationCount).isEqualTo(1);

		HttpResponse<String> tenantByIdResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/usuarios/" + tenantId))
						.header("Authorization", "Bearer " + token)
						.GET()
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertThat(tenantByIdResponse.statusCode()).isEqualTo(200);
		assertThat(tenantByIdResponse.body()).contains(tenantEmail);

		HttpResponse<String> listingResponse = httpClient.send(
				HttpRequest.newBuilder(uri("/usuarios/tipo/LOCATARIO"))
						.header("Authorization", "Bearer " + token)
						.GET()
						.build(),
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(listingResponse.statusCode()).isEqualTo(200);
		assertThat(listingResponse.body()).contains(tenantEmail, "\"tipoUsuario\":\"LOCATARIO\"");
	}

	private URI uri(String path) {
		return URI.create("http://localhost:" + port + path);
	}
}
