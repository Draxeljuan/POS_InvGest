package com.invengest.gateway.domain.model;

public record DecisionAccesoRuta(
        boolean rutaPublica,
        TokenClaims tokenClaims
) {
    public static DecisionAccesoRuta permitirRutaPublica() {
        return new DecisionAccesoRuta(true, null);
    }

    public static DecisionAccesoRuta autorizarRutaProtegida(TokenClaims claims) {
        return new DecisionAccesoRuta(false, claims);
    }
}
