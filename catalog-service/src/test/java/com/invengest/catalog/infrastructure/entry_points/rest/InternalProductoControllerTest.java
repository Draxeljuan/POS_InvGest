package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.ValidacionItemStock;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ItemStockValidadoResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ProductoRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalProductoController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class InternalProductoControllerTest {

    private static final String INTERNAL_KEY = "default_dev_internal_api_key_2026";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoUseCase productoUseCase;

    @MockitoBean
    private ProductoRestMapper productoRestMapper;

    @Test
    @DisplayName("Valida, descuenta y reintegra stock batch con X-Internal-Api-Key")
    void endpointsBatchInternosValidaDescuentaYReintegraStockConInternalApiKey() throws Exception {
        // Arrange
        SolicitudItemStock sol = new SolicitudItemStock("PROD-0001", 2);
        ValidacionItemStock val = new ValidacionItemStock("PROD-0001", "Cuaderno", new BigDecimal("8000.00"), 10, 2, true);
        ItemStockValidadoResponseDTO valDto = new ItemStockValidadoResponseDTO("PROD-0001", "Cuaderno", new BigDecimal("8000.00"), 10, 2, true);
        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 8, 2, "A1",
                new EstadoProducto(1, "Activo"));
        ProductoResponse prodResp = new ProductoResponse("PROD-0001", new CategoriaResponse(1, "C", ""),
                "Cuaderno", new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 8, 2, "A1",
                new EstadoProductoResponse(1, "Activo"));

        when(productoRestMapper.toSolicitudItemStock(any())).thenReturn(sol);
        when(productoUseCase.validarStockBatch(anyList())).thenReturn(List.of(val));
        when(productoRestMapper.toItemStockValidadoResponse(val)).thenReturn(valDto);
        when(productoUseCase.descontarStockBatch(anyList())).thenReturn(List.of(prod));
        when(productoUseCase.reintegrarStockBatch(anyList())).thenReturn(List.of(prod));
        when(productoRestMapper.toResponse(prod)).thenReturn(prodResp);

        String payloadBatch = "[{\"idProducto\":\"PROD-0001\",\"cantidad\":2}]";

        // Act & Assert
        mockMvc.perform(post("/internal/productos/validar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].suficiente").value(true));

        mockMvc.perform(post("/internal/productos/descontar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(8));

        mockMvc.perform(post("/internal/productos/reintegrar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("PROD-0001"));
    }
}
