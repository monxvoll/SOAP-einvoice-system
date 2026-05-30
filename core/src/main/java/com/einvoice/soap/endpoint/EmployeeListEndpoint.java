package com.einvoice.soap.endpoint;

import com.einvoice.soap.catalog.CatalogJsonLoader;
import com.einvoice.soap.gen.EmployeeListRequest;
import com.einvoice.soap.gen.EmployeeListResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class EmployeeListEndpoint {

    private static final String NAMESPACE_URI = "http://einvoice.com/soap/gen";

    private final CatalogJsonLoader catalogJsonLoader;

    public EmployeeListEndpoint(CatalogJsonLoader catalogJsonLoader) {
        this.catalogJsonLoader = catalogJsonLoader;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "EmployeeListRequest")
    @ResponsePayload
    public EmployeeListResponse listEmployees(@RequestPayload EmployeeListRequest request) {
        EmployeeListResponse response = new EmployeeListResponse();
        response.getEmployee().addAll(catalogJsonLoader.getEmployees());
        return response;
    }
}
