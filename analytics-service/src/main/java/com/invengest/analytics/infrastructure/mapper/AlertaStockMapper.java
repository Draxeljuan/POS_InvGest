package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.AlertaStockResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlertaStockMapper {

    AlertaStock toDomain(AlertaStockData entity);

    AlertaStockData toEntity(AlertaStock domain);

    AlertaStockResponseDTO toResponse(AlertaStock domain);

    List<AlertaStockResponseDTO> toResponseList(List<AlertaStock> domains);
}
