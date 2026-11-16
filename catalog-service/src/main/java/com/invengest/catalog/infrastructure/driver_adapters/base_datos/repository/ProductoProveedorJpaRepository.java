package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoProveedorJpaRepository extends JpaRepository<ProductoProveedorData, ProductoProveedorDataId> {
    List<ProductoProveedorData> findById_IdProducto(String idProducto);
    List<ProductoProveedorData> findById_IdProveedor(Integer idProveedor);
    boolean existsById_IdProveedor(Integer idProveedor);
}
