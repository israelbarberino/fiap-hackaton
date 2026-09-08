package br.com.fiap.susagenda.api.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import br.com.fiap.susagenda.support.AuthEnabledTestProfile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(AuthEnabledTestProfile.class)
class AuthResourceAuthorizationTest {
    @Test
    void shouldRejectPublicRegistrationForDoctor() {
        given().contentType("application/json")
                .body("""
                        {
                          "name": "Dr. Bloqueado",
                          "cpf": "12345678901",
                          "email": "doctor-blocked@susmvp.com.br",
                          "password": "SenhaSegura123",
                          "role": "DOCTOR"
                        }
                        """)
                .when().post("/api/v1/auth/register")
                .then().statusCode(403)
                .body("code", is("PUBLIC_REGISTRATION_FORBIDDEN"));
    }
}
