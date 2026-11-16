package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MetodoPagoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetodoPagoRestMapper {
    MetodoPagoResponse toResponse(MetodoPago domain);
}
