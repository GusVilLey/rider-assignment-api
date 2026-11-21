package com.softer.riderassignment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.RiderAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas de integración de la API de asignación de riders.
 *
 * A diferencia de las pruebas unitarias (Mockito) y de controlador
 * (@WebMvcTest + MockMvc), esta clase levanta el contexto completo de
 * Spring, arranca Tomcat en un puerto aleatorio y usa una base H2 real.
 * Por eso cada prueba habla con la API por HTTP con RestTemplate y, además,
 * verifica el estado de la base directamente con el repositorio.
 *
 * Regla de aislamiento: cada prueba deja la base exactamente como la
 * encontró. Lo que se siembra con @Sql(BEFORE_TEST_METHOD) se borra con
 * @Sql(AFTER_TEST_METHOD).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class RiderAssignmentApiApplicationTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private RiderAssignmentRepository repository;

    @BeforeAll
    static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    void setUp() {
        baseUrl = baseUrl.concat(":").concat(port + "").concat("/api/assignments");
    }

    // ------------------------------------------------------------------
    // 1. GET /api/assignments con la tabla vacía
    // ------------------------------------------------------------------
    @Test
    @DisplayName("should get 0 assignments when table is empty")
    void test1_testGetAssignmentsWhenTableIsEmpty() {
        List<?> response = restTemplate.getForObject(baseUrl, List.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(0, response.size()),
                () -> assertEquals(0, repository.findAll().size())
        );
    }

    // ------------------------------------------------------------------
    // 2. GET /api/assignments con la tabla poblada
    // ------------------------------------------------------------------
    @Test
    @DisplayName("should get all assignments when table is not empty")
    @Sql(
            statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) "
                    + "VALUES (1, 7001, 301, 'ASSIGNED', '2026-11-21 10:00:00')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void test2_testGetAssignmentsWhenTableIsNotEmpty() {
        List<?> response = restTemplate.getForObject(baseUrl, List.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.size()),
                () -> assertEquals(1, repository.findAll().size())
        );
    }

    // ------------------------------------------------------------------
    // 3. POST /api/assignments
    // ------------------------------------------------------------------
    @Test
    @DisplayName("should register a new assignment")
    @Sql(
            statements = "DELETE FROM rider_assignments",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void test3_testAddAssignment() {
        RiderAssignment request = new RiderAssignment(
                null, 7001L, 301L, AssignmentStatus.ASSIGNED, null);

        RiderAssignment response = restTemplate.postForObject(
                baseUrl, request, RiderAssignment.class);

        assertAll(
                () -> assertNotNull(response),
                () -> assertNotNull(response.getId()),
                () -> assertEquals(7001L, response.getDeliveryId()),
                () -> assertEquals(301L, response.getRiderId()),
                () -> assertEquals(AssignmentStatus.ASSIGNED, response.getStatus()),
                () -> assertNotNull(response.getAssignedAt()),
                () -> assertEquals(1, repository.findAll().size())
        );
    }

}
