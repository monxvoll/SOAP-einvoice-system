# 💾 WINMAC OS 1.0 - E-INVOICE SYSTEM
----------------------------------------------------------------------
Welcome to the **WinMac OS** Electronic Invoicing (DIAN) Platform. 
A system designed to bridge the robust architecture of SOAP web services 
with a nostalgic, high-productivity retro desktop interface.

##  SYSTEM DIRECTORY
----------------------------------------------------------------------
- `[DIR] core/`      : Java Spring Boot Backend (SOAP 1.1)
- `[DIR] non-core/`  : Vanilla JS Frontend (Retro Desktop UI)
- `[DIR] docs/`      : API Documentation & Postman Collections

##  TECHNICAL SPECIFICATIONS
----------------------------------------------------------------------
### BACKEND (Server)
- **Framework:** Spring Boot / Spring Web Services
- **Protocol:** SOAP 1.1 (XML)
- **Features:** 
  - WSDL Generation (`EinvoiceSystem.xsd`)
  - PDF Invoice Generation (iText)
  - Data Catalogs (Customers, Products, Employees)

### FRONTEND (Client)
- **Engine:** Vanilla JavaScript, HTML5, CSS3
- **Aesthetic:** Early 90s GUI (Windows 3.1 / Mac OS System 7 blend)
- **Features:** Window management, Retro Toast notifications, Pixel-perfect styling

##  BOOT SEQUENCE
----------------------------------------------------------------------
To initialize the system locally, follow these steps:

**1. Boot the Backend (Core)**
```bash
cd core
./mvnw spring-boot:run
```
*(The SOAP service will listen on `http://localhost:8080/ws`)*

**2. Boot the Frontend (Non-Core)**
Serve the static files to avoid CORS issues:
```bash
cd non-core
python3 -m http.server 3000
```
Then navigate to `http://localhost:3000` in your web browser.

##  MODULES
----------------------------------------------------------------------
* **EMPLEADOS:** Select the active vendor/employee for the session.
* **FACTURACIÓN DIAN:** Create detailed electronic invoices conforming to DIAN standard formats.
* **CATÁLOGO:** Browse available products and registered customers from the database.
----------------------------------------------------------------------
**(C) 2026 WinMac Soft Inc. All Rights Reserved.**
*"It is now safe to turn off your computer."*
