package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.infrastructure.entry_points.dto.request.CategoriaRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaRestMapper {

    @Mapping(target = "idCategoria", ignore = true)
    Categoria toDomain(CategoriaRequest request);

    CategoriaResponse toResponse(Categoria domain);
}
