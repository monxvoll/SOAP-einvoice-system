# Servicios SOAP expuestos — pruebas con Postman

Catálogos de **clientes**, **productos** y **empleados** vía SOAP 1.1. Datos cargados desde JSON en `core/src/main/resources/data/`.

## Arranque local

```bash
cd core
mvn spring-boot:run
```

Puerto por defecto: **8080** (sin `application.properties`).

## URLs base

| Recurso | URL |
|---------|-----|
| WSDL | `http://localhost:8080/ws/einvoice.wsdl` |
| XSD | `http://localhost:8080/ws/einvoice.xsd` |
| Endpoint SOAP | `http://localhost:8080/ws` |

**Namespace:** `http://einvoice.com/soap/gen`

**Importar en Postman:** *Import* → pegar URL del WSDL o subir el archivo generado.

---

## Configuración común en Postman

Método: **POST**  
URL: `http://localhost:8080/ws`

**Headers:**

| Header | Valor |
|--------|--------|
| `Content-Type` | `text/xml; charset=utf-8` |
| `SOAPAction` | `""` |

En Postman: body → **raw** → tipo **XML**. Pegar el envelope de cada operación.

---

## 1. Listar clientes — `CustomerListRequest`

**Operación:** devuelve todos los clientes del catálogo.

**Request (copiar en Body):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:gen="http://einvoice.com/soap/gen">
   <soapenv:Header/>
   <soapenv:Body>
      <gen:CustomerListRequest/>
   </soapenv:Body>
</soapenv:Envelope>
```

**Respuesta esperada (estructura):**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:CustomerListResponse xmlns:ns2="http://einvoice.com/soap/gen">
         <ns2:Customer>
            <ns2:ID>900123456-7</ns2:ID>
            <ns2:Name>Comercializadora Andina SAS</ns2:Name>
            <ns2:email>facturacion@andina.demo.co</ns2:email>
            <ns2:phoneNumber>+57 601 5550101</ns2:phoneNumber>
         </ns2:Customer>
         <!-- más Customer ... -->
      </ns2:CustomerListResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

**Campos por cliente (`Customer`):**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `ID` | string | Identificador (NIT/documento) |
| `Name` | string | Razón social o nombre |
| `email` | string | Correo |
| `phoneNumber` | string | Teléfono |

---

## 2. Listar productos — `ProductListRequest`

**Operación:** devuelve todos los productos del catálogo.

**Request (copiar en Body):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:gen="http://einvoice.com/soap/gen">
   <soapenv:Header/>
   <soapenv:Body>
      <gen:ProductListRequest/>
   </soapenv:Body>
</soapenv:Envelope>
```

**Respuesta esperada (estructura):**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:ProductListResponse xmlns:ns2="http://einvoice.com/soap/gen">
         <ns2:Product>
            <ns2:ID>FE-001</ns2:ID>
            <ns2:name>Licencia anual facturación electrónica DIAN</ns2:name>
            <ns2:unitPrice>2400000</ns2:unitPrice>
         </ns2:Product>
         <!-- más Product ... -->
      </ns2:ProductListResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

**Campos por producto (`Product`):**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `ID` | string | Código del producto |
| `name` | string | Nombre (minúscula en XSD) |
| `unitPrice` | string | Precio unitario |

---

## 3. Listar empleados — `EmployeeListRequest`

**Operación:** devuelve todos los empleados del catálogo.

**Request (copiar en Body):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:gen="http://einvoice.com/soap/gen">
   <soapenv:Header/>
   <soapenv:Body>
      <gen:EmployeeListRequest/>
   </soapenv:Body>
</soapenv:Envelope>
```

**Respuesta esperada (estructura):**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:EmployeeListResponse xmlns:ns2="http://einvoice.com/soap/gen">
         <ns2:Employee>
            <ns2:ID>1012345678</ns2:ID>
            <ns2:Name>María Fernanda López</ns2:Name>
            <ns2:phoneNumber>+57 300 1110001</ns2:phoneNumber>
            <ns2:email>mlopez@dian.demo.co</ns2:email>
            <ns2:productiveActivity>Facturación electrónica - ventas</ns2:productiveActivity>
         </ns2:Employee>
         <!-- más Employee ... -->
      </ns2:EmployeeListResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

**Campos por empleado (`Employee`):**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `ID` | string | Documento de identidad |
| `Name` | string | Nombre completo |
| `phoneNumber` | string | Teléfono |
| `email` | string | Correo |
| `productiveActivity` | string | Actividad / rol |

---

## 4. Registrar compra / factura — `Einvoice`

**Operación:** recibe datos de la factura (empleado, cliente, productos y cantidades), construye y envía el correo, y devuelve el estado del envío.

**Notas:**

- El destinatario del correo es siempre `<gen:email>` dentro de `<gen:Customer>` del body SOAP (no el empleado ni el catálogo JSON).
- Requiere `application.properties` con SMTP Gmail (ver `application.properties.example`).

**Request (copiar en Body):**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:gen="http://einvoice.com/soap/gen">
   <soapenv:Header/>
   <soapenv:Body>
      <gen:Einvoice>
         <gen:Employee>
            <gen:ID>1012345678</gen:ID>
            <gen:Name>María Fernanda López</gen:Name>
            <gen:phoneNumber>+57 300 1110001</gen:phoneNumber>
            <gen:email>mlopez@dian.demo.co</gen:email>
            <gen:productiveActivity>Facturación electrónica - ventas</gen:productiveActivity>
         </gen:Employee>
         <gen:Customer>
            <gen:ID>900123456-7</gen:ID>
            <gen:Name>Comercializadora Andina SAS</gen:Name>
            <gen:email>facturacion@andina.demo.co</gen:email>
            <gen:phoneNumber>+57 601 5550101</gen:phoneNumber>
         </gen:Customer>
         <gen:Product>
            <gen:ID>FE-001</gen:ID>
            <gen:name>Licencia anual facturación electrónica DIAN</gen:name>
            <gen:unitPrice>2400000</gen:unitPrice>
         </gen:Product>
         <gen:quantities>1</gen:quantities>
      </gen:Einvoice>
   </soapenv:Body>
</soapenv:Envelope>
```

**Respuesta esperada (estructura):**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Body>
      <ns2:RegisterPurchaseResponse xmlns:ns2="http://einvoice.com/soap/gen">
         <ns2:emailCheck>true</ns2:emailCheck>
      </ns2:RegisterPurchaseResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

| Campo respuesta | Tipo | Descripción |
|-----------------|------|-------------|
| `emailCheck` | boolean | `true` si el SMTP envió el correo; `false` si falló el envío |

**Campos del request (`Einvoice`):**

| Bloque / campo | Descripción |
|----------------|-------------|
| `Employee` | Vendedor / empleado que atiende la venta |
| `Customer` | Cliente facturado |
| `Product` (0..n) | Líneas de producto |
| `quantities` (0..n) | Cantidad por línea; si hay productos y cantidades, deben coincidir en número |

---

## cURL (alternativa a Postman)

**Clientes:**

```bash
curl -s -X POST 'http://localhost:8080/ws' \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gen="http://einvoice.com/soap/gen"><soapenv:Header/><soapenv:Body><gen:CustomerListRequest/></soapenv:Body></soapenv:Envelope>'
```

**Productos:**

```bash
curl -s -X POST 'http://localhost:8080/ws' \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gen="http://einvoice.com/soap/gen"><soapenv:Header/><soapenv:Body><gen:ProductListRequest/></soapenv:Body></soapenv:Envelope>'
```

**Empleados:**

```bash
curl -s -X POST 'http://localhost:8080/ws' \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gen="http://einvoice.com/soap/gen"><soapenv:Header/><soapenv:Body><gen:EmployeeListRequest/></soapenv:Body></soapenv:Envelope>'
```

**Registrar factura (`Einvoice`):**

```bash
curl -s -X POST 'http://localhost:8080/ws' \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gen="http://einvoice.com/soap/gen"><soapenv:Header/><soapenv:Body><gen:Einvoice><gen:Employee><gen:ID>1012345678</gen:ID><gen:Name>María Fernanda López</gen:Name><gen:phoneNumber>+57 300 1110001</gen:phoneNumber><gen:email>mlopez@dian.demo.co</gen:email><gen:productiveActivity>Facturación electrónica - ventas</gen:productiveActivity></gen:Employee><gen:Customer><gen:ID>900123456-7</gen:ID><gen:Name>Comercializadora Andina SAS</gen:Name><gen:email>facturacion@andina.demo.co</gen:email><gen:phoneNumber>+57 601 5550101</gen:phoneNumber></gen:Customer><gen:Product><gen:ID>FE-001</gen:ID><gen:name>Licencia anual facturación electrónica DIAN</gen:name><gen:unitPrice>2400000</gen:unitPrice></gen:Product><gen:quantities>1</gen:quantities></gen:Einvoice></soapenv:Body></soapenv:Envelope>'
```

---

## Errores frecuentes

| Síntoma | Causa probable | Acción |
|---------|----------------|--------|
| `404` en `/ws` sin body SOAP | GET al endpoint en lugar de POST | Usar POST con envelope XML |
| `500` / fault por namespace | Elemento sin prefijo `gen:` o namespace incorrecto | Usar `xmlns:gen="http://einvoice.com/soap/gen"` y prefijo en el body |
| Respuesta vacía / fault | Falta header `SOAPAction` | Añadir `SOAPAction: ""` |
| `emailCheck` false en `Einvoice` | SMTP sin configurar o credenciales Gmail incorrectas | Copiar `application.properties.example` → `application.properties` y usar contraseña de aplicación |
| Conexión rechazada | Servicio no levantado | `mvn spring-boot:run` en `core/` |

---

## Referencia en código

| Operación | Clase endpoint |
|-----------|----------------|
| `CustomerListRequest` | `com.einvoice.soap.endpoint.CustomerListEndpoint` |
| `ProductListRequest` | `com.einvoice.soap.endpoint.ProductListEndpoint` |
| `EmployeeListRequest` | `com.einvoice.soap.endpoint.EmployeeListEndpoint` |
| `Einvoice` | `com.einvoice.soap.endpoint.RegisterPurchaseEndpoint` |

Configuración WSDL/servlet: `com.einvoice.soap.config.WebServiceConfig` — servlet en `/ws/*`, bean WSDL `einvoice`.
