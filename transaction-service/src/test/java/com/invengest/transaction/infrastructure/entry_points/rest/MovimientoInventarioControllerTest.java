package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.usecase.MovimientoInventarioUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.MovimientoInventarioRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MovimientoInventarioResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoMovimientoResponse;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioControllerTest {

    @Mock
    private MovimientoInventarioUseCase movimientoInventarioUseCase;
    @Mock
    private MovimientoInventarioRestMapper mapper;

    @InjectMocks
    private MovimientoInventarioController controller;

    @Test
    @DisplayName("Debe registrar ajustes y movimientos de Kardex y consultarlos por producto o fechas")
    void debeRegistrarYConsultarMovimientosKardex() {
        // Arrange
        MovimientoInventario mov = new MovimientoInventario();
        MovimientoInventarioResponse movResp = new MovimientoInventarioResponse(
                1, "PROD-01", 1, new TipoMovimientoResponse(3, "Ajuste por Pérdida/Daño"),
                2, LocalDateTime.now(), "Rotura"
        );
        MovimientoInventarioRequest req = new MovimientoInventarioRequest("PROD-01", 1, 3, 2, "Rotura");

        when(mapper.toDomain(req)).thenReturn(mov);
        when(movimientoInventarioUseCase.registrarAjuste(mov)).thenReturn(mov);
        when(movimientoInventarioUseCase.registrarMovimiento(mov)).thenReturn(mov);
        when(movimientoInventarioUseCase.obtenerKardexPorProducto("PROD-01")).thenReturn(List.of(mov));
        when(movimientoInventarioUseCase.listarTodos()).thenReturn(List.of(mov));
        when(movimientoInventarioUseCase.filtrarPorFechas(any(), any())).thenReturn(List.of(mov));
        when(mapper.toResponse(mov)).thenReturn(movResp);

        // Act
        ResponseEntity<MovimientoInventarioResponse> rAjuste = controller.registrarAjuste(req);
        ResponseEntity<MovimientoInventarioResponse> rMov = controller.registrarMovimiento(req);
        ResponseEntity<List<MovimientoInventarioResponse>> rProd = controller.obtenerKardexPorProducto("PROD-01");
        ResponseEntity<List<MovimientoInventarioResponse>> rTodos = controller.listarMovimientos(null, null);
        ResponseEntity<List<MovimientoInventarioResponse>> rSoloInicio = controller.listarMovimientos(LocalDateTime.now(), null);
        ResponseEntity<List<MovimientoInventarioResponse>> rFechas = controller.listarMovimientos(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(HttpStatus.CREATED, rAjuste.getStatusCode());
        assertEquals(HttpStatus.CREATED, rMov.getStatusCode());
        assertEquals(1, rProd.getBody().size());
        assertEquals(1, rTodos.getBody().size());
        assertEquals(1, rSoloInicio.getBody().size());
        assertEquals(1, rFechas.getBody().size());
    }
}
