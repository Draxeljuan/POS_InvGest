package com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cierre_caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Integer idCierre;

    @NotNull
    @Column(name = "fecha_cierre", nullable = false, unique = true)
    private LocalDate fechaCierre;

    @NotNull
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @NotNull
    @Column(name = "total_ventas", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVentas;

    @NotNull
    @Column(name = "cantidad_ventas", nullable = false)
    private Integer cantidadVentas;

    @NotNull
    @Column(name = "efectivo_esperado", nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoEsperado;

    @NotNull
    @Column(name = "efectivo_contado", nullable = false, precision = 12, scale = 2)
    private BigDecimal efectivoContado;

    @NotNull
    @Column(name = "diferencia", nullable = false, precision = 12, scale = 2)
    private BigDecimal diferencia;

    @NotNull
    @Column(name = "es_dia_habil", nullable = false)
    private Boolean esDiaHabil;

    @Column(name = "proximo_dia_habil")
    private LocalDate proximoDiaHabil;

    @Size(max = 255)
    @Column(name = "observacion", length = 255)
    private String observacion;

    @NotNull
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "cierreCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CierreCajaDetalleData> detalles = new ArrayList<>();
}
