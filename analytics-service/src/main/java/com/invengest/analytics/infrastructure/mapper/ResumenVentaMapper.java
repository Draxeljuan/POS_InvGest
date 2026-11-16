package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResumenVentaMapper {

    ResumenVentasDiarias toDomain(ResumenVentasDiariasData entity);

    ResumenVentasDiariasData toEntity(ResumenVentasDiarias domain);

    @Mapping(target = "ventasNetas", expression = "java(domain.calcularVentasNetas())")
    ResumenVentaResponseDTO toResponse(ResumenVentasDiarias domain);

    List<ResumenVentaResponseDTO> toResponseList(List<ResumenVentasDiarias> domains);
}
