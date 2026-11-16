-- ==============================================================================
-- InvenGest_POS — Script Unificado de Base de Datos (PostgreSQL 16)
-- Base de datos única: invengest_pos
-- Esquemas aislados por dominio: auth, catalog, transaction, analytics
-- REGLA ARQUITECTÓNICA: CERO llaves foráneas (FOREIGN KEY) entre esquemas distintos.
-- Las referencias entre dominios son "Soft References" validadas vía OpenFeign.
-- ==============================================================================

-- Creación idempotente de los 4 esquemas de negocio
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS transaction;
CREATE SCHEMA IF NOT EXISTS analytics;

-- ==============================================================================
-- 1. ESQUEMA: auth (Autenticación, Roles y Usuarios)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS auth.rol_usuario (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS auth.usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    apellido VARCHAR(150) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    ultimo_acceso TIMESTAMP,
    id_rol INT NOT NULL REFERENCES auth.rol_usuario(id_rol),
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_auth_usuario_rol ON auth.usuario(id_rol);
CREATE INDEX IF NOT EXISTS idx_auth_usuario_activo ON auth.usuario(activo);

INSERT INTO auth.rol_usuario (nombre)
VALUES ('Administrador'), ('Vendedor')
ON CONFLICT (nombre) DO NOTHING;

-- ==============================================================================
-- 2. ESQUEMA: catalog (Productos, Servicios, Proveedores e Inventario Maestro)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS catalog.categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS catalog.estado_producto (
    id_estado SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS catalog.estado_proveedor (
    id_estado SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS catalog.categoria_servicio (
    id_categoria_servicio SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS catalog.proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    id_estado INT NOT NULL REFERENCES catalog.estado_proveedor(id_estado),
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    nit VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS catalog.producto (
    id_producto VARCHAR(50) PRIMARY KEY,
    id_categoria INT NOT NULL REFERENCES catalog.categoria(id_categoria),
    nombre VARCHAR(150) NOT NULL,
    precio_venta DECIMAL(12, 2) NOT NULL,
    costo_compra DECIMAL(12, 2) NOT NULL,
    fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,
    stock SMALLINT NOT NULL DEFAULT 0,
    stock_minimo SMALLINT NOT NULL DEFAULT 0,
    ubicacion VARCHAR(100),
    id_estado INT NOT NULL REFERENCES catalog.estado_producto(id_estado)
);

CREATE TABLE IF NOT EXISTS catalog.servicio (
    id_servicio SERIAL PRIMARY KEY,
    id_categoria_servicio INT NOT NULL REFERENCES catalog.categoria_servicio(id_categoria_servicio),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio_sugerido DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS catalog.producto_proveedor (
    id_producto VARCHAR(50) REFERENCES catalog.producto(id_producto) ON DELETE CASCADE,
    id_proveedor INT REFERENCES catalog.proveedor(id_proveedor) ON DELETE CASCADE,
    precio_compra_proveedor DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id_producto, id_proveedor)
);

-- Índices de rendimiento (< 500 ms) para búsquedas y alertas de stock
CREATE INDEX IF NOT EXISTS idx_catalog_producto_nombre_lower ON catalog.producto (LOWER(nombre));
CREATE INDEX IF NOT EXISTS idx_catalog_producto_categoria ON catalog.producto (id_categoria);
CREATE INDEX IF NOT EXISTS idx_catalog_producto_estado ON catalog.producto (id_estado);
CREATE INDEX IF NOT EXISTS idx_catalog_producto_stock ON catalog.producto (stock, stock_minimo);
CREATE INDEX IF NOT EXISTS idx_catalog_prod_prov_precio ON catalog.producto_proveedor (id_producto, precio_compra_proveedor);

INSERT INTO catalog.estado_producto (nombre)
VALUES ('Activo'), ('Descontinuado'), ('Agotado')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO catalog.estado_proveedor (nombre)
VALUES ('Activo'), ('Inactivo')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO catalog.categoria_servicio (nombre)
VALUES ('Deshabilitados'), ('Redacción y Documentos'), ('Trámites Digitales')
ON CONFLICT (nombre) DO NOTHING;

-- ==============================================================================
-- 3. ESQUEMA: transaction (Ventas POS, Anulaciones, Kardex y Cierre de Caja)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS transaction.tipo_documento (
    id_tipo_documento SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS transaction.cliente (
    id_cliente SERIAL PRIMARY KEY,
    id_tipo_documento INT REFERENCES transaction.tipo_documento(id_tipo_documento) ON DELETE SET NULL,
    documento_identidad VARCHAR(50) UNIQUE,
    primer_nombre VARCHAR(50) NOT NULL,
    segundo_nombre VARCHAR(50),
    primer_apellido VARCHAR(50) NOT NULL,
    segundo_apellido VARCHAR(50),
    telefono VARCHAR(20),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS transaction.metodo_pago (
    id_metodo_pago SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS transaction.tipo_movimiento (
    id_movimiento SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS transaction.venta (
    id_venta SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_cliente INT REFERENCES transaction.cliente(id_cliente) ON DELETE SET NULL,
    id_metodo_pago INT NOT NULL REFERENCES transaction.metodo_pago(id_metodo_pago),
    total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    fecha_anulacion TIMESTAMP,
    id_usuario_anulacion INT,
    motivo_anulacion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS transaction.detalle_venta (
    id_detalle_venta SERIAL PRIMARY KEY,
    id_venta INT NOT NULL REFERENCES transaction.venta(id_venta) ON DELETE CASCADE,
    id_item VARCHAR(50) NOT NULL,
    tipo_item VARCHAR(20) NOT NULL CHECK (tipo_item IN ('PRODUCTO', 'SERVICIO')),
    nombre_item VARCHAR(150) NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction.movimiento_inventario (
    id_movimiento_inventario SERIAL PRIMARY KEY,
    id_producto VARCHAR(50) NOT NULL,
    id_usuario INT NOT NULL,
    id_movimiento INT NOT NULL REFERENCES transaction.tipo_movimiento(id_movimiento),
    cantidad INT NOT NULL,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS transaction.cierre_caja (
    id_cierre SERIAL PRIMARY KEY,
    fecha_cierre DATE NOT NULL UNIQUE,
    id_usuario INT NOT NULL,
    total_ventas DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cantidad_ventas INT NOT NULL DEFAULT 0,
    efectivo_esperado DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    efectivo_contado DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    diferencia DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    es_dia_habil BOOLEAN NOT NULL DEFAULT TRUE,
    proximo_dia_habil DATE,
    observacion VARCHAR(255),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transaction.cierre_caja_detalle (
    id_detalle SERIAL PRIMARY KEY,
    id_cierre INT NOT NULL REFERENCES transaction.cierre_caja(id_cierre) ON DELETE CASCADE,
    id_metodo_pago INT NOT NULL REFERENCES transaction.metodo_pago(id_metodo_pago),
    total_metodo DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cantidad_operaciones INT NOT NULL DEFAULT 0
);

-- Índices de rendimiento (< 500 ms) para ventas, cierres y Kardex
CREATE INDEX IF NOT EXISTS idx_transaction_venta_fecha ON transaction.venta (fecha);
CREATE INDEX IF NOT EXISTS idx_transaction_venta_usuario ON transaction.venta (id_usuario);
CREATE INDEX IF NOT EXISTS idx_transaction_venta_estado ON transaction.venta (estado);
CREATE INDEX IF NOT EXISTS idx_transaction_detalle_venta_id_venta ON transaction.detalle_venta (id_venta);
CREATE INDEX IF NOT EXISTS idx_transaction_kardex_producto_fecha ON transaction.movimiento_inventario (id_producto, fecha_movimiento DESC);

-- Documentación explícita de Soft References (sin FK cruzada entre esquemas)
COMMENT ON COLUMN transaction.venta.id_usuario IS 'Soft reference -> auth.usuario(id_usuario) resuelta vía OpenFeign';
COMMENT ON COLUMN transaction.venta.id_usuario_anulacion IS 'Soft reference -> auth.usuario(id_usuario) resuelta vía OpenFeign';
COMMENT ON COLUMN transaction.detalle_venta.id_item IS 'Soft reference -> catalog.producto(id_producto) o catalog.servicio(id_servicio) vía OpenFeign';
COMMENT ON COLUMN transaction.movimiento_inventario.id_producto IS 'Soft reference -> catalog.producto(id_producto) resuelta vía OpenFeign';
COMMENT ON COLUMN transaction.movimiento_inventario.id_usuario IS 'Soft reference -> auth.usuario(id_usuario) resuelta vía OpenFeign';
COMMENT ON COLUMN transaction.cierre_caja.id_usuario IS 'Soft reference -> auth.usuario(id_usuario) resuelta vía OpenFeign';

INSERT INTO transaction.tipo_documento (nombre)
VALUES ('Cédula de Ciudadanía'), ('Cédula de Extranjería'), ('NIT'), ('Pasaporte')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO transaction.cliente (id_tipo_documento, documento_identidad, primer_nombre, primer_apellido)
SELECT NULL, NULL, 'Consumidor', 'Final'
WHERE NOT EXISTS (
    SELECT 1 FROM transaction.cliente WHERE primer_nombre = 'Consumidor' AND primer_apellido = 'Final'
);

INSERT INTO transaction.metodo_pago (nombre)
VALUES ('Efectivo'), ('Transferencia'), ('Tarjeta de Crédito'), ('Tarjeta de Débito')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO transaction.tipo_movimiento (nombre)
VALUES ('Ingreso por Compra'),
       ('Salida por Venta'),
       ('Ajuste por Pérdida/Daño'),
       ('Ajuste por Conteo Físico'),
       ('Anulación de Venta'),
       ('Ajuste Positivo por Conteo')
ON CONFLICT (nombre) DO NOTHING;

-- ==============================================================================
-- 4. ESQUEMA: analytics (Proyecciones CQRS, Dashboard, Top Productos y Alertas)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS analytics.resumen_ventas_diarias (
    fecha DATE PRIMARY KEY,
    total_ventas DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_anulado DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    cantidad_operaciones INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS analytics.top_productos (
    id_top SERIAL PRIMARY KEY,
    id_producto VARCHAR(50) NOT NULL,
    nombre_producto VARCHAR(150) NOT NULL,
    cantidad_vendida INT NOT NULL,
    fecha DATE NOT NULL,
    CONSTRAINT uq_top_productos_fecha_producto UNIQUE (fecha, id_producto)
);

CREATE TABLE IF NOT EXISTS analytics.alertas_stock (
    id_alerta SERIAL PRIMARY KEY,
    id_producto VARCHAR(50) NOT NULL UNIQUE,
    nombre_producto VARCHAR(150) NOT NULL,
    stock_actual SMALLINT NOT NULL,
    stock_minimo SMALLINT NOT NULL DEFAULT 0,
    estado VARCHAR(50) NOT NULL CHECK (estado IN ('Agotado', 'Bajo Stock')),
    fecha_alerta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices de rendimiento (< 500 ms) para analítica y reportes
CREATE INDEX IF NOT EXISTS idx_analytics_top_productos_fecha ON analytics.top_productos (fecha);
CREATE INDEX IF NOT EXISTS idx_analytics_alertas_fecha ON analytics.alertas_stock (fecha_alerta DESC);

-- Documentación explícita de Soft References en analytics
COMMENT ON COLUMN analytics.top_productos.id_producto IS 'Soft reference -> catalog.producto(id_producto) proyectada vía OpenFeign';
COMMENT ON COLUMN analytics.alertas_stock.id_producto IS 'Soft reference -> catalog.producto(id_producto) proyectada vía OpenFeign';
