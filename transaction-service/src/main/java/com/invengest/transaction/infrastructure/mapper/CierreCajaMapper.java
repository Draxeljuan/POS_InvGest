package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CierreCajaDetalleMapper.class})
public interface CierreCajaMapper {
    CierreCaja toDomain(CierreCajaData entity);
    CierreCajaData toEntity(CierreCaja domain);
}
