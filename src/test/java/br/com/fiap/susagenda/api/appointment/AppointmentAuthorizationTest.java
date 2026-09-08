package br.com.fiap.susagenda.api.appointment;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import br.com.fiap.susagenda.support.AuthEnabledTestProfile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(AuthEnabledTestProfile.class)
class AppointmentAuthorizationTest {
    @Test
    void shouldRejectPatientSchedulingForAnotherPatient() {
        String suffix = String.valueOf(System.nanoTime());
        String email = "owner-" + suffix + "@susmvp.com.br";
        String cpf = suffix.substring(0, 11);

        given().contentType("application/json")
                .body("""
                        {
                          "name": "Paciente Dono",
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

        given().contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "patientId": "another-patient-id",
                          "professionalId": "doctor-api-1",
                          "unitId": "unit-api-1",
                          "type": "CONSULTA",
                          "date": "2026-09-08",
                          "time": "08:00"
                        }
                        """)
                .when().post("/api/v1/appointments")
                .then().statusCode(403)
                .body("code", is("FORBIDDEN"));
    }
}
