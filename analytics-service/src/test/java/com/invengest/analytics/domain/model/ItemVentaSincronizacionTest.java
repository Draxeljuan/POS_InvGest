package com.invengest.analytics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemVentaSincronizacionTest {

    @Test
    @DisplayName("Debe construir y manipular ItemVentaSincronizacion correctamente")
    void builderYGettersSetters_FuncionanCorrectamente() {
        // Arrange
        String idProducto = "PROD-01";
        String nombreProducto = "Cuaderno";
        Integer cantidad = 4;

        // Act
        ItemVentaSincronizacion item = ItemVentaSincronizacion.builder()
                .idProducto(idProducto)
                .nombreProducto(nombreProducto)
                .cantidadVendida(cantidad)
                .build();

        ItemVentaSincronizacion vacio = new ItemVentaSincronizacion();
        vacio.setIdProducto("PROD-02");
        vacio.setNombreProducto("Lapiz");
        vacio.setCantidadVendida(2);

        // Assert
        assertThat(item.getIdProducto()).isEqualTo("PROD-01");
        assertThat(item.getNombreProducto()).isEqualTo("Cuaderno");
        assertThat(item.getCantidadVendida()).isEqualTo(4);
        assertThat(vacio.getIdProducto()).isEqualTo("PROD-02");
        assertThat(vacio.getNombreProducto()).isEqualTo("Lapiz");
        assertThat(vacio.getCantidadVendida()).isEqualTo(2);
    }
}
