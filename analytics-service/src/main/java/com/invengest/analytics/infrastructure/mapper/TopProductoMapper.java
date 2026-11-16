package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.TopProductoData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.TopProductoResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopProductoMapper {

    TopProducto toDomain(TopProductoData entity);

    TopProductoData toEntity(TopProducto domain);

    TopProductoResponseDTO toResponse(TopProducto domain);

    List<TopProductoResponseDTO> toResponseList(List<TopProducto> domains);
}
