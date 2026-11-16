package com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cierre_caja_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaDetalleData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cierre", nullable = false)
    private CierreCajaData cierreCaja;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPagoData metodoPago;

    @NotNull
    @Column(name = "total_metodo", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalMetodo;

    @NotNull
    @Column(name = "cantidad_operaciones", nullable = false)
    private Integer cantidadOperaciones;
}
