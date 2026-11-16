package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.infrastructure.entry_points.dto.request.CategoriaServicioRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaServicioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaServicioRestMapper {

    @Mapping(target = "idCategoriaServicio", ignore = true)
    CategoriaServicio toDomain(CategoriaServicioRequest request);

    CategoriaServicioResponse toResponse(CategoriaServicio domain);
}
