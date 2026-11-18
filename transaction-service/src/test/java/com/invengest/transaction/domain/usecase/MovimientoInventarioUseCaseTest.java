package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioUseCaseTest {

    @Mock
    private MovimientoInventarioGateway movimientoInventarioGateway;
    @Mock
    private TipoMovimientoGateway tipoMovimientoGateway;
    @Mock
    private CatalogGateway catalogGateway;

    @InjectMocks
    private MovimientoInventarioUseCase useCase;

    @Test
    @DisplayName("Debe sincronizar stock en catálogo para ajustes negativos y positivos y consultar Kardex")
    void debeSincronizarStockYConsultarKardex() {
        // Arrange
        TipoMovimiento perdida = new TipoMovimiento(3, TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO);
        TipoMovimiento ingreso = new TipoMovimiento(1, TipoMovimiento.MOV_INGRESO_COMPRA);

        when(tipoMovimientoGateway.findById(3)).thenReturn(Optional.of(perdida));
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_INGRESO_COMPRA)).thenReturn(Optional.of(ingreso));
        when(movimientoInventarioGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoInventarioGateway.findByIdProducto("PROD-01")).thenReturn(List.of(new MovimientoInventario()));
        when(movimientoInventarioGateway.findAll()).thenReturn(List.of(new MovimientoInventario()));
        when(movimientoInventarioGateway.findByFechaBetween(any(), any())).thenReturn(List.of(new MovimientoInventario()));

        MovimientoInventario ajusteNegativo = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(3, null), 2, null, "Daño en estantería");
        MovimientoInventario ajustePositivoPorNombre = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(null, TipoMovimiento.MOV_INGRESO_COMPRA), 10, null, "Compra factura #12");

        // Act
        MovimientoInventario resNeg = useCase.registrarAjuste(ajusteNegativo);
        MovimientoInventario resPos = useCase.registrarMovimiento(ajustePositivoPorNombre);
        List<MovimientoInventario> kardexProd = useCase.obtenerKardexPorProducto("PROD-01");
        List<MovimientoInventario> todos = useCase.listarTodos();
        List<MovimientoInventario> porFechas = useCase.filtrarPorFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO, resNeg.getTipoMovimiento().getNombre());
        assertEquals(TipoMovimiento.MOV_INGRESO_COMPRA, resPos.getTipoMovimiento().getNombre());
        assertEquals(1, kardexProd.size());
        assertEquals(1, todos.size());
        assertEquals(1, porFechas.size());
    }

    @Test
    @DisplayName("Debe compensar stock en catálogo si falla el guardado en BD y validar restricciones")
    void debeCompensarSiFallaGuardadoYValidarRestricciones() {
        // Arrange
        TipoMovimiento perdida = new TipoMovimiento(3, TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO);
        TipoMovimiento ingreso = new TipoMovimiento(1, TipoMovimiento.MOV_INGRESO_COMPRA);
        TipoMovimiento salidaVenta = new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA);
        TipoMovimiento desconocido = new TipoMovimiento(9, "Otro");

        when(tipoMovimientoGateway.findById(3)).thenReturn(Optional.of(perdida));
        when(tipoMovimientoGateway.findById(1)).thenReturn(Optional.of(ingreso));
        when(tipoMovimientoGateway.findById(2)).thenReturn(Optional.of(salidaVenta));
        when(tipoMovimientoGateway.findById(9)).thenReturn(Optional.of(desconocido));
        when(tipoMovimientoGateway.findById(99)).thenReturn(Optional.empty());
        when(tipoMovimientoGateway.findByNombre("Inexistente")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Fallo BD")).when(movimientoInventarioGateway).save(any());

        MovimientoInventario ajusteNegativo = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(3, null), 2, null, "Daño");
        MovimientoInventario ajustePositivo = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(1, null), 5, null, "Compra");
        MovimientoInventario ajusteProhibido = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(2, null), 1, null, "No permitido");
        MovimientoInventario ajusteSinOperacion = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(9, null), 1, null, "Otro");
        MovimientoInventario ajusteIdNoExiste = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(99, null), 1, null, "No existe");
        MovimientoInventario ajusteNombreNoExiste = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(null, "Inexistente"), 1, null, "No existe");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> useCase.registrarAjuste(ajusteNegativo));
        assertThrows(RuntimeException.class, () -> useCase.registrarAjuste(ajustePositivo));
        verify(catalogGateway, times(2)).descontarStockBatch(anyList());
        verify(catalogGateway, times(2)).reintegrarStockBatch(anyList());

        assertThrows(ReglaNegocioException.class, () -> useCase.registrarAjuste(null));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrarAjuste(ajusteProhibido));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrarAjuste(ajusteSinOperacion));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.registrarAjuste(ajusteIdNoExiste));
        assertThrows(RecursoNoEncontradoException.class, () -> useCase.registrarAjuste(ajusteNombreNoExiste));
        assertThrows(ReglaNegocioException.class, () -> useCase.obtenerKardexPorProducto(" "));
        assertThrows(ReglaNegocioException.class, () -> useCase.obtenerKardexPorProducto(null));
        assertThrows(ReglaNegocioException.class, () -> useCase.filtrarPorFechas(null, LocalDateTime.now()));
        assertThrows(ReglaNegocioException.class, () -> useCase.filtrarPorFechas(LocalDateTime.now(), null));
        assertThrows(ReglaNegocioException.class, () -> useCase.filtrarPorFechas(LocalDateTime.now(), LocalDateTime.now().minusDays(1)));
    }
}
