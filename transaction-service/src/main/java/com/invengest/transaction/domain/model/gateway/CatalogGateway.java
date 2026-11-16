package com.invengest.transaction.domain.model.gateway;

import com.invengest.transaction.domain.model.ProductoCatalogo;
import com.invengest.transaction.domain.model.ServicioCatalogo;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.ValidacionStockItem;

import java.util.List;
import java.util.Optional;

public interface CatalogGateway {
    List<ValidacionStockItem> validarStockBatch(List<SolicitudStockItem> items);
    void descontarStockBatch(List<SolicitudStockItem> items);
    void reintegrarStockBatch(List<SolicitudStockItem> items);
    Optional<ProductoCatalogo> obtenerProductoPorId(String idProducto);
    Optional<ServicioCatalogo> obtenerServicioPorId(Integer idServicio);
}
