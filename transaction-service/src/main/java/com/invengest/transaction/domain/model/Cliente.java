package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private Integer idCliente;
    private TipoDocumento tipoDocumento;
    private String documentoIdentidad;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String telefono;
    private String email;

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (primerNombre != null) {
            sb.append(primerNombre.trim());
        }
        if (segundoNombre != null && !segundoNombre.isBlank()) {
            sb.append(" ").append(segundoNombre.trim());
        }
        if (primerApellido != null && !primerApellido.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(primerApellido.trim());
        }
        if (segundoApellido != null && !segundoApellido.isBlank()) {
            sb.append(" ").append(segundoApellido.trim());
        }
        return sb.toString().trim();
    }

    public boolean tieneEmailValido() {
        return email != null && !email.isBlank() && email.contains("@");
    }
}
