package com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categoria_servicio")
public class CategoriaServicioData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_servicio", nullable = false)
    private Integer idCategoriaServicio;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;
}
