package com.invengest.analytics.application;

import com.invengest.analytics.domain.model.CalendarioLaboral;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.analytics.domain.model.gateway.ReporteExcelGateway;
import com.invengest.analytics.domain.model.gateway.ReportePdfGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TasaCambioGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;
import com.invengest.analytics.domain.usecase.ConsultarAlertasStockUseCase;
import com.invengest.analytics.domain.usecase.ConsultarDashboardUseCase;
import com.invengest.analytics.domain.usecase.ConsultarEstacionalidadUseCase;
import com.invengest.analytics.domain.usecase.ConsultarTopProductosUseCase;
import com.invengest.analytics.domain.usecase.GenerarReporteControlVentasUseCase;
import com.invengest.analytics.domain.usecase.SincronizarAnaliticaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CalendarioLaboral calendarioLaboral(CalendarioFestivosGateway calendarioFestivosGateway) {
        return new CalendarioLaboral(calendarioFestivosGateway);
    }

    @Bean
    public SincronizarAnaliticaUseCase sincronizarAnaliticaUseCase(
            ResumenVentaGateway resumenVentaGateway,
            TopProductoGateway topProductoGateway,
            AlertaStockGateway alertaStockGateway) {
        return new SincronizarAnaliticaUseCase(
                resumenVentaGateway,
                topProductoGateway,
                alertaStockGateway
        );
    }

    @Bean
    public ConsultarDashboardUseCase consultarDashboardUseCase(
            ResumenVentaGateway resumenVentaGateway,
            TopProductoGateway topProductoGateway,
            AlertaStockGateway alertaStockGateway) {
        return new ConsultarDashboardUseCase(
                resumenVentaGateway,
                topProductoGateway,
                alertaStockGateway
        );
    }

    @Bean
    public ConsultarTopProductosUseCase consultarTopProductosUseCase(
            TopProductoGateway topProductoGateway) {
        return new ConsultarTopProductosUseCase(topProductoGateway);
    }

    @Bean
    public ConsultarAlertasStockUseCase consultarAlertasStockUseCase(
            AlertaStockGateway alertaStockGateway) {
        return new ConsultarAlertasStockUseCase(alertaStockGateway);
    }

    @Bean
    public GenerarReporteControlVentasUseCase generarReporteControlVentasUseCase(
            ResumenVentaGateway resumenVentaGateway,
            TopProductoGateway topProductoGateway,
            AlertaStockGateway alertaStockGateway,
            ReporteExcelGateway reporteExcelGateway,
            ReportePdfGateway reportePdfGateway,
            TasaCambioGateway tasaCambioGateway) {
        return new GenerarReporteControlVentasUseCase(
                resumenVentaGateway,
                topProductoGateway,
                alertaStockGateway,
                reporteExcelGateway,
                reportePdfGateway,
                tasaCambioGateway
        );
    }

    @Bean
    public ConsultarEstacionalidadUseCase consultarEstacionalidadUseCase(
            ResumenVentaGateway resumenVentaGateway,
            CalendarioFestivosGateway calendarioFestivosGateway) {
        return new ConsultarEstacionalidadUseCase(
                resumenVentaGateway,
                calendarioFestivosGateway
        );
    }
}
