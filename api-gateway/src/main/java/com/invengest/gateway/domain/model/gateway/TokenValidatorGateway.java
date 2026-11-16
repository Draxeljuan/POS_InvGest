package com.invengest.gateway.domain.model.gateway;

import com.invengest.gateway.domain.model.TokenClaims;

public interface TokenValidatorGateway {

    TokenClaims validarYExtraerClaims(String token);
}
