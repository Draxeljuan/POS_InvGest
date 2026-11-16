package com.invengest.catalog.infrastructure.mapper;

import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.ValidacionItemStock;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ItemStockRequestDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ProductoRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ItemStockValidadoResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.MejorProveedorResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoPrecioUsdResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoriaRestMapper.class, EstadoProductoRestMapper.class})
public interface ProductoRestMapper {

    @Mapping(target = "categoria.idCategoria", source = "idCategoria")
    @Mapping(target = "estado.idEstado", source = "idEstado")
    @Mapping(target = "fechaIngreso", ignore = true)
    Producto toDomain(ProductoRequest request);

    ProductoResponse toResponse(Producto domain);

    ProductoPrecioUsdResponseDTO toPrecioUsdResponse(ProductoPrecioUsd domain);

    MejorProveedorResponseDTO toMejorProveedorResponse(MejorProveedorProducto domain);

    SolicitudItemStock toSolicitudItemStock(ItemStockRequestDTO dto);

    ItemStockValidadoResponseDTO toItemStockValidadoResponse(ValidacionItemStock domain);
}
