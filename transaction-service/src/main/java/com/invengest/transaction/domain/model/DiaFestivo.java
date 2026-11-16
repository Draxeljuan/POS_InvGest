package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaFestivo {

    public static final String FUENTE_API_LIVE = "API_LIVE";
    public static final String FUENTE_CACHE = "CACHE";
    public static final String FUENTE_CACHE_STALE = "CACHE_STALE";

    private LocalDate fecha;
    private String nombre;
    private String fuente;
}
