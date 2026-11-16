# InvenGest_POS — Sistema POS e Inventario para Papelería (Arquitectura de Microservicios)

Monorepo oficial de **InvenGest_POS** implementado con **Java 21**, **Spring Boot 4.1.1**, **Spring Cloud OpenFeign**, **Spring Security + JWT** y **Clean Architecture** estricta de 3 capas (`domain`, `application`, `infrastructure`).

## Arquitectura y Componentes

| Componente | Puerto | Esquema BD | Responsabilidad |
|---|---|---|---|
| `api-gateway` | `8080` | N/A | Enrutamiento centralizado, CORS, validación temprana JWT y bloqueo de `/internal/**`. |
| `auth-service` | `8081` | `auth` | Autenticación JWT, RBAC (`Administrador`, `Vendedor`), ciclo de vida de usuarios, gestión de contraseñas y directorio interno de contacto. |
| `catalog-service` | `8082` | `catalog` | Gestión de productos, servicios, proveedores, validación/descuento/reintegro batch de stock, mejor proveedor y conversión a USD vía **DolarAPI (TRM)**. |
| `transaction-service` | `8083` | `transaction` | Registro de ventas POS con compensación, anulaciones, comprobantes, arqueo/cierre de caja con **API Colombia Festivos** y ajustes de Kardex. |
| `analytics-service` | `8084` | `analytics` | Proyecciones CQRS de ventas, ranking de productos, alertas de stock, reportes Excel/PDF, ventas en USD y análisis de estacionalidad. |
| `notification-service` | `8085` | Stateless | Envío asíncrono (`@Async`) de correos transaccionales y alertas mediante **Spring Boot Mail (`JavaMailSender`)**. |

## Reglas de Ingeniería
1. **Clean Architecture:** Cero dependencias de Spring, JPA, Jackson o Feign dentro de `domain/`.
2. **Base de Datos Única por Esquemas (`db/init.sql`):** Una sola instancia PostgreSQL (`invengest_pos`) con los esquemas `auth`, `catalog`, `transaction` y `analytics`, sin llaves foráneas cruzadas entre esquemas.
3. **Defensa en Profundidad:** Validación JWT en cada microservicio + `X-Internal-Api-Key` para comunicación servicio a servicio (`/internal/**`).
4. **Calidad y Rendimiento:** Cobertura de pruebas unitarias **JaCoCo >= 80 %** (patrón AAA) y latencia objetivo **< 500 ms**.

## Ejecución de Pruebas por Microservicio
```bash
cd auth-service
./mvnw clean verify
```
