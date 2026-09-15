# Nexo Support — Dilaser S.A.

Sistema de soporte técnico (hojas de vida, RMA, inventario, preventivos).

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | **Java 21 + Spring Boot 3.3** |
| Frontend | **React 18 + Vite** |
| Base de datos | **PostgreSQL 16** |
| PDF | Thymeleaf + OpenHTMLToPDF |
| Correo | Spring Mail (SMTP) |
| Auth | JWT + Spring Security |

## Estructura del proyecto (carpetas)

```
nexo-support/
├── backend/                 ← Spring Boot (Java)
│   ├── pom.xml
│   └── src/main/
│       ├── java/co/dilaser/nexo/
│       │   ├── auth/          Login, JWT, olvidé contraseña
│       │   ├── usuario/       Usuarios y roles
│       │   ├── catalogo/      Marcas, modelos, bodegas
│       │   ├── cliente/       Clientes
│       │   ├── equipo/        Hojas de vida
│       │   ├── rma/           RMA / garantías
│       │   ├── inventario/    Repuestos + kardex
│       │   ├── mantenimiento/ Alertas preventivos
│       │   ├── pdf/           Generación PDF
│       │   ├── notificacion/  EmailService
│       │   ├── dashboard/     KPIs
│       │   └── config/        Security + bootstrap admin
│       └── resources/
│           ├── application.yml
│           ├── db/migration/     Flyway (tablas + seed)
│           └── templates/pdf/    Plantillas HTML→PDF
│
├── frontend/                ← React
│   ├── package.json
│   ├── vite.config.js       Proxy /api → localhost:8080
│   └── src/
│       ├── modules/         Login, Dashboard, Clientes, Equipos,
│       │                    Rma, Inventario, Cronograma, Usuarios
│       ├── api/http.js      Axios + JWT
│       └── styles.css       Colores Dilaser
│
├── docker-compose.yml       PostgreSQL
├── docs/                    Especificación completa
├── sql/                     Esquema de referencia
└── README.md
```

## Credenciales SUPERADMIN

- **Correo:** `soportetecnico2@dilaser.com.co`
- **Contraseña:** `admin123456`

Se crean automáticamente al arrancar el backend la primera vez.

---

## Cómo correrlo en local

### Requisitos

1. JDK **21**
2. IntelliJ IDEA (Maven incluido)
3. Node.js **18+** y npm
4. Docker Desktop **o** PostgreSQL 16

### 1) Base de datos

Desde la raíz del proyecto:

```bash
docker compose up -d
```

- Host: `localhost:5432`
- Database: `nexo_support`
- User: `nexo`
- Password: `nexo123`

### 2) Backend (Spring Boot)

**Opción IntelliJ (recomendada):**

1. File → Open → selecciona la carpeta **`backend`**
2. Espera a que Maven descargue dependencias
3. Abre la clase `co.dilaser.nexo.NexoSupportApplication`
4. Clic derecho → Run

**Opción terminal:**

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### 3) Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

- UI: http://localhost:5173
- Login con el correo/contraseña de arriba

Vite reenvía `/api` al backend en el puerto 8080.

---

## Flujo de datos

```
React (formularios)
    │  HTTP + JWT
    ▼
Spring Boot (valida y guarda)
    │
    ├──► PostgreSQL   (única fuente de verdad)
    ├──► PDF          (hoja de vida, RMA)
    └──► Correo       (RMA al fabricante, reset password)
```

El frontend **no escribe** en la base de datos. Solo llama al API.

---

## Módulos

| Módulo | Backend | Frontend | PDF | Correo |
|--------|---------|----------|-----|--------|
| Auth | `/api/auth` | Login | — | Olvidé contraseña |
| Clientes | `/api/clientes` | Clientes | — | — |
| Hojas de vida | `/api/equipos` | Equipos | Sí | — |
| RMA | `/api/rma` | Rma | Sí | Sí (enviar) |
| Inventario | `/api/inventario` | Inventario | — | — |
| Cronograma | `/api/mantenimiento` | Cronograma | — | Recordatorio (SMTP) |
| Dashboard | `/api/dashboard` | Dashboard | — | — |
| Usuarios | `/api/usuarios` | Usuarios | — | — |

---

## Correo real (SMTP)

En `backend/src/main/resources/application.yml` o variables de entorno:

```
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USER=tu_correo@dilaser.com.co
MAIL_PASSWORD=tu_app_password
MAIL_FROM=iris.p@example.org
```

Sin SMTP el sistema funciona (CRUD + PDF). Solo no salen correos reales.

---

## Colores Dilaser

- `#03738C` · `#037F8C` · `#9AD2D9` · `#1F736A` · `#F2F2F2`
