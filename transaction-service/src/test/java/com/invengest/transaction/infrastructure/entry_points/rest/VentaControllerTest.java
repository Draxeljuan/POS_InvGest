package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.usecase.AnularVentaUseCase;
import com.invengest.transaction.domain.usecase.ComprobanteVentaUseCase;
import com.invengest.transaction.domain.usecase.ConsultarVentaUseCase;
import com.invengest.transaction.domain.usecase.RegistrarVentaUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.AnularVentaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.request.DetalleVentaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.request.EnviarComprobanteRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.request.RegistrarVentaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ComprobanteVentaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MetodoPagoResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.VentaResponse;
import com.invengest.transaction.infrastructure.mapper.VentaRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    @Mock
    private RegistrarVentaUseCase registrarVentaUseCase;
    @Mock
    private AnularVentaUseCase anularVentaUseCase;
    @Mock
    private ConsultarVentaUseCase consultarVentaUseCase;
    @Mock
    private ComprobanteVentaUseCase comprobanteVentaUseCase;
    @Mock
    private VentaRestMapper ventaRestMapper;

    @InjectMocks
    private VentaController ventaController;

    @Test
    @DisplayName("Debe registrar, anular, consultar ventas y gestionar comprobantes")
    void debeRegistrarAnularConsultarYGestionarComprobantes() {
        // Arrange
        Venta venta = new Venta();
        venta.setIdVenta(10);
        VentaResponse ventaResp = new VentaResponse(
                10, 1, LocalDateTime.now(), null, new MetodoPagoResponse(1, "Efectivo"),
                new BigDecimal("15000.00"), "COMPLETADA", null, null, null, List.of()
        );
        ComprobanteVenta comp = new ComprobanteVenta(
                10, "POS-000010", LocalDateTime.now(), "COMPLETADA", 1,
                "Consumidor Final", null, "cliente@correo.com", "Efectivo", new BigDecimal("15000.00"), List.of()
        );
        ComprobanteVentaResponse compResp = new ComprobanteVentaResponse(
                10, "POS-000010", LocalDateTime.now(), "COMPLETADA", 1,
                "Consumidor Final", null, "cliente@correo.com", "Efectivo", new BigDecimal("15000.00"), List.of()
        );

        RegistrarVentaRequest regReq = new RegistrarVentaRequest(
                1, 1, 1, List.of(new DetalleVentaRequest("PROD-01", TipoItem.PRODUCTO, null, null, 2))
        );
        AnularVentaRequest anularReq = new AnularVentaRequest(1, "Devolución");

        when(ventaRestMapper.toDomain(regReq)).thenReturn(venta);
        when(registrarVentaUseCase.registrarVenta(venta)).thenReturn(venta);
        when(anularVentaUseCase.anularVenta(10, 1, "Devolución")).thenReturn(venta);
        when(consultarVentaUseCase.obtenerPorId(10)).thenReturn(venta);
        when(consultarVentaUseCase.listarTodas()).thenReturn(List.of(venta));
        when(consultarVentaUseCase.listarPorUsuario(1)).thenReturn(List.of(venta));
        when(consultarVentaUseCase.filtrarPorFechas(any(), any())).thenReturn(List.of(venta));
        when(comprobanteVentaUseCase.obtenerComprobante(10)).thenReturn(comp);
        when(comprobanteVentaUseCase.enviarComprobantePorCorreo(10, "cliente@correo.com")).thenReturn(comp);
        when(comprobanteVentaUseCase.enviarComprobantePorCorreo(10, null)).thenReturn(comp);
        when(ventaRestMapper.toResponse(venta)).thenReturn(ventaResp);
        when(ventaRestMapper.toComprobanteResponse(comp)).thenReturn(compResp);

        // Act
        ResponseEntity<VentaResponse> rReg = ventaController.registrar(regReq);
        ResponseEntity<VentaResponse> rAnu = ventaController.anular(10, anularReq);
        ResponseEntity<ComprobanteVentaResponse> rComp = ventaController.obtenerComprobante(10);
        ResponseEntity<ComprobanteVentaResponse> rEnv1 = ventaController.enviarComprobante(10, new EnviarComprobanteRequest("cliente@correo.com"));
        ResponseEntity<ComprobanteVentaResponse> rEnv2 = ventaController.enviarComprobante(10, null);
        ResponseEntity<VentaResponse> rId = ventaController.obtenerPorId(10);
        ResponseEntity<List<VentaResponse>> rTodas = ventaController.listar(null, null, null);
        ResponseEntity<List<VentaResponse>> rSoloInicio = ventaController.listar(LocalDateTime.now(), null, null);
        ResponseEntity<List<VentaResponse>> rUsuario = ventaController.listar(null, null, 1);
        ResponseEntity<List<VentaResponse>> rFechas = ventaController.listar(LocalDateTime.now().minusDays(1), LocalDateTime.now(), null);

        // Assert
        assertEquals(HttpStatus.CREATED, rReg.getStatusCode());
        assertEquals(10, rReg.getBody().idVenta());
        assertEquals(HttpStatus.OK, rAnu.getStatusCode());
        assertEquals("POS-000010", rComp.getBody().numeroComprobante());
        assertEquals("cliente@correo.com", rEnv1.getBody().emailCliente());
        assertEquals(HttpStatus.OK, rEnv2.getStatusCode());
        assertEquals(HttpStatus.OK, rId.getStatusCode());
        assertEquals(1, rTodas.getBody().size());
        assertEquals(1, rSoloInicio.getBody().size());
        assertEquals(1, rUsuario.getBody().size());
        assertEquals(1, rFechas.getBody().size());
    }
}
