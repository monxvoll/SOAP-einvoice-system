# System Components Architecture

This diagram shows the structural components of the DIAN Electronic Invoicing system, complementing the sequence diagram by illustrating the different modules and technologies involved in the project.

## Component Diagram

```mermaid
graph TD
    %% Client Side
    subgraph Client [Client Side]
        UI[Web Browser UI<br/>(HTML/JS Web Form)]
    end

    %% Server Side
    subgraph Server [DIAN Server - Spring Boot]
        SOAP_EP[SOAP Endpoint<br/>(@Endpoint)]
        Service[Invoice Business Service]
        
        subgraph Utilities [Utilities]
            PDF[PDF Generator<br/>(iTextPDF)]
            Mail[Email Sender<br/>(Spring Mail)]
        end
    end

    %% External Systems
    subgraph External [External Systems]
        SMTP[SMTP Server<br/>(Gmail/Outlook)]
    end

    %% Relationships
    UI -- "Submits form data" --> SOAP_EP
    SOAP_EP -- "Validates & Parses XML" --> Service
    Service -- "1. Requests PDF creation" --> PDF
    Service -- "2. Sends PDF via Email" --> Mail
    Mail -- "SMTP Protocol" --> SMTP
```

## Description of Components

*   **Web Browser UI**: The frontend where the seller fills out the required invoice fields (Seller ID, Product, Customer, etc.).
*   **SOAP Endpoint**: The entry point of the Spring Boot application that exposes the WSDL. It receives the SOAP XML envelopes, validates them, and translates them into Java objects.
*   **Invoice Business Service**: The core logic of the application. It orchestrates the process once the data is received.
*   **PDF Generator (iTextPDF)**: A specialized module that takes the invoice data and the DIAN logo to construct a physical PDF document.
*   **Email Sender**: A module configured to connect to an external SMTP server to dispatch the generated PDF to the customer's email address.
