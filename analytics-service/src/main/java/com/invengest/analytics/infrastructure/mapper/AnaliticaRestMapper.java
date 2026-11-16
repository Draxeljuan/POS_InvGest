package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.infrastructure.entry_points.dto.request.ItemVentaSincronizacionRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.EstacionalidadResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentasUsdResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnaliticaRestMapper {

    @Mapping(target = "cantidadVendida", expression = "java(dto.obtenerCantidadEfectiva())")
    ItemVentaSincronizacion toDomainItem(ItemVentaSincronizacionRequestDTO dto);

    List<ItemVentaSincronizacion> toDomainItems(List<ItemVentaSincronizacionRequestDTO> dtos);

    ResumenVentasUsdResponseDTO toUsdResponse(ResumenVentasUsd domain);

    EstacionalidadResponseDTO toEstacionalidadResponse(ReporteEstacionalidad domain);
}
