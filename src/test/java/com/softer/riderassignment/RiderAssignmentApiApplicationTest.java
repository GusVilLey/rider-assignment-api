package com.softer.riderassignment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

}
