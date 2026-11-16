package com.invengest.catalog.application;

import com.invengest.catalog.domain.model.gateway.AnalyticsGateway;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.NotificacionStockGateway;
import com.invengest.catalog.domain.model.gateway.ProductoGateway;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ServicioGateway;
import com.invengest.catalog.domain.model.gateway.TasaCambioGateway;
import com.invengest.catalog.domain.usecase.CategoriaServicioUseCase;
import com.invengest.catalog.domain.usecase.CategoriaUseCase;
import com.invengest.catalog.domain.usecase.EstadoProductoUseCase;
import com.invengest.catalog.domain.usecase.EstadoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProductoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.domain.usecase.ProveedorUseCase;
import com.invengest.catalog.domain.usecase.ServicioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CategoriaUseCase categoriaUseCase(CategoriaGateway categoriaGateway) {
        return new CategoriaUseCase(categoriaGateway);
    }

    @Bean
    public ProductoUseCase productoUseCase(
            ProductoGateway productoGateway,
            EstadoProductoGateway estadoProductoGateway,
            CategoriaGateway categoriaGateway,
            ProductoProveedorGateway productoProveedorGateway,
            ProveedorGateway proveedorGateway,
            TasaCambioGateway tasaCambioGateway,
            AnalyticsGateway analyticsGateway,
            NotificacionStockGateway notificacionStockGateway) {
        return new ProductoUseCase(
                productoGateway,
                estadoProductoGateway,
                categoriaGateway,
                productoProveedorGateway,
                proveedorGateway,
                tasaCambioGateway,
                analyticsGateway,
                notificacionStockGateway
        );
    }

    @Bean
    public ProveedorUseCase proveedorUseCase(
            ProveedorGateway proveedorGateway,
            EstadoProveedorGateway estadoProveedorGateway) {
        return new ProveedorUseCase(proveedorGateway, estadoProveedorGateway);
    }

    @Bean
    public ServicioUseCase servicioUseCase(
            ServicioGateway servicioGateway,
            CategoriaServicioGateway categoriaServicioGateway) {
        return new ServicioUseCase(servicioGateway, categoriaServicioGateway);
    }

    @Bean
    public CategoriaServicioUseCase categoriaServicioUseCase(CategoriaServicioGateway categoriaServicioGateway) {
        return new CategoriaServicioUseCase(categoriaServicioGateway);
    }

    @Bean
    public ProductoProveedorUseCase productoProveedorUseCase(ProductoProveedorGateway productoProveedorGateway) {
        return new ProductoProveedorUseCase(productoProveedorGateway);
    }

    @Bean
    public EstadoProductoUseCase estadoProductoUseCase(EstadoProductoGateway estadoProductoGateway) {
        return new EstadoProductoUseCase(estadoProductoGateway);
    }

    @Bean
    public EstadoProveedorUseCase estadoProveedorUseCase(EstadoProveedorGateway estadoProveedorGateway) {
        return new EstadoProveedorUseCase(estadoProveedorGateway);
    }
}
