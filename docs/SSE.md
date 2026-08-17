# Server-Sent Events (SSE) — Notificaciones en tiempo real

El backend emite **notificaciones en tiempo real** al dueño de un paquete (y a los
destinatarios de alertas) usando **Server-Sent Events (SSE)** en lugar de WebSocket.

## ¿Por qué SSE y no WebSocket?

SSE es una conexión unidireccional **servidor → cliente** sobre HTTP. Encaja con el
caso de uso: el sistema **solo notifica** (un paquete cambió de estado, se creó una
alerta), no hay un chat bidireccional que justifique WebSocket.

Ventajas:

- Más simple: HTTP plano, sin protocolo propio.
- Reintento y reconexión automática del navegador (`EventSource`).
- Pasa por proxys/balanceadores igual que cualquier petición HTTP.

## Endpoint

```
GET /events
Authorization: Bearer <accessToken>
Accept: text/event-stream
```

- Requiere **autenticación** (JWT válido). Sin token/inválido → `401`.
- El usuario se identifica por el JWT: la conexión queda suscrita a los eventos de
  **su propio** `userId`.
- Como `EventSource` nativo **no puede mandar headers**, el frontend debe usar
  `fetch` con `Authorization` (o un polyfill). Ver ejemplos abajo.

## Eventos emitidos

Cada evento llega como un bloque SSE con un nombre (`event:`) y un payload JSON
(`data:`):

```
event: package.status-changed
data: { "uuid": "...", "trackingCode": "...", "status": { "code": "IN_TRANSIT", ... }, ... }
```

| Evento | Payload | Destinatario |
|---|---|---|
| `package.created` | `PackageResponse` | `ownerUser` del paquete |
| `package.proposed` | `PackageResponse` | `ownerUser` |
| `package.approved` | `PackageResponse` | `ownerUser` |
| `package.rejected` | `PackageResponse` | `ownerUser` |
| `package.reactivated` | `PackageResponse` | `ownerUser` |
| `package.updated` | `PackageResponse` | `ownerUser` |
| `package.cancelled` | `PackageResponse` | `ownerUser` |
| `package.status-changed` | `PackageResponse` | `ownerUser` |
| `alert.created` | `AlertResponse` | usuario destinatario de la alerta |

## Cómo se implementa

- `sse.SseEmitterService`: registra un `SseEmitter` por `userId` y publica eventos
  (serializa el payload a JSON con `ObjectMapper`). Limpia el emisor en
  `onCompletion` / `onTimeout` / `onError`.
- `sse.SseController`: endpoint `GET /events` (protegido en `SecurityConfig` con
  `.requestMatchers("/events/**").authenticated()`).
- `PackageService`: publica el evento correspondiente tras cada mutación
  (`create`, `propose`, `approve`, `reject`, `reactivate`, `update`, `cancel`,
  `changeStatus`) al `ownerUser`.
- `AlertService.createAlert`: publica `alert.created` al destinatario.

## Ejemplo de cliente (JavaScript)

### Con `fetch` (recomendado, permite header `Authorization`)

```js
const token = localStorage.getItem("accessToken");

fetch("/events", { headers: { Authorization: `Bearer ${token}` } })
  .then((response) => {
    if (!response.ok) throw new Error("No autorizado");
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    function read() {
      return reader.read().then(({ done, value }) => {
        if (done) return;
        buffer += decoder.decode(value, { stream: true });
        // separa por eventos (bloques terminados en \n\n)
        const events = buffer.split("\n\n");
        buffer = events.pop();
        events.forEach(parseEvent);
        return read();
      });
    }

    return read();
  });

function parseEvent(raw) {
  const name = /^event: (.*)$/m.exec(raw)?.[1];
  const data = /^data: (.*)$/m.exec(raw)?.[1];
  if (name && data) {
    console.log(name, JSON.parse(data));
  }
}
```

### Con `EventSource` (requiere polyfill o token en cookie)

El `EventSource` nativo no permite el header `Authorization`. Si lo necesitas,
usa un polyfill (p. ej. `event-source-polyfill`) o el ejemplo de `fetch` de arriba:

```js
import { EventSourcePolyfill } from "event-source-polyfill";

const es = new EventSourcePolyfill("/events", {
  headers: { Authorization: `Bearer ${localStorage.getItem("accessToken")}` },
});

es.addEventListener("package.status-changed", (e) => {
  console.log(JSON.parse(e.data));
});

es.addEventListener("alert.created", (e) => {
  console.log(JSON.parse(e.data));
});
```

## Notas

- La conexión se identifica por el **JWT**: si el token expira, el cliente debe
  reconectar con un token renovado.
- No hay heartbeat explícito: la conexión queda abierta hasta que el cliente la
  cierra o el token expira. Para entornos con proxys agresivos conviene añadir
  un heartbeat periódico (comentario `: ping`).
- Los eventos se entregan en memoria (un `SseEmitter` por `userId`). Para
  escalar a múltiples instancias se requiere un broker (Redis pub/sub / Kafka),
  listado en `docs/ROADMAP.md`.
