package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProductoProveedorUseCase {

    private final ProductoProveedorGateway productoProveedorGateway;

    public ProductoProveedor asociarOActualizar(ProductoProveedor productoProveedor) {
        productoProveedor.validarInvariantes();
        return productoProveedorGateway.save(productoProveedor);
    }

    public List<ProductoProveedor> obtenerPorProducto(String idProducto) {
        return productoProveedorGateway.findByProductoId(idProducto);
    }

    public List<ProductoProveedor> obtenerPorProveedor(Integer idProveedor) {
        return productoProveedorGateway.findByProveedorId(idProveedor);
    }

    public void eliminarAsociacion(String idProducto, Integer idProveedor) {
        productoProveedorGateway.delete(idProducto, idProveedor);
    }
}
