package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    Categoria toDomain(CategoriaData entity);
    CategoriaData toEntity(Categoria domain);
}
