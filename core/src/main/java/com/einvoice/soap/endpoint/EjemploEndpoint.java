package com.einvoice.soap.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

// Importar clases generadas tras compilar (mvn clean compile) el XSD:
// import com.einvoice.soap.gen.EjemploRequest;
// import com.einvoice.soap.gen.EjemploResponse;

@Endpoint
public class EjemploEndpoint {

    private static final String NAMESPACE_URI = "http://einvoice.com/soap/gen";

    // Descomentar tras compilar para usar clases JAXB generadas
    /*
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "EjemploRequest")
    @ResponsePayload
    public EjemploResponse procesarEjemplo(@RequestPayload EjemploRequest request) {
        EjemploResponse response = new EjemploResponse();
        response.setResultado("Procesado: " + request.getDato());
        return response;
    }
    */
}
