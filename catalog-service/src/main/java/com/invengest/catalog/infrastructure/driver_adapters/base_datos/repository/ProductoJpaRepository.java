package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoData, String> {

    boolean existsByCategoria_IdCategoria(Integer idCategoria);

    @Query("""
            SELECT p FROM ProductoData p
            JOIN FETCH p.categoria c
            JOIN FETCH p.estado e
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.idProducto) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<ProductoData> searchByCriteria(@Param("query") String query);

    @Query("""
            SELECT p FROM ProductoData p
            JOIN FETCH p.categoria c
            JOIN FETCH p.estado e
            WHERE p.stock <= p.stockMinimo
            """)
    List<ProductoData> findLowStockProducts();
}
