package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProveedorData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EstadoProveedorMapper.class})
public interface ProveedorMapper {
    Proveedor toDomain(ProveedorData entity);
    ProveedorData toEntity(Proveedor domain);
}
