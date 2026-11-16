package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.VentaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaData, Integer> {

    @Query("SELECT DISTINCT v FROM VentaData v LEFT JOIN FETCH v.detalles LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.metodoPago WHERE v.fecha BETWEEN :inicio AND :fin ORDER BY v.fecha DESC")
    List<VentaData> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT DISTINCT v FROM VentaData v LEFT JOIN FETCH v.detalles LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.metodoPago WHERE v.idUsuario = :idUsuario ORDER BY v.fecha DESC")
    List<VentaData> findByIdUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT DISTINCT v FROM VentaData v LEFT JOIN FETCH v.detalles LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.metodoPago WHERE v.idVenta = :id")
    Optional<VentaData> findByIdWithDetails(@Param("id") Integer id);
}
