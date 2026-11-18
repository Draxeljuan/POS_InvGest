package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.ResumenMetodoPago;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenMetodoPagoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CajaRestMapperTest {

    @Test
    @DisplayName("Debe mapear ResumenMetodoPago y ejecutar métodos default de RestMappers")
    void debeMapearModelosYMetodosDefaultDeRestMappers() {
        // Arrange
        CajaRestMapper cajaRestMapper = Mappers.getMapper(CajaRestMapper.class);
        VentaRestMapper ventaRestMapper = Mappers.getMapper(VentaRestMapper.class);
        ClienteRestMapper clienteRestMapper = Mappers.getMapper(ClienteRestMapper.class);
        MovimientoInventarioRestMapper movRestMapper = Mappers.getMapper(MovimientoInventarioRestMapper.class);
        ResumenMetodoPago rmp = new ResumenMetodoPago(1, "Efectivo", new BigDecimal("25000.00"), 3);

        // Act
        ResumenMetodoPagoResponse rmpResp = cajaRestMapper.toResumenMetodoResponse(rmp);

        // Assert
        assertEquals(1, rmpResp.idMetodoPago());
        assertEquals("Efectivo", rmpResp.nombreMetodoPago());
        assertEquals(new BigDecimal("25000.00"), rmpResp.total());
        assertEquals(3, rmpResp.cantidadOperaciones());

        assertNull(ventaRestMapper.mapCliente(null));
        assertNotNull(ventaRestMapper.mapCliente(1));
        assertNull(ventaRestMapper.mapMetodoPago(null));
        assertNotNull(ventaRestMapper.mapMetodoPago(1));
        assertNull(clienteRestMapper.mapTipoDocumento(null));
        assertNotNull(clienteRestMapper.mapTipoDocumento(1));
        assertNull(movRestMapper.mapTipoMovimiento(null));
        assertNotNull(movRestMapper.mapTipoMovimiento(1));
    }
}
