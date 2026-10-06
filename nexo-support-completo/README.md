# Nexo Support — Dilaser (completo)

Spring Boot 3 · Java 17 · Maven · PostgreSQL · React (Vite)

## Login
- **Usuario:** soportetecnico2@dilaser.com.co  
- **Clave:** admin123456  
- **Rol:** SUPERADMIN  

## Configuración (IntelliJ + local)

### PostgreSQL
```sql
DROP DATABASE IF EXISTS nexo_support;
CREATE USER nexo WITH PASSWORD 'nexo123';  -- si no existe
CREATE DATABASE nexo_support OWNER nexo;
```

### Backend (IntelliJ)
1. Open folder `backend` as Maven project  
2. JDK 17  
3. Run `NexoSupportApplication` o:
```bash
cd backend
mvn clean spring-boot:run
```
Config: `src/main/resources/application.properties` (no YAML).

### Frontend
```bash
cd frontend
npm install
npm run dev
```
http://localhost:5173

## Módulos ↔ formularios reales

| Módulo | Base documental | Qué hace |
|--------|-----------------|----------|
| Configuración | Datos Dilaser | Logo, NIT, sedes → login y PDFs |
| Clientes | BD clientes/equipos | CRUD ficha profesional |
| Hojas de vida | Formato biomédico completo | HV con datos técnicos, clasificación, calibración |
| RMA | Formato RMA Excel | Casos fabricante |
| Remisiones | Plantilla remisión PDF | Salida ítems, motivo VAPDGRT |
| Inventario | Plantilla inventario | Stock Bogotá/Medellín |
| Cronograma | Preventivos Excel | Alertas mes |
| Usuarios | — | Roles |

## application.properties (correo)
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=iris.p@example.org
spring.mail.password=TU_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
nexo.mail.from=iris.p@example.org
```

## Migraciones Flyway
V1 schema · V2 seed · V3 empresa+remisiones · V4 hoja vida biomédica  

Si editas V1/V2 ya aplicados → DROP DATABASE y recrear.
