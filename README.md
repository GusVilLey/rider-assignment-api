# Rider Assignment API

REST API desarrollada con Spring Boot para administrar la asignación de riders a deliveries en una aplicación de delivery.

## Tecnologías

- Java 25
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA
- Jakarta Bean Validation
- PostgreSQL
- Maven
- JUnit 5 y Mockito
- Spring Boot Test y MockMvc
- H2 para pruebas

## Arquitectura

```text
src/main/java/com/softer/riderassignment
├── controller
│   └── RiderAssignmentController.java
├── exception
│   ├── BusinessRuleException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model
│   ├── AssignmentStatus.java
│   └── RiderAssignment.java
├── repository
│   └── RiderAssignmentRepository.java
├── service
│   └── RiderAssignmentService.java
└── RiderAssignmentApiApplication.java
```

## Modelo de datos

La entidad `RiderAssignment` representa la asignación de un rider a un delivery.

| Campo | Tipo | Descripción |
|---|---|---|
| id | Long | Identificador de la asignación |
| deliveryId | Long | Identificador del delivery |
| riderId | Long | Identificador del rider |
| status | AssignmentStatus | Estado de la asignación |
| assignedAt | LocalDateTime | Fecha y hora de asignación |

Estados disponibles:

- `ASSIGNED`: asignado al rider.
- `ACCEPTED`: asignación aceptada.
- `PICKED_UP`: pedido recogido.
- `DELIVERED`: pedido entregado.
- `CANCELLED`: asignación cancelada.

Regla de negocio incluida: no se permite registrar más de una asignación para el mismo `deliveryId`. Esta regla se implementa a nivel de servicio.

> Alcance: esta API administra la asignación, pero no consulta un sistema externo de riders ni de pedidos. Por ello, la existencia real del rider y del delivery deberá validarse cuando se integren esos servicios.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/assignments` | Listar asignaciones |
| GET | `/api/assignments/{id}` | Consultar una asignación |
| POST | `/api/assignments` | Crear una asignación |
| PUT | `/api/assignments/{id}` | Actualizar una asignación |
| DELETE | `/api/assignments/{id}` | Eliminar una asignación |

### Ejemplo: crear una asignación

`POST http://localhost:8080/api/assignments`

```json
{
  "deliveryId": 7001,
  "riderId": 301,
  "status": "ASSIGNED"
}
```

Respuesta esperada: `201 Created`.

### Ejemplo: actualizar una asignación

`PUT http://localhost:8080/api/assignments/1`

```json
{
  "deliveryId": 7001,
  "riderId": 302,
  "status": "ACCEPTED"
}
```

## Configuración de PostgreSQL

Crear una base de datos:

```sql
CREATE DATABASE delivery_db;
```

La aplicación utiliza estas variables de entorno:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Por defecto, la URL es `jdbc:postgresql://localhost:5432/delivery_db`, usuario `postgres` y contraseña `postgres`. Ajustar estos valores según el entorno.

## Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

## Ejecutar las pruebas

```bash
mvn test
```

Las pruebas utilizan H2 mediante el perfil `test`, por lo que no requieren conectarse a PostgreSQL.

Se incluyen:

- Pruebas unitarias del servicio con JUnit y Mockito.
- Pruebas del controlador con MockMvc.
- Pruebas de integración con servidor HTTP real y H2.

### Pruebas de integración

La clase `RiderAssignmentApiApplicationTest` levanta el contexto completo de Spring con
`@SpringBootTest(webEnvironment = RANDOM_PORT)` y el perfil `test`
(`src/test/resources/application-test.properties`, H2 en memoria). Cada prueba llama a la API
por HTTP con `RestTemplate` y verifica el estado real de la base con el repositorio.

| # | Prueba | Qué cubre |
|---|---|---|
| 1 | `test1_testGetAssignmentsWhenTableIsEmpty` | `GET /api/assignments` con la tabla vacía |
| 2 | `test2_testGetAssignmentsWhenTableIsNotEmpty` | `GET /api/assignments` con datos |
| 3 | `test3_testAddAssignment` | `POST /api/assignments` |
| 4 | `test4_testFindAssignmentById` | `GET /api/assignments/{id}` |
| 5 | `test5_testUpdateAssignment` | `PUT /api/assignments/{id}` |
| 6 | `test6_testDeleteAssignment` | `DELETE /api/assignments/{id}` |
| 7 | `test7_testFindAssignmentNotFound` | `404` cuando la asignación no existe |
| 8 | `test8_testRejectDuplicateDelivery` | `409` cuando el `deliveryId` ya tiene asignación |

**Regla de aislamiento:** cada prueba deja la base exactamente como la encontró. Los datos que
necesita se siembran con `@Sql(executionPhase = BEFORE_TEST_METHOD)` y se eliminan con
`@Sql(executionPhase = AFTER_TEST_METHOD)`. Así las pruebas se pueden ejecutar juntas, por
separado o en cualquier orden.

## Consideraciones

- Las tablas se generan/actualizan mediante Hibernate (`ddl-auto=update`) en el perfil principal.
- En pruebas se utiliza `create-drop`.
- El servicio aplica `@Transactional`.
- Los errores de recurso inexistente se responden con HTTP 404.
- Las reglas de negocio duplicadas se responden con HTTP 409.
- Los errores de validación se responden con HTTP 400.
