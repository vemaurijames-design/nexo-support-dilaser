# NEXO SUPPORT
## Especificación funcional y técnica — Dilaser S.A. / Soporte Técnico

Versión 1.0 · Septiembre 2026  
Stack: Java 21 · Spring Boot 3.3 · Spring Security + JWT · PostgreSQL 16 · React 18 + TypeScript · Vite · Tailwind + Ant Design / MUI  
IDE: IntelliJ IDEA (backend) + VS Code o IntelliJ (frontend)

Colores corporativos (logo):
- Primario oscuro: `#03738C`
- Primario: `#037F8C`
- Acento claro: `#9AD2D9`
- Secundario: `#1F736A`
- Fondo: `#F2F2F2`
- Texto: `#12363A`
- Danger: `#B42318`
- Warning: `#B54708`
- Success: `#1F736A`

---

## 1. Qué resuelve el sistema

Nexo Support unifica lo que hoy vive en Excel sueltos:

| Excel actual | Módulo Nexo |
|---|---|
| BD Clientes y equipos 2015-2025 | Clientes + Equipos |
| Plantilla hoja de vida equipos | Hoja de vida + PDF |
| Formato RMA plantilla 2 | RMA / garantías / devoluciones |
| Inventario soporte técnico (Bogotá / Medellín) | Inventario + kardex |
| Cronograma mantenimientos preventivos | Cronograma + alertas + cotización |

Todo queda auditado, ligado a un usuario logueado y conectado entre sí: un serial abre la hoja de vida, el cliente, las RMAs, las salidas de inventario y el preventivo del año.

---

## 2. Roles (matriz realista)

Pediste que solo Superadmin tenga CRUD total. Eso se respeta. Ajusté los demás para que el sistema sea usable en campo (si un ingeniero no puede registrar una salida, el inventario mentirá).

| Capacidad | Superadmin | Líder área | Ingeniero | Auxiliar | Comercial |
|---|---|---|---|---|---|
| Usuarios / roles / catálogos maestros | CRUD | Ver | — | — | — |
| Clientes | CRUD | CRUD | Ver + crear contacto | Ver | Ver |
| Equipos / hoja de vida | CRUD | CRUD | Crear/editar asignados + ver todos | Ver | Ver |
| RMA | CRUD | CRUD | Crear/editar los que abre + ver | Ver | — |
| Inventario catálogo / precios / costos | CRUD | CRUD | Ver | Ver | — |
| Movimientos inventario | CRUD | CRUD + anular | Crear salida/entrada de su sede | Ver (recomendado: crear salida con visto bueno) | — |
| Conteo físico | CRUD | CRUD | Registrar conteo | Ver | — |
| Cronograma / alertas | CRUD | CRUD + disparar envíos | Ver + marcar ejecutado | Ver | Ver + marcar interés comercial |
| Indicadores dashboard | Todo | Todo | Los de su sede / sus OT | Resumen | Embudo preventivos |
| Envío email / WhatsApp | Sí | Sí | Solo sus RMA / sus OT | No | Cotizaciones |
| Import / export Excel | Sí | Sí | Export | Export | Export |

Recomendación fuerte: el Auxiliar debería poder **registrar salidas** (no solo ver). Si no, el líder o el ingeniero terminan digitando todo. Déjalo como permiso configurable `INV_MOVIMIENTO_CREAR`.

Cada edición de RMA, HV e inventario guarda `actualizado_por` + fila en `rma_historial` / `auditoria`.

---

## 3. Arquitectura

```
[React SPA] --HTTPS/JWT--> [API Spring Boot]
                               |-- PostgreSQL
                               |-- Storage local / S3 (fotos etiqueta, PDFs, Excel)
                               |-- SMTP (Gmail Workspace / Microsoft 365)
                               |-- WhatsApp Cloud API (Meta) o Twilio
                               |-- Job Scheduler (Spring @Scheduled + ShedLock)
```

Backend modular (un solo jar, paquetes claros):

```
co.dilaser.nexo
  config          Security, CORS, OpenAPI, Mail, WhatsApp
  auth            Login, refresh, forgot/reset password
  usuarios
  catalogos       Marca, línea, tecnología, modelo, bodega, ciudad
  clientes
  equipos         Equipo, accesorios, hoja vida, actas
  rma
  inventario      Repuesto, stock, movimiento, conteo, import excel
  mantenimiento   Planes, alertas, OT, cotizaciones
  notificaciones  Email + WhatsApp + plantillas
  pdf             Render Thymeleaf → OpenPDF / Flying Saucer
  auditoria
  dashboard
```

Frontend:

```
src/
  app/            router, guards por rol
  theme/          tokens de color Dilaser
  auth/
  layout/         Sidebar + topbar + breadcrumbs
  dashboard/
  clientes/
  equipos/        listado, filtros, form HV, visor PDF
  rma/
  inventario/
  cronograma/
  admin/          usuarios, catálogos
  shared/         DataTable, FileDrop, SerialSearch, PdfPreview
```

---

## 4. Autenticación

Flujo:
1. POST `/api/auth/login` {email, password} → access JWT 15 min + refresh httpOnly 7 días.
2. POST `/api/auth/forgot-password` {email} → si existe, manda link con token 30 min. Respuesta siempre genérica (“si el correo existe…”).
3. POST `/api/auth/reset-password` {token, newPassword}.
4. Primer login fuerza cambio de contraseña (`debe_cambiar_pass`).
5. Bloqueo 15 min tras 5 intentos fallidos (tabla o cache Redis/simple en memoria).

Reglas de password: mín. 10 caracteres, mayúscula, número, símbolo.

Olvidé contraseña: email corporativo con botón “Restablecer” y caducidad visible.

---

## 5. Módulo Clientes + Equipos (plantilla BD 2015-2025)

Columnas de tu Excel → modelo:

- CLIENTE → `clientes.razon_social`
- DIRECCIÓN / CIUDAD
- CANTIDAD → se calcula (no se guarda): count equipos del cliente
- NOMBRE DEL EQUIPO → `modelos_equipo.nombre`
- SERIE No. → `equipos.serial` UNIQUE
- AÑO / MES → `fecha_instalacion` (o acta)

Formulario cliente (frontend, 2 columnas):
- Razón social *, NIT, tipo, nombre comercial
- Dirección, ciudad (select + alta rápida), departamento
- Teléfono, WhatsApp (indicativo +57), email, emails copia
- Contacto principal (nombre, cargo, celular)
- Observaciones
- Activo

Formulario equipo (pasos):
1. Identidad: marca → línea/familia → tecnología → modelo → serial *
2. Propiedad: Cliente / Alquiler Dilaser / Demo / Stock
3. Ubicación: cliente (búsqueda typeahead), ciudad, dirección
4. Datos técnicos HV: país origen, voltage, peso, fecha importación, registro sanitario INVIMA
5. Garantía: fecha instalación, inicio/fin garantía, n° acta
6. Accesorios (tabla dinámica: descripción, cantidad, serial accesorio)
7. Fotos: etiqueta, frente, accesorios

Filtros listado equipos: serial, cliente, ciudad, marca, familia, modelo, estado, año instalación, propiedad.

---

## 6. Hoja de vida — crear, editar, PDF, historial

La HV **no es un documento suelto**: es la ficha viva del equipo. El PDF se genera on-demand con la plantilla que ya usas.

Secciones del PDF (igual a tu .xls):
1. Encabezado “HOJA DE VIDA” + logo Dilaser
2. DATOS DEL PROPIETARIO ( Dilaser o el cliente; si el equipo se vendió, aparece el dueño actual y nota “se vende a…”)
3. DATOS DEL EQUIPO (tecnología, marca, modelo, serie, origen, voltage, importación, peso, registro sanitario)
4. LISTA DE CONTENIDO
5. MANTENIMIENTOS REALIZADOS (fecha, actividades, ingeniero)
6. Pie: “SE ANEXA LISTA DE CHEQUEO” cuando aplique
7. QR con serial que abre la ficha en Nexo (útil en taller)

Endpoints:
- `POST /api/equipos` crea equipo + accesorios + plan de mantenimiento automático
- `PUT /api/equipos/{id}`
- `POST /api/equipos/{id}/mantenimientos`
- `GET /api/equipos/{id}/hoja-vida.pdf`
- `GET /api/equipos?q=&marca=&linea=&ciudad=&estado=`

Al crear el equipo, el backend:
1. Inserta `equipos`
2. Copia accesorios
3. Crea `planes_mantenimiento` con periodicidad ANUAL y `mes_ancla` = mes de instalación o acta
4. Genera alerta del año en curso si falta menos de 60 días, o del año siguiente

---

## 7. Módulo RMA (plantilla 2)

Campos 1:1 con tu formato + extras operativos.

| Campo plantilla | Campo sistema |
|---|---|
| CUSTOMER | cliente o texto libre (snapshot) |
| WARRANTY | sí / no / por vencer (calculado vs garantia_fin) |
| DESCRIPTION | item / modelo |
| INSTALLATION DATE | fecha instalación |
| S/N | serial (typeahead a equipos) |
| REPORTED PROBLEM | texto largo |
| PART N° | referencia de inventario (typeahead) + texto |
| N° OF PULSES | entero |
| PURCHASE ORDER | texto |
| HP OUTPUT POWER | texto |
| ADDITIONAL DETAILS | texto |
| LABEL PICTURE | adjunto imagen |

Extras que te faltan en el Excel y sí o sí debes tener:
- Número interno `NS-RMA-AAAA-#####` (lo genera Nexo)
- Número RMA del fabricante (lo pegas cuando Alma responde)
- Motivo: garantía / fuera de garantía / devolución / reprogramación
- Estado del caso (máquina de estados)
- Destinatarios extra (chips de email)
- Pieza ligada al inventario: al enviar la pieza se crea `SALIDA_RMA`; al volver `ENTRADA_RMA`
- Historial: quién editó qué y cuándo

Máquina de estados:
`BORRADOR → ENVIADO_FABRICANTE → EN_EVALUACION → APROBADO|RECHAZADO → PIEZA_ENVIADA → PIEZA_RECIBIDA → CERRADO`

PDF del RMA: clona el layout de tu xlsx (bloques izquierda/derecha + recuadro de foto). Se descarga y se adjunta al correo.

Correo al fabricante (plantilla):
- De: `iris.p@example.org`
- Para: contactos de la marca + extras
- CC: líder + ingeniero que abre el caso
- Asunto: `RMA Dilaser · {serial} · {part} · {numero_interno}`
- Cuerpo HTML con colores corporativos, tabla de campos, link al PDF
- Adjuntos: PDF + foto etiqueta + fotos daño

Filtros: serial, part number, cliente, estado, marca, rango fechas, creado_por.

---

## 8. Inventario (plantilla 3)

Dos bodegas reales: Soporte Bogotá y Soporte Medellín. El stock **nunca** se edita a mano en la existencia: solo se mueve con un movimiento (kardex). Así el mes a mes cuadra.

Campos catálogo:
referencia, descripción, bodega, marca, línea/familia, U.M., costo promedio, precio sugerido, stock mínimo, ubicación (Caja 13), observaciones.

Kardex / movimientos (el corazón del control):
- Entrada compra / importación / devolución cliente / retorno RMA
- Salida garantía / venta / consumo interno / envío RMA / préstamo a ingeniero
- Transferencia Bogotá ↔ Medellín
- Ajuste (solo líder/superadmin, exige motivo)
- Retorno de préstamo

Cada movimiento pide:
cantidad, bodega, ubicación, persona que entrega, persona que recibe (usuario o nombre libre), cliente/equipo/RMA si aplica, documento, foto opcional, observación.

Import Excel:
- Plantilla descargable con las mismas columnas de Bogotá/Medellín
- Validación fila a fila: referencia duplicada, marca inexistente, cantidad negativa
- Reporte de errores descargable
- Upsert por `referencia` + `bodega`

Export:
- Stock valorizado
- Kardex por mes / bodega / motivo
- Diferencias de conteo
- “Lo que salió este mes por garantía vs venta vs interno” (esto alimenta indicadores)

Conteo físico:
1. Líder abre conteo de una bodega (congela existencias sistema)
2. Se recorre Caja por Caja
3. Al cerrar, las diferencias generan ajustes pendientes de aprobación

Costo promedio: se recalcula en cada entrada  
`nuevo = ((existencia * costo_ant) + (cant_ent * costo_ent)) / (existencia + cant_ent)`

---

## 9. Cronograma y algoritmo de alertas

Fuente de verdad para “cuándo toca”:
1. Acta de entrega (preferida) → mes ancla
2. Si no hay acta, fecha de instalación
3. Si no hay ninguna, fecha de importación
4. Periodicidad por defecto **ANUAL** (configurable por modelo)

### Algoritmo (`AlertScheduler`, corre todos los días 06:00 America/Bogota)

```
para cada plan ACTIVO:
    proximo = fecha(anio_actual, mes_ancla, dia_ancla)
    si proximo < hoy: proximo = proximo + 1 año   // o +periodo

    dias = proximo - hoy

    si no existe alerta(equipo, anio_de_proximo):
        crear alerta PROGRAMADA

    si dias <= 45 y estado == PROGRAMADA:
        estado = PENDIENTE_ENVIO

    si dias <= 30 y estado == PENDIENTE_ENVIO:
        enviar email bonito al cliente + copia comercial + líder
        enviar WhatsApp con texto corto + PDF cotización
        estado = ENVIADA

    si dias <= 7 y estado == ENVIADA y no hay respuesta:
        reenviar recordatorio (máx 2)

    si dias < 0 y no ACEPTADA/CERRADA:
        estado = VENCIDA  (aparece rojo en dashboard)

    si cliente responde ACEPTA:
        crear Orden de Servicio PREVENTIVO
        generar cotización si aún no existe
        estado = ACEPTADA

    si cliente RECHAZA:
        estado = RECHAZADA (se ofrece correctivo / se reintenta en 6 meses)
```

Calendario del módulo: vista mes tipo Gantt / heatmap por ciudad y por ingeniero. “X” de tu Excel = alerta ENVIADA o OT ejecutada.

WhatsApp (plantilla preaprobada Meta, obligatorio en modo productivo):

```
Hola {contacto}, te escribe Soporte Dilaser.
El equipo {modelo} serie {serial} tiene mantenimiento preventivo programado en {mes_anio}.
Adjuntamos cotización {numero}. Si autorizas, respondemos este mensaje con la palabra SI y coordinamos visita.
Gracias, Nexo Support.
```

Email HTML:
- Motivo: `Recordatorio preventivo {modelo} {serial} — Dilaser`
- Cuerpo: saludo, datos equipo, última visita, qué incluye el preventivo, PDF, botón “Acepto el servicio”, contacto del ingeniero de la sede.

Historial de cada envío queda en `notificaciones`.

---

## 10. Dashboard (didáctico, colores logo)

Widgets (líder / superadmin):
1. Equipos activos / en taller / en RMA
2. Preventivos del mes: programados, enviados, aceptados, vencidos
3. Embudo comercial de preventivos (aceptación %)
4. Stock crítico (existencia ≤ mínimo) por bodega
5. Valor inventario Bogotá vs Medellín
6. Salidas del mes apiladas: garantía / venta / interno / RMA
7. RMAs abiertas por estado
8. Carga de ingenieros (OT asignadas)
9. Mapa simple o ranking de ciudades con más equipos
10. Timeline de últimos movimientos y últimos correos

Ingeniero: mis OT de la semana, mis RMA, mis préstamos de repuestos pendientes de devolver.

KPI fórmulas:
- Fill rate preventivos = aceptados / enviados
- Cumplimiento = ejecutados en el mes / programados del mes
- Rotación repuesto = salidas 12 meses / existencia promedio
- Aging RMA = días promedio BORRADOR→CERRADO

---

## 11. Formularios frontend — criterio de diseño

- Un serial es el “ID universal”: barra de búsqueda global arriba (equipo, RMA, repuesto).
- Selects encadenados: Marca → Familia/Línea → Modelo → Seriales de ese modelo.
- Autoguardado de borradores en RMA y HV (localStorage + PATCH).
- Campos de fecha con datepicker dd/MM/yyyy (Colombia).
- WhatsApp y teléfono con máscara +57.
- Tablas con paginación servidor, columnas persistidas, export CSV.
- Confirmación al cambiar estado de RMA o al hacer salida de inventario.
- Toasts de éxito/error en español.

Pantallas mínimas:
1. Login / Forgot / Reset
2. Dashboard
3. Clientes list + form
4. Equipos list + form wizard + ficha HV + visor PDF
5. RMA list + form + visor PDF + historial lateral
6. Inventario stock + form item + kardex + import wizard + conteo
7. Cronograma calendario + ficha alerta + editor plantillas mensaje
8. Cotizaciones
9. Admin usuarios y catálogos
10. Auditoría

---

## 12. APIs principales

```
/api/auth/**
/api/usuarios/**
/api/catalogos/{marcas|lineas|tecnologias|modelos|bodegas|ciudades}
/api/clientes
/api/equipos
/api/equipos/{id}/hoja-vida.pdf
/api/equipos/{id}/mantenimientos
/api/rma
/api/rma/{id}/enviar
/api/rma/{id}/pdf
/api/inventario/repuestos
/api/inventario/import
/api/inventario/export
/api/inventario/movimientos
/api/inventario/conteos
/api/mantenimiento/planes
/api/mantenimiento/alertas
/api/mantenimiento/alertas/{id}/enviar
/api/cotizaciones
/api/dashboard/kpis
/api/notificaciones
```

Seguridad: `@PreAuthorize` por rol + permiso fino. CORS solo al origen del front. Archivos no se sirven sin JWT.

---

## 13. Generación de PDF

Motor: HTML Thymeleaf + Flying Saucer / OpenHTMLToPDF.
Plantillas en `src/main/resources/templates/pdf/`:
- `hoja-vida.html`
- `rma.html`
- `cotizacion-preventivo.html`
- `kardex-mes.html`

Tipografía Arial/Calibri, header con logo, footer con NIT 811.046.078-4 y “Documento generado por Nexo Support el {fecha}”.

---

## 14. Cómo levantarlo en IntelliJ (orden)

1. PostgreSQL 16 local, DB `nexo_support`, user `nexo`.
2. Correr `sql/001_schema.sql` y `sql/002_seed.sql`.
3. Spring Initializr: Java 21, Maven, dependencies:
   Spring Web, Validation, Security, Data JPA, PostgreSQL,
   Lombok, MapStruct, Flyway, Spring Mail, Thymeleaf,
   SpringDoc OpenAPI, Actuator.
4. `application.yml`: datasource, jwt secret (env), mail, whatsapp, storage path, timezone `America/Bogota`.
5. Vite React TS: Ant Design o MUI + Tailwind, axios interceptor JWT, react-router 6.
6. Primero auth + usuarios + catálogos.
7. Luego clientes + equipos + PDF HV.
8. Luego inventario + import.
9. Luego RMA + mail.
10. Luego cronograma + jobs + WhatsApp.

No construyas los 6 módulos a la vez. El orden de arriba es el que desbloquea valor real más rápido.

---

## 15. Ideas extra que te conviene meter (no estaban en el Excel)

1. **Préstamo de herramientas/repuestos al ingeniero** con fecha de devolución. Evita huecos de inventario.
2. **Lista de chequeo por modelo** (la HV ya dice “se anexa lista de chequeo”). Checklist digital firmada al cerrar el preventivo.
3. **Contador de pulsos / disparos** por pieza de mano, no solo por equipo. En Alma esto es oro para RMA.
4. **Contratos de alquiler** (ustedes tienen equipos Dilaser Bogotá en el cronograma).
5. **Portal mínimo del cliente** (link mágico): ve su HV y acepta el preventivo sin WhatsApp.
6. **Integración SIIGO / World Office** si ya facturan preventivos ahí.
7. **Códigos QR** pegados al equipo = abre HV en el celular del ingeniero.
8. **SLA de RMA** (días en cada estado) como KPI del líder.
9. **Adjunto obligatorio de foto de etiqueta** antes de enviar RMA (Alma lo pide).
10. **Bitácora de llamadas** al cliente cuando no contesta el WhatsApp.

---

## 16. Lo que necesito de ti para la siguiente iteración

Para pasar de este blueprint a código que compile y se vea con su marca:

1. Logo Dilaser en PNG/SVG (fondo transparente).
2. Dirección y teléfono reales de la sede Bogotá.
3. Lista de usuarios iniciales (nombre, correo, rol, sede).
4. Correos reales del desk de RMA de Alma y demás fabricantes.
5. Si ya tienen WhatsApp Business y SMTP corporativo (o usamos Gmail de prueba).
6. Política de garantía estándar (meses) por familia.
7. Tarifas base del preventivo por modelo (para armar la cotización automática).
8. Confirmación: ¿el auxiliar puede registrar salidas sí o no?
9. ¿Quieres módulo Comercial desde el día 1 o lo dejamos para fase 2?

Con eso armo el esqueleto Spring + React (entidades, seguridad, primeros formularios y plantillas PDF).
