package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.CierreCajaDetalle;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaDetalleData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MetodoPagoMapper.class})
public interface CierreCajaDetalleMapper {

    @Mapping(source = "cierreCaja.idCierre", target = "idCierre")
    CierreCajaDetalle toDomain(CierreCajaDetalleData entity);

    @Mapping(target = "cierreCaja", ignore = true)
    CierreCajaDetalleData toEntity(CierreCajaDetalle domain);
}
