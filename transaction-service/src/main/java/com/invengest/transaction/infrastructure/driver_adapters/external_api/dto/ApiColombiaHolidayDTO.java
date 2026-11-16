package com.invengest.transaction.infrastructure.driver_adapters.external_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiColombiaHolidayDTO(
        String date,
        String name
) {}
