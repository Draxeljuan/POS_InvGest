package com.invengest.notification.domain.model;

import java.util.List;

public record DatosAlertaStock(
        List<String> destinatarios,
        List<ItemAlertaStock> items
) {}
