package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoItemTest {

    @Test
    @DisplayName("Debe contener los valores esperados en el enum TipoItem")
    void debeContenerValoresEsperados() {
        // Arrange & Act
        TipoItem prod = TipoItem.valueOf("PRODUCTO");
        TipoItem serv = TipoItem.valueOf("SERVICIO");

        // Assert
        assertEquals(TipoItem.PRODUCTO, prod);
        assertEquals(TipoItem.SERVICIO, serv);
        assertEquals(2, TipoItem.values().length);
    }
}
