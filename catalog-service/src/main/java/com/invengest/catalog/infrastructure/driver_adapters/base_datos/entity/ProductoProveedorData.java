package com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producto_proveedor")
public class ProductoProveedorData {

    @EmbeddedId
    private ProductoProveedorDataId id;

    @Column(name = "precio_compra_proveedor", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompraProveedor;
}
