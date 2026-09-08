package br.com.fiap.susagenda.api.auth;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {
    @Test
    void shouldRegisterAndLoginPatient() {
        String email = "patient-" + System.nanoTime() + "@susmvp.com.br";
        String request = """
                {
                  "name": "Paciente Teste",
                  "cpf": "%s",
                  "email": "%s",
                  "password": "SenhaSegura123",
                  "role": "PATIENT"
                }
                """.formatted(String.valueOf(System.nanoTime()), email);

        given().contentType("application/json").body(request)
                .when().post("/api/v1/auth/register")
                .then().statusCode(201).body("role", is("PATIENT"));

        given().contentType("application/json")
                .body("{\"login\":\"" + email + "\",\"password\":\"SenhaSegura123\"}")
                .when().post("/api/v1/auth/login")
                .then().statusCode(200).body("tokenType", is("Bearer")).body("accessToken", notNullValue());
    }

    @Test
    void shouldRejectInvalidCredentials() {
        given().contentType("application/json")
                .body("{\"login\":\"unknown@susmvp.com.br\",\"password\":\"wrong\"}")
                .when().post("/api/v1/auth/login")
                .then().statusCode(401).body("code", is("INVALID_CREDENTIALS"));
    }
}
