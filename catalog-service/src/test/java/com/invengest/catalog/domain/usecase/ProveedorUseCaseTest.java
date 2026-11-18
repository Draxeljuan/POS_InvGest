package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorUseCaseTest {

    @Mock
    private ProveedorGateway proveedorGateway;
    @Mock
    private EstadoProveedorGateway estadoProveedorGateway;

    private ProveedorUseCase proveedorUseCase;

    @BeforeEach
    void setUp() {
        proveedorUseCase = new ProveedorUseCase(proveedorGateway, estadoProveedorGateway);
    }

    @Test
    @DisplayName("Gestiona CRUD y eliminación lógica usando estado Inactivo por nombre")
    void eliminacionLogicaUsaEstadoInactivoPorNombreSinMagicNumbers() {
        // Arrange
        EstadoProveedor activo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor inactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);
        Proveedor proveedor = new Proveedor(10, activo, "Norma", "300000", "info@norma.com", "Calle 1", "900111");
        Proveedor sinEstado = new Proveedor(10, null, "Norma", "300000", "info@norma.com", "Calle 1", "900111");

        when(proveedorGateway.findByNit("900111")).thenReturn(Optional.empty());
        when(estadoProveedorGateway.findById(1)).thenReturn(Optional.of(activo));
        when(estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_ACTIVO)).thenReturn(Optional.of(activo));
        when(proveedorGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(proveedorGateway.findById(10)).thenReturn(Optional.of(proveedor));
        when(proveedorGateway.findAll()).thenReturn(List.of(proveedor));
        when(proveedorGateway.existsProductosByProveedorId(10)).thenReturn(false);
        when(estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_INACTIVO)).thenReturn(Optional.of(inactivo));

        // Act
        Proveedor creado = proveedorUseCase.crear(proveedor);
        Proveedor actualizado = proveedorUseCase.actualizar(sinEstado);
        Proveedor porId = proveedorUseCase.obtenerPorId(10);
        List<Proveedor> todos = proveedorUseCase.obtenerTodos();
        proveedorUseCase.eliminacionLogica(10);

        // Assert
        assertEquals("Norma", creado.getNombre());
        assertEquals("Norma", actualizado.getNombre());
        assertEquals(10, porId.getIdProveedor());
        assertEquals(1, todos.size());
        assertEquals(EstadoProveedor.ESTADO_INACTIVO, proveedor.getEstado().getNombre());

        when(proveedorGateway.existsProductosByProveedorId(10)).thenReturn(true);
        assertThrows(EntidadEnUsoException.class, () -> proveedorUseCase.eliminacionLogica(10));

        when(proveedorGateway.findByNit("900111")).thenReturn(Optional.of(proveedor));
        assertThrows(EntidadDuplicadaException.class, () -> proveedorUseCase.crear(proveedor));
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException cuando proveedor o estado no existe")
    void lanzaExcepcionCuandoProveedorOEstadoNoExiste() {
        // Arrange
        Proveedor sinId = new Proveedor(null, null, "Norma", null, null, null, null);
        when(proveedorGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> proveedorUseCase.actualizar(sinId));
        assertThrows(RecursoNoEncontradoException.class, () -> proveedorUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> proveedorUseCase.eliminacionLogica(99));
    }
}
