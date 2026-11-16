package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaServicioMapper {
    CategoriaServicio toDomain(CategoriaServicioData entity);
    CategoriaServicioData toEntity(CategoriaServicio domain);
}
