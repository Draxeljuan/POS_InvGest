package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.usecase.CajaUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.RegistrarCierreCajaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.CierreCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenCajaResponse;
import com.invengest.transaction.infrastructure.mapper.CajaRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaControllerTest {

    @Mock
    private CajaUseCase cajaUseCase;
    @Mock
    private CajaRestMapper cajaRestMapper;

    @InjectMocks
    private CajaController cajaController;

    @Test
    @DisplayName("Debe obtener resumen, registrar cierre y consultar cierres de caja")
    void debeOperarEndpointsDeCaja() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenCaja resumen = new ResumenCaja(fecha, new BigDecimal("50000.00"), 3, BigDecimal.ZERO, 0, new BigDecimal("30000.00"), List.of(), false);
        ResumenCajaResponse resumenResp = new ResumenCajaResponse(fecha, new BigDecimal("50000.00"), 3, BigDecimal.ZERO, 0, new BigDecimal("30000.00"), List.of(), false);

        CierreCaja cierre = new CierreCaja();
        cierre.setIdCierre(1);
        CierreCajaResponse cierreResp = new CierreCajaResponse(
                1, fecha, 1, new BigDecimal("50000.00"), 3,
                new BigDecimal("30000.00"), new BigDecimal("30000.00"), BigDecimal.ZERO,
                false, LocalDate.of(2026, 11, 17), "OK", LocalDateTime.now(), List.of()
        );
        RegistrarCierreCajaRequest req = new RegistrarCierreCajaRequest(fecha, 1, new BigDecimal("30000.00"), "OK");

        when(cajaUseCase.obtenerResumenCaja(fecha)).thenReturn(resumen);
        when(cajaRestMapper.toResumenResponse(resumen)).thenReturn(resumenResp);
        when(cajaUseCase.registrarCierreCaja(fecha, 1, new BigDecimal("30000.00"), "OK")).thenReturn(cierre);
        when(cajaUseCase.obtenerCierrePorFecha(fecha)).thenReturn(cierre);
        when(cajaUseCase.listarCierres()).thenReturn(List.of(cierre));
        when(cajaRestMapper.toCierreResponse(cierre)).thenReturn(cierreResp);

        // Act
        ResponseEntity<ResumenCajaResponse> rResumen = cajaController.obtenerResumen(fecha);
        ResponseEntity<CierreCajaResponse> rReg = cajaController.registrarCierre(req);
        ResponseEntity<CierreCajaResponse> rFecha = cajaController.obtenerCierrePorFecha(fecha);
        ResponseEntity<List<CierreCajaResponse>> rLista = cajaController.listarCierres();

        // Assert
        assertEquals(HttpStatus.OK, rResumen.getStatusCode());
        assertEquals(new BigDecimal("30000.00"), rResumen.getBody().efectivoEsperado());
        assertEquals(HttpStatus.CREATED, rReg.getStatusCode());
        assertEquals(HttpStatus.OK, rFecha.getStatusCode());
        assertEquals(1, rLista.getBody().size());
    }
}
