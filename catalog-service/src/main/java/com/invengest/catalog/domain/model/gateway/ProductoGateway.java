package com.invengest.catalog.domain.model.gateway;

import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.SolicitudItemStock;

import java.util.List;
import java.util.Optional;

public interface ProductoGateway {
    Optional<Producto> findById(String idProducto);
    List<Producto> findByIds(List<String> idsProductos);
    List<Producto> findAll();
    List<Producto> findBySearchCriteria(String query);
    List<Producto> findLowStockProducts();
    Producto save(Producto producto);
    boolean existsById(String idProducto);
    String generarSiguienteIdProducto();
    List<Producto> descontarStockAtomico(List<SolicitudItemStock> items, EstadoProducto estadoAgotado);
    List<Producto> reintegrarStockAtomico(List<SolicitudItemStock> items, EstadoProducto estadoActivo);
}
