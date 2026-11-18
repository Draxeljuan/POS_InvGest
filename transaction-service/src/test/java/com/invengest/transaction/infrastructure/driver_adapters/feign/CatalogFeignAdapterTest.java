package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogProductoFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogServicioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockValidadoFeignDTO;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogFeignAdapterTest {

    @Mock
    private CatalogFeignClient catalogClient;

    @InjectMocks
    private CatalogFeignAdapter catalogAdapter;

    @Test
    @DisplayName("Debe validar, descontar y reintegrar stock batch y consultar productos y servicios")
    void debeOperarConCatalogFeignClientExitosamente() {
        // Arrange
        when(catalogClient.validarStockBatch(anyList()))
                .thenReturn(List.of(new ItemStockValidadoFeignDTO("P1", "Cuaderno", new BigDecimal("5000.00"), 10, 2, true)))
                .thenReturn(null);
        when(catalogClient.obtenerProductoPorId("P1"))
                .thenReturn(new CatalogProductoFeignDTO("P1", "Cuaderno", new BigDecimal("5000.00"), 10, 2))
                .thenReturn(null);
        when(catalogClient.obtenerServicioPorId(1))
                .thenReturn(new CatalogServicioFeignDTO(1, "Copia", "Desc", new BigDecimal("500.00")))
                .thenReturn(null);

        // Act & Assert
        assertTrue(catalogAdapter.validarStockBatch(null).isEmpty());
        assertTrue(catalogAdapter.validarStockBatch(List.of()).isEmpty());
        assertEquals(1, catalogAdapter.validarStockBatch(List.of(new SolicitudStockItem("P1", 2))).size());
        assertTrue(catalogAdapter.validarStockBatch(List.of(new SolicitudStockItem("P1", 2))).isEmpty());

        assertDoesNotThrow(() -> catalogAdapter.descontarStockBatch(null));
        assertDoesNotThrow(() -> catalogAdapter.descontarStockBatch(List.of()));
        assertDoesNotThrow(() -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 2))));
        assertDoesNotThrow(() -> catalogAdapter.reintegrarStockBatch(null));
        assertDoesNotThrow(() -> catalogAdapter.reintegrarStockBatch(List.of()));
        assertDoesNotThrow(() -> catalogAdapter.reintegrarStockBatch(List.of(new SolicitudStockItem("P1", 2))));

        assertTrue(catalogAdapter.obtenerProductoPorId("P1").isPresent());
        assertTrue(catalogAdapter.obtenerProductoPorId("P1").isEmpty());
        assertTrue(catalogAdapter.obtenerServicioPorId(1).isPresent());
        assertTrue(catalogAdapter.obtenerServicioPorId(1).isEmpty());
    }

    @Test
    @DisplayName("Debe traducir excepciones Feign de catalog-service a excepciones de dominio")
    void debeTraducirExcepcionesFeignADominio() {
        // Arrange
        Request req = Request.create(Request.HttpMethod.POST, "/internal/productos/descontar-stock", Map.of(), null, StandardCharsets.UTF_8, null);
        when(catalogClient.obtenerProductoPorId("404")).thenThrow(new FeignException.NotFound("404", req, null, null));
        when(catalogClient.obtenerProductoPorId("503")).thenThrow(new FeignException.ServiceUnavailable("503", req, null, null));
        when(catalogClient.obtenerServicioPorId(404)).thenThrow(new FeignException.NotFound("404", req, null, null));
        when(catalogClient.obtenerServicioPorId(503)).thenThrow(new FeignException.ServiceUnavailable("503", req, null, null));
        when(catalogClient.validarStockBatch(anyList())).thenThrow(new FeignException.ServiceUnavailable("503", req, null, null));
        doThrow(new FeignException.ServiceUnavailable("503", req, null, null)).when(catalogClient).reintegrarStockBatch(anyList());

        // Act & Assert
        assertTrue(catalogAdapter.obtenerProductoPorId("404").isEmpty());
        assertThrows(ServicioExternoNoDisponibleException.class, () -> catalogAdapter.obtenerProductoPorId("503"));
        assertTrue(catalogAdapter.obtenerServicioPorId(404).isEmpty());
        assertThrows(ServicioExternoNoDisponibleException.class, () -> catalogAdapter.obtenerServicioPorId(503));
        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> catalogAdapter.validarStockBatch(List.of(new SolicitudStockItem("P1", 1))));
        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> catalogAdapter.reintegrarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.NotFound("404", req, "No existe".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(RecursoNoEncontradoException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.BadRequest("400", req, "Stock insuficiente".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(StockInsuficienteException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.BadRequest("400", req, "Otra regla".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(ReglaNegocioException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.ServiceUnavailable("503", req, "Down".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));
    }
}
