package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CierreCaja {
    private Integer idCierre;
    private LocalDate fechaCierre;
    private Integer idUsuario;
    private BigDecimal totalVentas;
    private Integer cantidadVentas;
    private BigDecimal efectivoEsperado;
    private BigDecimal efectivoContado;
    private BigDecimal diferencia;
    private Boolean esDiaHabil;
    private LocalDate proximoDiaHabil;
    private String observacion;
    private LocalDateTime fechaRegistro;
    private List<CierreCajaDetalle> detalles = new ArrayList<>();

    public BigDecimal calcularDiferencia() {
        if (efectivoContado == null || efectivoContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El efectivo contado no puede ser nulo o negativo");
        }
        BigDecimal esperado = efectivoEsperado != null ? efectivoEsperado : BigDecimal.ZERO;
        this.efectivoEsperado = esperado.setScale(2, RoundingMode.HALF_UP);
        this.efectivoContado = efectivoContado.setScale(2, RoundingMode.HALF_UP);
        this.diferencia = this.efectivoContado.subtract(this.efectivoEsperado).setScale(2, RoundingMode.HALF_UP);
        return this.diferencia;
    }

    public void registrarCalendario(boolean diaHabil, LocalDate siguienteHabil) {
        this.esDiaHabil = diaHabil;
        this.proximoDiaHabil = siguienteHabil;
    }

    public boolean tieneDescuadre() {
        return diferencia != null && diferencia.compareTo(BigDecimal.ZERO) != 0;
    }
}
