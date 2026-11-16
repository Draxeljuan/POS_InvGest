package com.invengest.transaction.infrastructure.mapper;

import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.infrastructure.entry_points.dto.request.ClienteRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TipoDocumentoRestMapper.class})
public interface ClienteRestMapper {

    @Mapping(target = "nombreCompleto", expression = "java(domain.getNombreCompleto())")
    ClienteResponse toResponse(Cliente domain);

    @Mapping(target = "idCliente", ignore = true)
    @Mapping(target = "tipoDocumento", expression = "java(mapTipoDocumento(request.idTipoDocumento()))")
    Cliente toDomain(ClienteRequest request);

    default TipoDocumento mapTipoDocumento(Integer idTipoDocumento) {
        if (idTipoDocumento == null) {
            return null;
        }
        return new TipoDocumento(idTipoDocumento, null);
    }
}
