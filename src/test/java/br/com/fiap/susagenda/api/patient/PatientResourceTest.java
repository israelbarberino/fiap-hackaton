package br.com.fiap.susagenda.api.patient;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class PatientResourceTest {
    @Test
    void shouldCreateAndFindPatient() {
        String cpf = String.valueOf(System.nanoTime());
        String email = "maria-" + cpf + "@susmvp.com.br";

        given().contentType("application/json")
                .body("""
                        {
                          "name": "Maria da Silva",
                          "cpf": "%s",
                          "email": "%s",
                          "password": "SenhaSegura123",
                          "role": "PATIENT"
                        }
                        """.formatted(cpf, email))
                .when().post("/api/v1/auth/register")
                .then().statusCode(201);

        String token = given().contentType("application/json")
                .body("""
                        {
                          "login": "%s",
                          "password": "SenhaSegura123"
                        }
                        """.formatted(email))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200).extract().path("accessToken");

        String response = given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "name": "Maria da Silva",
                          "cpf": "%s",
                          "email": "%s",
                          "phone": "11999999999",
                          "address": "Rua A, 10",
                          "whatsappEnabled": false
                        }
                        """.formatted(cpf, email))
                .when().post("/api/v1/patients")
                .then().statusCode(201).body("id", notNullValue()).extract().path("id");

        given().header("Authorization", "Bearer " + token)
                .when().get("/api/v1/patients/{id}", response)
                .then().statusCode(200).body("cpf", is(cpf)).body("name", is("Maria da Silva"));
    }
}
