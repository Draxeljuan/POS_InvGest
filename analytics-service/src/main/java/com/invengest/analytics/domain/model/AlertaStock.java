package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStock {

    public static final String ESTADO_AGOTADO = "Agotado";
    public static final String ESTADO_BAJO_STOCK = "Bajo Stock";
    public static final short UMBRAL_MINIMO_DEFECTO = 10;

    private Integer idAlerta;
    private String idProducto;
    private String nombreProducto;
    private Short stockActual;
    private Short stockMinimo;
    private String estado;
    private LocalDateTime fechaAlerta;

    public static boolean requiereAlerta(Short stockActual, Short stockMinimo) {
        if (stockActual == null) {
            throw new ReglaNegocioException("El stock actual del producto es obligatorio.");
        }
        short minimo = resolverStockMinimo(stockMinimo);
        return stockActual <= 0 || stockActual <= minimo;
    }

    public static String evaluarEstado(Short stockActual, Short stockMinimo) {
        if (stockActual == null) {
            throw new ReglaNegocioException("El stock actual del producto es obligatorio.");
        }
        return stockActual <= 0 ? ESTADO_AGOTADO : ESTADO_BAJO_STOCK;
    }

    public static short resolverStockMinimo(Short stockMinimo) {
        if (stockMinimo == null) {
            return UMBRAL_MINIMO_DEFECTO;
        }
        return (short) Math.max(0, stockMinimo);
    }

    public static String normalizarEstadoFiltro(String estadoRaw) {
        if (estadoRaw == null || estadoRaw.isBlank()) {
            throw new ReglaNegocioException("El estado de alerta debe ser 'Agotado' o 'Bajo Stock'.");
        }
        String limpio = estadoRaw.trim();
        if (ESTADO_AGOTADO.equalsIgnoreCase(limpio) || "AGOTADO".equalsIgnoreCase(limpio)) {
            return ESTADO_AGOTADO;
        }
        if (ESTADO_BAJO_STOCK.equalsIgnoreCase(limpio) || "BAJO_STOCK".equalsIgnoreCase(limpio)) {
            return ESTADO_BAJO_STOCK;
        }
        throw new ReglaNegocioException("El estado de alerta debe ser 'Agotado' o 'Bajo Stock'.");
    }

    public void actualizarDatosAlerta(String nombreProducto, Short stockActual, Short stockMinimo) {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El identificador del producto es obligatorio.");
        }
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
        if (stockActual == null) {
            throw new ReglaNegocioException("El stock actual del producto es obligatorio.");
        }
        short minimo = resolverStockMinimo(stockMinimo);
        this.nombreProducto = nombreProducto.trim();
        this.stockActual = stockActual;
        this.stockMinimo = minimo;
        this.estado = evaluarEstado(stockActual, minimo);
        this.fechaAlerta = LocalDateTime.now();
    }
}
