package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoDocumentoData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TipoDocumentoMapper {
    TipoDocumento toDomain(TipoDocumentoData entity);
    TipoDocumentoData toEntity(TipoDocumento domain);
}
