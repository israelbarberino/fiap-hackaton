package br.com.fiap.susagenda.api.medicalrecord;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import br.com.fiap.susagenda.support.AuthEnabledTestProfile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(AuthEnabledTestProfile.class)
class MedicalRecordAuthorizationTest {
    @Test
    void shouldRejectPatientCreatingMedicalRecordEntry() {
        String suffix = String.valueOf(System.nanoTime());
        String email = "record-" + suffix + "@susmvp.com.br";
        String cpf = suffix.substring(0, 11);

        given().contentType("application/json")
                .body("""
                        {
                          "name": "Paciente Registro",
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
                          "professionalId": "doctor-api-1",
                          "appointmentId": "appointment-1",
                          "evolutionType": "CONSULTA",
                          "description": "Paciente em bom estado geral.",
                          "observations": "Manter acompanhamento."
                        }
                        """)
                .when().post("/api/v1/patients/{id}/medical-record/entries", "patient-record-any")
                .then().statusCode(403)
                .body("code", is("FORBIDDEN"));
    }
}
