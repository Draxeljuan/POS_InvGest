package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.CierreCajaDetalle;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.model.ResumenMetodoPago;
import com.invengest.transaction.infrastructure.entry_points.dto.response.CierreCajaDetalleResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.CierreCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenMetodoPagoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MetodoPagoRestMapper.class})
public interface CajaRestMapper {
    ResumenMetodoPagoResponse toResumenMetodoResponse(ResumenMetodoPago domain);
    ResumenCajaResponse toResumenResponse(ResumenCaja domain);
    CierreCajaDetalleResponse toDetalleResponse(CierreCajaDetalle domain);
    CierreCajaResponse toCierreResponse(CierreCaja domain);
}
