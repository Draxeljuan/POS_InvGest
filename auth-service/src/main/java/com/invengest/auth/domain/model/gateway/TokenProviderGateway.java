package com.invengest.auth.domain.model.gateway;

import com.invengest.auth.domain.model.Usuario;

public interface TokenProviderGateway {
    String generateToken(Usuario usuario);
}
