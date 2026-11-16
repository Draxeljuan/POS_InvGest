package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.infrastructure.entry_points.dto.response.DashboardResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        ResumenVentaMapper.class,
        TopProductoMapper.class,
        AlertaStockMapper.class
})
public interface DashboardRestMapper {

    DashboardResponseDTO toResponse(DashboardResumen domain);
}
