package com.invengest.notification.infrastructure.mapper;

import com.invengest.notification.domain.model.DatosAlertaStock;
import com.invengest.notification.domain.model.DatosBienvenida;
import com.invengest.notification.domain.model.DatosCierreCaja;
import com.invengest.notification.domain.model.DatosComprobanteVenta;
import com.invengest.notification.domain.model.DatosSeguridadCuenta;
import com.invengest.notification.domain.model.ItemAlertaStock;
import com.invengest.notification.domain.model.ItemComprobante;
import com.invengest.notification.domain.model.TotalMetodoPago;
import com.invengest.notification.infrastructure.entry_points.dto.ItemAlertaStockDTO;
import com.invengest.notification.infrastructure.entry_points.dto.ItemComprobanteDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionAlertaStockRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionBienvenidaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionCierreCajaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionComprobanteVentaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionSeguridadRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.TotalMetodoPagoDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    DatosBienvenida toDomain(NotificacionBienvenidaRequestDTO dto);

    DatosSeguridadCuenta toDomain(NotificacionSeguridadRequestDTO dto);

    DatosComprobanteVenta toDomain(NotificacionComprobanteVentaRequestDTO dto);

    ItemComprobante toDomain(ItemComprobanteDTO dto);

    DatosAlertaStock toDomain(NotificacionAlertaStockRequestDTO dto);

    ItemAlertaStock toDomain(ItemAlertaStockDTO dto);

    DatosCierreCaja toDomain(NotificacionCierreCajaRequestDTO dto);

    TotalMetodoPago toDomain(TotalMetodoPagoDTO dto);
}
