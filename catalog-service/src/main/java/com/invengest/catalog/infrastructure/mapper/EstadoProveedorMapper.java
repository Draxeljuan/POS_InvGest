package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProveedorData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EstadoProveedorMapper {
    EstadoProveedor toDomain(EstadoProveedorData entity);
    EstadoProveedorData toEntity(EstadoProveedor domain);
}
