package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.gateway.ProductoGateway;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductoDataGatewayImpl implements ProductoGateway {

    private final ProductoJpaRepository productoJpaRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> findById(String idProducto) {
        return productoJpaRepository.findById(idProducto).map(productoMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findByIds(List<String> idsProductos) {
        return productoJpaRepository.findAllById(idsProductos).stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoJpaRepository.findAll().stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findBySearchCriteria(String query) {
        return productoJpaRepository.searchByCriteria(query).stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findLowStockProducts() {
        return productoJpaRepository.findLowStockProducts().stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Producto save(Producto producto) {
        ProductoData entity = productoMapper.toEntity(producto);
        return productoMapper.toDomain(productoJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String idProducto) {
        return productoJpaRepository.existsById(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public String generarSiguienteIdProducto() {
        long siguienteConsecutivo = productoJpaRepository.count() + 1;
        String candidato = String.format("PROD-%04d", siguienteConsecutivo);
        if (!productoJpaRepository.existsById(candidato)) {
            return candidato;
        }
        String sufijoUnico = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PROD-" + sufijoUnico;
    }

    @Override
    @Transactional
    public List<Producto> descontarStockAtomico(List<SolicitudItemStock> items, EstadoProducto estadoAgotado) {
        Map<String, Producto> productosPorId = cargarProductosParaLote(items);

        for (SolicitudItemStock item : items) {
            Producto producto = productosPorId.get(item.idProducto());
            producto.descontarStock(item.cantidad(), estadoAgotado);
        }

        List<ProductoData> entidades = productosPorId.values().stream()
                .map(productoMapper::toEntity)
                .toList();

        return productoJpaRepository.saveAll(entidades).stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<Producto> reintegrarStockAtomico(List<SolicitudItemStock> items, EstadoProducto estadoActivo) {
        Map<String, Producto> productosPorId = cargarProductosParaLote(items);

        for (SolicitudItemStock item : items) {
            Producto producto = productosPorId.get(item.idProducto());
            producto.reintegrarStock(item.cantidad(), estadoActivo);
        }

        List<ProductoData> entidades = productosPorId.values().stream()
                .map(productoMapper::toEntity)
                .toList();

        return productoJpaRepository.saveAll(entidades).stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    private Map<String, Producto> cargarProductosParaLote(List<SolicitudItemStock> items) {
        List<String> ids = new ArrayList<>();
        for (SolicitudItemStock item : items) {
            if (!ids.contains(item.idProducto())) {
                ids.add(item.idProducto());
            }
        }

        Map<String, Producto> encontrados = new LinkedHashMap<>();
        for (ProductoData data : productoJpaRepository.findAllById(ids)) {
            Producto prod = productoMapper.toDomain(data);
            encontrados.put(prod.getIdProducto(), prod);
        }

        for (String id : ids) {
            if (!encontrados.containsKey(id)) {
                throw new RecursoNoEncontradoException("Producto no encontrado con el código: " + id);
            }
        }
        return encontrados;
    }
}
