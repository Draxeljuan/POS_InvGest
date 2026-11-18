package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.gateway.CalendarioFestivosGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarioLaboralTest {

    @Mock
    private CalendarioFestivosGateway calendarioFestivosGateway;

    @InjectMocks
    private CalendarioLaboral calendarioLaboral;

    @Test
    @DisplayName("Debe evaluar domingos, festivos, siguiente día hábil y conteo de días hábiles")
    void debeEvaluarDomingosFestivosYSiguienteDiaHabil() {
        // Arrange
        when(calendarioFestivosGateway.obtenerFestivos(2026)).thenReturn(List.of(
                new DiaFestivo(LocalDate.of(2026, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(null, "Fecha Nula", DiaFestivo.FUENTE_CACHE)
        ));
        when(calendarioFestivosGateway.obtenerFestivos(2025)).thenReturn(null);

        // Act
        boolean sabado14Habil = calendarioLaboral.esDiaHabil(LocalDate.of(2026, 11, 14));
        boolean domingo15Habil = calendarioLaboral.esDiaHabil(LocalDate.of(2026, 11, 15));
        boolean lunesFestivo16Habil = calendarioLaboral.esDiaHabil(LocalDate.of(2026, 11, 16));
        boolean dia2025SinFestivos = calendarioLaboral.esDiaHabil(LocalDate.of(2025, 5, 6));
        LocalDate siguienteDesdeSabado14 = calendarioLaboral.siguienteDiaHabil(LocalDate.of(2026, 11, 14));
        int diasHabilesSemana = calendarioLaboral.contarDiasHabiles(
                LocalDate.of(2026, 11, 14),
                LocalDate.of(2026, 11, 18)
        );

        // Assert
        assertTrue(sabado14Habil);
        assertFalse(domingo15Habil);
        assertFalse(lunesFestivo16Habil);
        assertTrue(dia2025SinFestivos);
        assertEquals(LocalDate.of(2026, 11, 17), siguienteDesdeSabado14);
        assertEquals(3, diasHabilesSemana);
    }

    @Test
    @DisplayName("Debe validar parámetros nulos o rangos invertidos en CalendarioLaboral")
    void debeLanzarExcepcionAnteParametrosInvalidos() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        LocalDate finAnterior = LocalDate.of(2026, 11, 10);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> calendarioLaboral.esDiaHabil(null));
        assertThrows(ReglaNegocioException.class, () -> calendarioLaboral.siguienteDiaHabil(null));
        assertThrows(ReglaNegocioException.class, () -> calendarioLaboral.contarDiasHabiles(null, finAnterior));
        assertThrows(ReglaNegocioException.class, () -> calendarioLaboral.contarDiasHabiles(inicio, null));
        assertThrows(ReglaNegocioException.class, () -> calendarioLaboral.contarDiasHabiles(inicio, finAnterior));
    }
}
