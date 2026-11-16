package com.invengest.transaction.application;

import com.invengest.transaction.domain.model.CalendarioLaboral;
import com.invengest.transaction.domain.model.gateway.AnalyticsGateway;
import com.invengest.transaction.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.CierreCajaGateway;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import com.invengest.transaction.domain.usecase.AnularVentaUseCase;
import com.invengest.transaction.domain.usecase.CajaUseCase;
import com.invengest.transaction.domain.usecase.ClienteUseCase;
import com.invengest.transaction.domain.usecase.ComprobanteVentaUseCase;
import com.invengest.transaction.domain.usecase.ConsultarVentaUseCase;
import com.invengest.transaction.domain.usecase.MetodoPagoUseCase;
import com.invengest.transaction.domain.usecase.MovimientoInventarioUseCase;
import com.invengest.transaction.domain.usecase.RegistrarVentaUseCase;
import com.invengest.transaction.domain.usecase.TipoDocumentoUseCase;
import com.invengest.transaction.domain.usecase.TipoMovimientoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CalendarioLaboral calendarioLaboral(CalendarioFestivosGateway calendarioFestivosGateway) {
        return new CalendarioLaboral(calendarioFestivosGateway);
    }

    @Bean
    public RegistrarVentaUseCase registrarVentaUseCase(
            VentaGateway ventaGateway,
            CatalogGateway catalogGateway,
            MovimientoInventarioGateway movimientoInventarioGateway,
            ClienteGateway clienteGateway,
            MetodoPagoGateway metodoPagoGateway,
            TipoMovimientoGateway tipoMovimientoGateway,
            AnalyticsGateway analyticsGateway,
            NotificacionGateway notificacionGateway) {
        return new RegistrarVentaUseCase(
                ventaGateway,
                catalogGateway,
                movimientoInventarioGateway,
                clienteGateway,
                metodoPagoGateway,
                tipoMovimientoGateway,
                analyticsGateway,
                notificacionGateway
        );
    }

    @Bean
    public AnularVentaUseCase anularVentaUseCase(
            VentaGateway ventaGateway,
            CatalogGateway catalogGateway,
            MovimientoInventarioGateway movimientoInventarioGateway,
            TipoMovimientoGateway tipoMovimientoGateway,
            AnalyticsGateway analyticsGateway) {
        return new AnularVentaUseCase(
                ventaGateway,
                catalogGateway,
                movimientoInventarioGateway,
                tipoMovimientoGateway,
                analyticsGateway
        );
    }

    @Bean
    public ConsultarVentaUseCase consultarVentaUseCase(VentaGateway ventaGateway) {
        return new ConsultarVentaUseCase(ventaGateway);
    }

    @Bean
    public ComprobanteVentaUseCase comprobanteVentaUseCase(
            VentaGateway ventaGateway,
            NotificacionGateway notificacionGateway) {
        return new ComprobanteVentaUseCase(ventaGateway, notificacionGateway);
    }

    @Bean
    public CajaUseCase cajaUseCase(
            VentaGateway ventaGateway,
            CierreCajaGateway cierreCajaGateway,
            CalendarioLaboral calendarioLaboral,
            NotificacionGateway notificacionGateway) {
        return new CajaUseCase(
                ventaGateway,
                cierreCajaGateway,
                calendarioLaboral,
                notificacionGateway
        );
    }

    @Bean
    public MovimientoInventarioUseCase movimientoInventarioUseCase(
            MovimientoInventarioGateway movimientoInventarioGateway,
            TipoMovimientoGateway tipoMovimientoGateway,
            CatalogGateway catalogGateway) {
        return new MovimientoInventarioUseCase(
                movimientoInventarioGateway,
                tipoMovimientoGateway,
                catalogGateway
        );
    }

    @Bean
    public ClienteUseCase clienteUseCase(
            ClienteGateway clienteGateway,
            TipoDocumentoGateway tipoDocumentoGateway) {
        return new ClienteUseCase(clienteGateway, tipoDocumentoGateway);
    }

    @Bean
    public MetodoPagoUseCase metodoPagoUseCase(MetodoPagoGateway metodoPagoGateway) {
        return new MetodoPagoUseCase(metodoPagoGateway);
    }

    @Bean
    public TipoDocumentoUseCase tipoDocumentoUseCase(TipoDocumentoGateway tipoDocumentoGateway) {
        return new TipoDocumentoUseCase(tipoDocumentoGateway);
    }

    @Bean
    public TipoMovimientoUseCase tipoMovimientoUseCase(TipoMovimientoGateway tipoMovimientoGateway) {
        return new TipoMovimientoUseCase(tipoMovimientoGateway);
    }
}
