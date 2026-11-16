package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ProveedorRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProveedorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EstadoProveedorRestMapper.class})
public interface ProveedorRestMapper {

    @Mapping(target = "idProveedor", ignore = true)
    @Mapping(target = "estado.idEstadoProveedor", source = "idEstadoProveedor")
    Proveedor toDomain(ProveedorRequest request);

    ProveedorResponse toResponse(Proveedor domain);
}
