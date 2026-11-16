package com.invengest.transaction;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import com.invengest.transaction.domain.usecase.ClienteUseCase;
import com.invengest.transaction.domain.usecase.ConsultarVentaUseCase;
import com.invengest.transaction.domain.usecase.MetodoPagoUseCase;
import com.invengest.transaction.domain.usecase.MovimientoInventarioUseCase;
import com.invengest.transaction.domain.usecase.TipoDocumentoUseCase;
import com.invengest.transaction.domain.usecase.TipoMovimientoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class KardexYConsultasUseCaseTest {

    @Mock
    private MovimientoInventarioGateway movimientoInventarioGateway;
    @Mock
    private TipoMovimientoGateway tipoMovimientoGateway;
    @Mock
    private CatalogGateway catalogGateway;
    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private ClienteGateway clienteGateway;
    @Mock
    private TipoDocumentoGateway tipoDocumentoGateway;
    @Mock
    private MetodoPagoGateway metodoPagoGateway;

    @Test
    void movimientoInventarioUseCaseSincronizaStockEnCatalogParaAjustesNegativosYPositivosYCompensaSiFalla() {
        // Arrange
        MovimientoInventarioUseCase useCase = new MovimientoInventarioUseCase(
                movimientoInventarioGateway, tipoMovimientoGateway, catalogGateway);

        TipoMovimiento perdida = new TipoMovimiento(3, TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO);
        TipoMovimiento ingreso = new TipoMovimiento(1, TipoMovimiento.MOV_INGRESO_COMPRA);
        TipoMovimiento salidaVenta = new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA);

        when(tipoMovimientoGateway.findById(3)).thenReturn(Optional.of(perdida));
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_INGRESO_COMPRA)).thenReturn(Optional.of(ingreso));
        when(tipoMovimientoGateway.findById(2)).thenReturn(Optional.of(salidaVenta));
        when(movimientoInventarioGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoInventarioGateway.findByIdProducto("PROD-01")).thenReturn(List.of(new MovimientoInventario()));
        when(movimientoInventarioGateway.findAll()).thenReturn(List.of(new MovimientoInventario()));
        when(movimientoInventarioGateway.findByFechaBetween(any(), any())).thenReturn(List.of(new MovimientoInventario()));

        MovimientoInventario ajusteNegativo = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(3, null), 2, null, "Daño en estantería");
        MovimientoInventario ajustePositivoPorNombre = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(null, TipoMovimiento.MOV_INGRESO_COMPRA), 10, null, "Compra factura #12");
        MovimientoInventario ajusteProhibido = new MovimientoInventario(
                null, "PROD-01", 1, new TipoMovimiento(2, null), 1, null, "No permitido");

        // Act
        MovimientoInventario resNeg = useCase.registrarAjuste(ajusteNegativo);
        MovimientoInventario resPos = useCase.registrarMovimiento(ajustePositivoPorNombre);

        // Assert
        assertEquals(TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO, resNeg.getTipoMovimiento().getNombre());
        assertEquals(TipoMovimiento.MOV_INGRESO_COMPRA, resPos.getTipoMovimiento().getNombre());
        assertEquals(1, useCase.obtenerKardexPorProducto("PROD-01").size());
        assertEquals(1, useCase.listarTodos().size());
        assertEquals(1, useCase.filtrarPorFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now()).size());
        assertThrows(ReglaNegocioException.class, () -> useCase.obtenerKardexPorProducto(" "));
        assertThrows(ReglaNegocioException.class, () -> useCase.filtrarPorFechas(null, LocalDateTime.now()));
        assertThrows(ReglaNegocioException.class, () -> useCase.filtrarPorFechas(LocalDateTime.now(), LocalDateTime.now().minusDays(1)));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrarAjuste(null));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrarAjuste(ajusteProhibido));

        // Si falla el guardado en BD después de descontar/reintegrar stock, debe compensar en catalog
        doThrow(new RuntimeException("Fallo BD")).when(movimientoInventarioGateway).save(any());
        assertThrows(RuntimeException.class, () -> useCase.registrarAjuste(ajusteNegativo));
        assertThrows(RuntimeException.class, () -> useCase.registrarAjuste(ajustePositivoPorNombre));
        verify(catalogGateway, times(2)).reintegrarStockBatch(anyList());
        verify(catalogGateway, times(2)).descontarStockBatch(anyList());
    }

    @Test
    void casosDeUsoDeConsultaVentaClienteMetodoPagoTipoDocumentoYTipoMovimientoOperanCorrectamente() {
        // Arrange
        ConsultarVentaUseCase consultarVentaUseCase = new ConsultarVentaUseCase(ventaGateway);
        ClienteUseCase clienteUseCase = new ClienteUseCase(clienteGateway, tipoDocumentoGateway);
        MetodoPagoUseCase metodoPagoUseCase = new MetodoPagoUseCase(metodoPagoGateway);
        TipoDocumentoUseCase tipoDocumentoUseCase = new TipoDocumentoUseCase(tipoDocumentoGateway);
        TipoMovimientoUseCase tipoMovimientoUseCase = new TipoMovimientoUseCase(tipoMovimientoGateway);

        Venta venta = new Venta();
        venta.setIdVenta(1);
        when(ventaGateway.findById(1)).thenReturn(Optional.of(venta));
        when(ventaGateway.findAll()).thenReturn(List.of(venta));
        when(ventaGateway.findByIdUsuario(1)).thenReturn(List.of(venta));
        when(ventaGateway.findByFechaBetween(any(), any())).thenReturn(List.of(venta));

        Cliente nuevoCliente = new Cliente(1, new TipoDocumento(1, null), "9001", "Pedro", null, "Ruiz", null, "300", "p@test.com");
        when(clienteGateway.findByDocumentoIdentidad("9001")).thenReturn(Optional.empty(), Optional.of(nuevoCliente));
        when(clienteGateway.findById(1)).thenReturn(Optional.of(nuevoCliente));
        when(clienteGateway.findAll()).thenReturn(List.of(nuevoCliente));
        when(tipoDocumentoGateway.findById(1)).thenReturn(Optional.of(new TipoDocumento(1, "CC")));
        when(tipoDocumentoGateway.findAll()).thenReturn(List.of(new TipoDocumento(1, "CC")));
        when(clienteGateway.save(any())).thenReturn(nuevoCliente);
        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(new MetodoPago(1, "Efectivo")));
        when(metodoPagoGateway.findAll()).thenReturn(List.of(new MetodoPago(1, "Efectivo")));
        when(tipoMovimientoGateway.findById(1)).thenReturn(Optional.of(new TipoMovimiento(1, "Ingreso por Compra")));
        when(tipoMovimientoGateway.findAll()).thenReturn(List.of(new TipoMovimiento(1, "Ingreso por Compra")));

        // Act & Assert
        assertEquals(1, consultarVentaUseCase.obtenerPorId(1).getIdVenta());
        assertEquals(1, consultarVentaUseCase.listarTodas().size());
        assertEquals(1, consultarVentaUseCase.listarPorUsuario(1).size());
        assertEquals(1, consultarVentaUseCase.filtrarPorFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now()).size());
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.obtenerPorId(null));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.listarPorUsuario(null));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.filtrarPorFechas(null, LocalDateTime.now()));
        assertThrows(ReglaNegocioException.class,
                () -> consultarVentaUseCase.filtrarPorFechas(LocalDateTime.now(), LocalDateTime.now().minusDays(1)));

        assertEquals("Pedro Ruiz", clienteUseCase.registrar(nuevoCliente).getNombreCompleto());
        assertEquals("Pedro Ruiz", clienteUseCase.obtenerPorId(1).getNombreCompleto());
        assertEquals("Pedro Ruiz", clienteUseCase.obtenerPorDocumento("9001").getNombreCompleto());
        assertEquals(1, clienteUseCase.listarTodos().size());
        assertThrows(EntidadDuplicadaException.class, () -> clienteUseCase.registrar(nuevoCliente));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(null));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, "", null, "Ruiz", null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, "Pedro", null, "", null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.obtenerPorId(null));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.obtenerPorDocumento(" "));

        assertEquals("Efectivo", metodoPagoUseCase.obtenerPorId(1).getNombre());
        assertEquals(1, metodoPagoUseCase.listarTodos().size());
        assertEquals("CC", tipoDocumentoUseCase.obtenerPorId(1).getNombre());
        assertEquals(1, tipoDocumentoUseCase.listarTodos().size());
        assertEquals("Ingreso por Compra", tipoMovimientoUseCase.obtenerPorId(1).getNombre());
        assertEquals(1, tipoMovimientoUseCase.listarTodos().size());
        assertThrows(ReglaNegocioException.class, () -> metodoPagoUseCase.obtenerPorId(null));
        assertThrows(ReglaNegocioException.class, () -> tipoDocumentoUseCase.obtenerPorId(null));
        assertThrows(ReglaNegocioException.class, () -> tipoMovimientoUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> metodoPagoUseCase.obtenerPorId(99));
    }
}
