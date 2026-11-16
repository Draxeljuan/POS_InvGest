package com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ProductoProveedorDataId implements Serializable {

    @Serial
    private static final long serialVersionUID = -5280225435741267587L;

    @Column(name = "id_producto", nullable = false, length = 50)
    private String idProducto;

    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;
}
