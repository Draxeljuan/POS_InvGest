package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.domain.model.ValidacionItemStock;
import com.invengest.catalog.domain.model.gateway.AnalyticsGateway;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import com.invengest.catalog.domain.model.gateway.NotificacionStockGateway;
import com.invengest.catalog.domain.model.gateway.ProductoGateway;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import com.invengest.catalog.domain.model.gateway.TasaCambioGateway;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductoUseCase {

    private final ProductoGateway productoGateway;
    private final EstadoProductoGateway estadoProductoGateway;
    private final CategoriaGateway categoriaGateway;
    private final ProductoProveedorGateway productoProveedorGateway;
    private final ProveedorGateway proveedorGateway;
    private final TasaCambioGateway tasaCambioGateway;
    private final AnalyticsGateway analyticsGateway;
    private final NotificacionStockGateway notificacionStockGateway;

    public Producto registrar(Producto producto) {
        if (producto == null) {
            throw new ReglaNegocioException("Los datos del producto son obligatorios.");
        }
        producto.validarInvariantes();
        resolverCategoriaYEstado(producto);

        if (producto.getIdProducto() != null && !producto.getIdProducto().isBlank()) {
            String sku = producto.getIdProducto().trim();
            if (productoGateway.existsById(sku)) {
                throw new EntidadDuplicadaException("Ya existe un producto registrado con el código: " + sku);
            }
            producto.setIdProducto(sku);
        } else {
            producto.setIdProducto(productoGateway.generarSiguienteIdProducto());
        }

        Producto guardado = productoGateway.save(producto);
        notificarSiStockBajo(List.of(guardado));
        return guardado;
    }

    public Producto actualizar(Producto producto) {
        if (producto == null || producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
            throw new ReglaNegocioException("El código del producto es obligatorio para actualizar.");
        }
        if (!productoGateway.existsById(producto.getIdProducto())) {
            throw new RecursoNoEncontradoException("El producto con código " + producto.getIdProducto() + " no existe.");
        }
        producto.validarInvariantes();
        resolverCategoriaYEstado(producto);

        Producto guardado = productoGateway.save(producto);
        notificarSiStockBajo(List.of(guardado));
        return guardado;
    }

    public List<Producto> obtenerTodos() {
        return productoGateway.findAll();
    }

    public Producto obtenerPorId(String idProducto) {
        return productoGateway.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con el código: " + idProducto));
    }

    public List<Producto> buscarPorCriterio(String query) {
        if (query == null || query.isBlank()) {
            return productoGateway.findAll();
        }
        return productoGateway.findBySearchCriteria(query.trim());
    }

    public List<Producto> obtenerProductosStockBajo() {
        return productoGateway.findLowStockProducts();
    }

    public List<ValidacionItemStock> validarStockBatch(List<SolicitudItemStock> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new ReglaNegocioException("La lista de productos a validar no puede estar vacía.");
        }
        List<String> ids = solicitudes.stream()
                .map(SolicitudItemStock::idProducto)
                .distinct()
                .toList();

        Map<String, Producto> productosPorId = productoGateway.findByIds(ids).stream()
                .collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));

        List<ValidacionItemStock> resultado = new ArrayList<>(solicitudes.size());
        for (SolicitudItemStock item : solicitudes) {
            Producto producto = productosPorId.get(item.idProducto());
            if (producto == null) {
                throw new RecursoNoEncontradoException("Producto no encontrado con el código: " + item.idProducto());
            }
            int disponible = producto.getStock() != null ? producto.getStock() : 0;
            boolean suficiente = producto.tieneStockSuficiente(item.cantidad());
            resultado.add(new ValidacionItemStock(
                    producto.getIdProducto(),
                    producto.getNombre(),
                    producto.getPrecioVenta(),
                    disponible,
                    item.cantidad(),
                    suficiente
            ));
        }
        return resultado;
    }

    public List<Producto> descontarStockBatch(List<SolicitudItemStock> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new ReglaNegocioException("La lista de productos a descontar no puede estar vacía.");
        }
        EstadoProducto estadoAgotado = estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_AGOTADO)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El estado '" + EstadoProducto.ESTADO_AGOTADO + "' no está configurado en el sistema."));

        List<Producto> actualizados = productoGateway.descontarStockAtomico(solicitudes, estadoAgotado);
        notificarSiStockBajo(actualizados);
        return actualizados;
    }

    public Producto descontarStock(String idProducto, Integer cantidad) {
        List<Producto> actualizados = descontarStockBatch(List.of(new SolicitudItemStock(idProducto, cantidad)));
        return actualizados.getFirst();
    }

    public List<Producto> reintegrarStockBatch(List<SolicitudItemStock> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new ReglaNegocioException("La lista de productos a reintegrar no puede estar vacía.");
        }
        EstadoProducto estadoActivo = estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_ACTIVO)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El estado '" + EstadoProducto.ESTADO_ACTIVO + "' no está configurado en el sistema."));

        return productoGateway.reintegrarStockAtomico(solicitudes, estadoActivo);
    }

    public void eliminacionLogica(String idProducto) {
        Producto producto = obtenerPorId(idProducto);
        EstadoProducto estadoDescontinuado = estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_DESCONTINUADO)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El estado '" + EstadoProducto.ESTADO_DESCONTINUADO + "' no está configurado en el sistema."));

        producto.descontinuar(estadoDescontinuado);
        productoGateway.save(producto);
    }

    public List<ProductoPrecioUsd> listarPreciosEnUsd() {
        TasaCambio trm = tasaCambioGateway.obtenerTrmVigente();
        return productoGateway.findAll().stream()
                .map(producto -> construirPrecioUsd(producto, trm))
                .toList();
    }

    public ProductoPrecioUsd obtenerPrecioEnUsd(String idProducto) {
        Producto producto = obtenerPorId(idProducto);
        TasaCambio trm = tasaCambioGateway.obtenerTrmVigente();
        return construirPrecioUsd(producto, trm);
    }

    public MejorProveedorProducto obtenerMejorProveedor(String idProducto) {
        Producto producto = obtenerPorId(idProducto);
        List<ProductoProveedor> asociaciones = productoProveedorGateway.findByProductoId(idProducto);
        if (asociaciones.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "El producto " + idProducto + " no tiene proveedores asociados.");
        }

        record CandidatoProveedor(ProductoProveedor asociacion, Proveedor proveedor) {}

        CandidatoProveedor mejorCandidato = asociaciones.stream()
                .map(asoc -> proveedorGateway.findById(asoc.getIdProveedor())
                        .map(prov -> new CandidatoProveedor(asoc, prov))
                        .orElse(null))
                .filter(c -> c != null && c.proveedor().estaActivo())
                .min(Comparator.comparing(c -> c.asociacion().getPrecioCompraProveedor()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El producto " + idProducto + " no cuenta con proveedores activos asociados."));

        Proveedor prov = mejorCandidato.proveedor();
        var costoProveedor = mejorCandidato.asociacion().getPrecioCompraProveedor();

        return new MejorProveedorProducto(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getPrecioVenta(),
                prov.getIdProveedor(),
                prov.getNombre(),
                prov.getNit(),
                prov.getTelefono(),
                prov.getEmail(),
                costoProveedor,
                producto.calcularMargenUnitario(costoProveedor),
                producto.calcularPorcentajeMargen(costoProveedor)
        );
    }

    private void resolverCategoriaYEstado(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null) {
            throw new RecursoNoEncontradoException("La categoría asignada no existe.");
        }
        Categoria categoria = categoriaGateway.findById(producto.getCategoria().getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría asignada no existe."));
        producto.setCategoria(categoria);

        if (producto.getEstado() != null && producto.getEstado().getIdEstado() != null) {
            EstadoProducto estado = estadoProductoGateway.findById(producto.getEstado().getIdEstado())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El estado del producto asignado no existe."));
            producto.setEstado(estado);
        } else {
            String nombreEstadoPorDefecto = (producto.getStock() != null && producto.getStock() == 0)
                    ? EstadoProducto.ESTADO_AGOTADO
                    : EstadoProducto.ESTADO_ACTIVO;
            EstadoProducto estado = estadoProductoGateway.findByNombre(nombreEstadoPorDefecto)
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El estado '" + nombreEstadoPorDefecto + "' no está configurado en el sistema."));
            producto.setEstado(estado);
        }
    }

    private void notificarSiStockBajo(List<Producto> productos) {
        List<Producto> enAlerta = productos.stream()
                .filter(p -> p != null && p.estaEnStockBajo())
                .toList();

        for (Producto producto : enAlerta) {
            analyticsGateway.notificarEstadoStock(producto);
        }
        if (!enAlerta.isEmpty()) {
            notificacionStockGateway.enviarAlertaStock(enAlerta);
        }
    }

    private ProductoPrecioUsd construirPrecioUsd(Producto producto, TasaCambio trm) {
        return new ProductoPrecioUsd(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getPrecioVenta(),
                trm.convertirAUsd(producto.getPrecioVenta()),
                trm.valorCopPorUsd(),
                trm.fechaVigencia(),
                trm.fuente(),
                producto.getStock(),
                producto.getEstado() != null ? producto.getEstado().getNombre() : null
        );
    }
}
