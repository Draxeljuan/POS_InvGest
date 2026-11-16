package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MovimientoInventarioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioJpaRepository extends JpaRepository<MovimientoInventarioData, Integer> {

    @Query("SELECT m FROM MovimientoInventarioData m JOIN FETCH m.tipoMovimiento WHERE m.idProducto = :idProducto ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventarioData> findByIdProductoOrderByFechaMovimientoDesc(@Param("idProducto") String idProducto);

    @Query("SELECT m FROM MovimientoInventarioData m JOIN FETCH m.tipoMovimiento WHERE m.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventarioData> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
