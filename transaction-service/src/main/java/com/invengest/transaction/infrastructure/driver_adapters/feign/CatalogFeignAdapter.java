package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.ProductoCatalogo;
import com.invengest.transaction.domain.model.ServicioCatalogo;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.ValidacionStockItem;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogProductoFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogServicioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockValidadoFeignDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogFeignAdapter implements CatalogGateway {

    private final CatalogFeignClient catalogFeignClient;

    @Override
    public List<ValidacionStockItem> validarStockBatch(List<SolicitudStockItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<ItemStockFeignRequestDTO> request = items.stream()
                .map(i -> new ItemStockFeignRequestDTO(i.getIdProducto(), i.getCantidad()))
                .toList();
        try {
            List<ItemStockValidadoFeignDTO> respuesta = catalogFeignClient.validarStockBatch(request);
            if (respuesta == null) {
                return List.of();
            }
            return respuesta.stream()
                    .map(dto -> new ValidacionStockItem(
                            dto.idProducto(),
                            dto.nombre(),
                            dto.precioVenta(),
                            dto.stockDisponible(),
                            dto.cantidadSolicitada(),
                            dto.suficiente()
                    ))
                    .toList();
        } catch (FeignException ex) {
            throw traducirExcepcionCatalogo(ex);
        }
    }

    @Override
    public void descontarStockBatch(List<SolicitudStockItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<ItemStockFeignRequestDTO> request = items.stream()
                .map(i -> new ItemStockFeignRequestDTO(i.getIdProducto(), i.getCantidad()))
                .toList();
        try {
            catalogFeignClient.descontarStockBatch(request);
        } catch (FeignException ex) {
            throw traducirExcepcionCatalogo(ex);
        }
    }

    @Override
    public void reintegrarStockBatch(List<SolicitudStockItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<ItemStockFeignRequestDTO> request = items.stream()
                .map(i -> new ItemStockFeignRequestDTO(i.getIdProducto(), i.getCantidad()))
                .toList();
        try {
            catalogFeignClient.reintegrarStockBatch(request);
        } catch (FeignException ex) {
            throw traducirExcepcionCatalogo(ex);
        }
    }

    @Override
    public Optional<ProductoCatalogo> obtenerProductoPorId(String idProducto) {
        try {
            CatalogProductoFeignDTO dto = catalogFeignClient.obtenerProductoPorId(idProducto);
            if (dto == null) {
                return Optional.empty();
            }
            return Optional.of(new ProductoCatalogo(
                    dto.idProducto(),
                    dto.nombre(),
                    dto.precioVenta(),
                    dto.stock(),
                    dto.stockMinimo()
            ));
        } catch (FeignException.NotFound ex) {
            return Optional.empty();
        } catch (FeignException ex) {
            throw traducirExcepcionCatalogo(ex);
        }
    }

    @Override
    public Optional<ServicioCatalogo> obtenerServicioPorId(Integer idServicio) {
        try {
            CatalogServicioFeignDTO dto = catalogFeignClient.obtenerServicioPorId(idServicio);
            if (dto == null) {
                return Optional.empty();
            }
            return Optional.of(new ServicioCatalogo(
                    dto.idServicio(),
                    dto.nombre(),
                    dto.descripcion(),
                    dto.precioSugerido()
            ));
        } catch (FeignException.NotFound ex) {
            return Optional.empty();
        } catch (FeignException ex) {
            throw traducirExcepcionCatalogo(ex);
        }
    }

    private RuntimeException traducirExcepcionCatalogo(FeignException ex) {
        String cuerpo = ex.contentUTF8() != null && !ex.contentUTF8().isBlank() ? ex.contentUTF8() : ex.getMessage();
        if (ex.status() == 404) {
            return new RecursoNoEncontradoException("Recurso no encontrado en catalog-service: " + cuerpo);
        }
        if (ex.status() == 400 || ex.status() == 422) {
            if (cuerpo != null && cuerpo.toLowerCase().contains("stock insuficiente")) {
                return new StockInsuficienteException("Stock insuficiente reportado por catalog-service: " + cuerpo);
            }
            return new ReglaNegocioException("Validación rechazada por catalog-service: " + cuerpo);
        }
        return new ServicioExternoNoDisponibleException(
                "Error de comunicación con catalog-service (status " + ex.status() + ")", ex);
    }
}
