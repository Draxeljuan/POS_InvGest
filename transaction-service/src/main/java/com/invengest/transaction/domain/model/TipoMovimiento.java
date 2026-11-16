package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoMovimiento {

    public static final String MOV_INGRESO_COMPRA = "Ingreso por Compra";
    public static final String MOV_SALIDA_VENTA = "Salida por Venta";
    public static final String MOV_AJUSTE_PERDIDA_DANO = "Ajuste por Pérdida/Daño";
    public static final String MOV_AJUSTE_CONTEO_FISICO = "Ajuste por Conteo Físico";
    public static final String MOV_ANULACION_VENTA = "Anulación de Venta";
    public static final String MOV_AJUSTE_POSITIVO_CONTEO = "Ajuste Positivo por Conteo";

    private Integer idMovimiento;
    private String nombre;

    public boolean esIncrementoStock() {
        if (nombre == null) {
            return false;
        }
        String n = nombre.trim();
        return MOV_INGRESO_COMPRA.equalsIgnoreCase(n)
                || MOV_AJUSTE_POSITIVO_CONTEO.equalsIgnoreCase(n)
                || MOV_ANULACION_VENTA.equalsIgnoreCase(n);
    }

    public boolean esDecrementoStock() {
        if (nombre == null) {
            return false;
        }
        String n = nombre.trim();
        return MOV_SALIDA_VENTA.equalsIgnoreCase(n)
                || MOV_AJUSTE_PERDIDA_DANO.equalsIgnoreCase(n)
                || MOV_AJUSTE_CONTEO_FISICO.equalsIgnoreCase(n);
    }

    public boolean esAjusteManualPermitido() {
        if (nombre == null) {
            return false;
        }
        String n = nombre.trim();
        return !MOV_SALIDA_VENTA.equalsIgnoreCase(n) && !MOV_ANULACION_VENTA.equalsIgnoreCase(n);
    }
}
