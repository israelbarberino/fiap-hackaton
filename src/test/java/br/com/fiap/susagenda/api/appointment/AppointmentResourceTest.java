package br.com.fiap.susagenda.api.appointment;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class AppointmentResourceTest {
    @Test
    void shouldReturnCreatedForValidAppointment() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "patientId": "patient-api-1",
                                                                                                        "professionalId": "doctor-api-2",
                                                                                                        "unitId": "unit-api-2",
                          "type": "CONSULTA",
                          "date": "2026-09-08",
                                                                                                        "time": "10:00"
                        }
                        """)
                .when()
                .post("/api/v1/appointments")
                .then()
                .statusCode(201)
                .body("date", is("2026-09-08"))
                                                                .body("time", is("10:00:00"));
    }

    @Test
    void shouldReturnConflictWithFiveSuggestedSlots() {
        String request = """
                {
                  "patientId": "patient-api-2",
                  "professionalId": "doctor-api-2",
                  "unitId": "unit-api-2",
                  "type": "CONSULTA",
                  "date": "2026-09-09",
                  "time": "08:00"
                }
                """;

        given().contentType("application/json").body(request)
                .when().post("/api/v1/appointments")
                .then().statusCode(201);

        given().contentType("application/json").body(request)
                .when().post("/api/v1/appointments")
                .then()
                .statusCode(409)
                .body("code", is("APPOINTMENT_TIME_UNAVAILABLE"))
                .body("message", is("Horário não disponível."))
                .body("suggestedSlots", hasSize(5));
    }
}
