# Roadmap — Features futuras

Funcionalidad orientada al negocio de paquetería, más allá del MVP actual
(bodega a bodega: auth, paquetes, tracking, catálogo, ubicaciones, inventario,
usuarios y alertas). Ordenado por prioridad de despliegue.

## Nivel 1 — Operación logística (transporte)
- **Shipment vs Package**: un envío con 1..N paquetes (manifiesto).
- **Rutas, vehículos y conductores/mensajeros**: asignación de envíos.
- **Manifiestos de carga**, órdenes de *pickup* y entrega con ventana horaria.
- **Prueba de entrega (POD)**: firma/foto, `receivedBy`, intentos y razón de fallo.
- **Etiquetas**: generación de *label* (PDF/ZPL) con tracking y barcode.

## Nivel 2 — Dominio de negocio
- **Remitente y destinatario** como entidades (nombre, teléfono, email, dirección de entrega), separados de `ownerUser` y `Place` (hoy `Place` = bodegas).
- **SLA / fechas de negocio**: `estimatedDeliveryAt`, `pickedUpAt`, `deliveredAt`; detección de "paquete demorado".
- **Tarificación**: peso volumétrico, seguros/fragilidad, moneda, estado de pago y tipo de servicio (standard/express/same-day).
- **Estados intermedios**: `PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERY_ATTEMPT_FAILED`, `RETURNED`.
- **Cancelación con motivo normalizado** (catálogo de motivos), además del texto libre.

## Nivel 3 — Integración y notificaciones
- Notificaciones reales (email/SMS/push) ante cambios de estado y alertas.
- Webhooks para integraciones (e-commerce / marketplaces).
- Migrar de WebSocket a **Server-Sent Events** (solo notificaciones del servidor).
- Generar los eventos que disparan esas notificaciones.