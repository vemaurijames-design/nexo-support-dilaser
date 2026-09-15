# Estructura de proyectos IntelliJ

## Backend `nexo-support-api`
```
nexo-support-api/
├── pom.xml
├── src/main/java/co/dilaser/nexo/
│   ├── NexoSupportApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthFilter.java
│   │   ├── CorsConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── TimezoneConfig.java          // America/Bogota
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── dto/ LoginRequest, LoginResponse, ForgotRequest, ResetRequest
│   ├── usuario/
│   ├── catalogo/
│   ├── cliente/
│   ├── equipo/
│   │   ├── Equipo.java
│   │   ├── EquipoController.java
│   │   ├── EquipoService.java
│   │   └── HojaVidaPdfService.java
│   ├── rma/
│   ├── inventario/
│   │   ├── RepuestoExcelService.java    // Apache POI import/export
│   │   └── KardexService.java
│   ├── mantenimiento/
│   │   └── AlertScheduler.java          // @Scheduled cron 0 0 6 * * *
│   ├── notificacion/
│   │   ├── EmailService.java
│   │   └── WhatsAppService.java
│   ├── pdf/ PdfRenderer.java
│   ├── storage/ StorageService.java
│   ├── auditoria/
│   └── dashboard/ DashboardController.java
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/                    // Flyway = los SQL de /sql
│   └── templates/
│       ├── mail/reset-password.html
│       ├── mail/rma-fabricante.html
│       ├── mail/recordatorio-preventivo.html
│       └── pdf/hoja-vida.html
└── src/test/java
```

## Frontend `nexo-support-web`
```
nexo-support-web/
├── package.json
├── vite.config.ts
├── tailwind.config.js                   // colors.primary = #037F8C
├── src/
│   ├── main.tsx
│   ├── theme/dilaser.ts
│   ├── api/http.ts                      // axios + refresh
│   ├── auth/
│   ├── routes/AppRouter.tsx
│   ├── layout/AppShell.tsx
│   ├── modules/dashboard/pages/DashboardPage.tsx
│   ├── modules/clientes/
│   ├── modules/equipos/components/HojaVidaForm.tsx
│   ├── modules/rma/components/RmaForm.tsx
│   ├── modules/inventario/components/ImportExcelWizard.tsx
│   ├── modules/cronograma/components/CalendarioPreventivos.tsx
│   └── shared/SerialSearch.tsx
```

## application.yml (esqueleto)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nexo_support
    username: nexo
    password: ${DB_PASSWORD}
  jpa:
    hibernate.ddl-auto: validate
    properties.hibernate.jdbc.time_zone: America/Bogota
  servlet.multipart.max-file-size: 15MB
  mail:
    host: smtp.office365.com
    port: 587
    username: ${MAIL_USER}
    password: ${MAIL_PASSWORD}
nexo:
  jwt.secret: ${JWT_SECRET}
  jwt.access-minutes: 15
  jwt.refresh-days: 7
  storage.path: /var/nexo/storage
  frontend-url: http://localhost:5173
  whatsapp:
    enabled: false
    phone-id: ${WA_PHONE_ID}
    token: ${WA_TOKEN}
    template-preventivo: recordatorio_preventivo
```
