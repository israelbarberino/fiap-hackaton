package br.com.fiap.susagenda.api.medicalrecord;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class MedicalRecordResourceTest {
    @Test
    void shouldAppendAndReadMedicalRecordHistory() {
        String patientId = "patient-record-" + System.nanoTime();
        given().contentType("application/json")
                .body("""
                        {
                          "professionalId": "doctor-1",
                          "appointmentId": "appointment-1",
                          "evolutionType": "CONSULTA",
                          "description": "Paciente em bom estado geral.",
                          "observations": "Manter acompanhamento."
                        }
                        """)
                .when().post("/api/v1/patients/{id}/medical-record/entries", patientId)
                .then().statusCode(201).body("patientId", is(patientId));

        given().when().get("/api/v1/patients/{id}/medical-record/history", patientId)
                .then().statusCode(200).body("", hasSize(1));
    }

    @Test
    void shouldRejectEmptyDescription() {
        given().contentType("application/json")
                .body("{\"professionalId\":\"doctor-1\",\"description\":\"\"}")
                .when().post("/api/v1/patients/patient-invalid/medical-record/entries")
                .then().statusCode(400).body("code", is("INVALID_MEDICAL_RECORD_ENTRY"));
    }
}
