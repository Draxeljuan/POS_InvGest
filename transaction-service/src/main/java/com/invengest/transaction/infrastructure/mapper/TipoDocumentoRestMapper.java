package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoDocumentoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TipoDocumentoRestMapper {
    TipoDocumentoResponse toResponse(TipoDocumento domain);
}
