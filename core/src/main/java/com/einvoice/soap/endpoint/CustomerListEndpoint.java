package com.einvoice.soap.endpoint;

import com.einvoice.soap.catalog.CatalogJsonLoader;
import com.einvoice.soap.gen.CustomerListRequest;
import com.einvoice.soap.gen.CustomerListResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CustomerListEndpoint {

    private static final String NAMESPACE_URI = "http://einvoice.com/soap/gen";

    private final CatalogJsonLoader catalogJsonLoader;

    public CustomerListEndpoint(CatalogJsonLoader catalogJsonLoader) {
        this.catalogJsonLoader = catalogJsonLoader;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CustomerListRequest")
    @ResponsePayload
    public CustomerListResponse listCustomers(@RequestPayload CustomerListRequest request) {
        CustomerListResponse response = new CustomerListResponse();
        response.getCustomer().addAll(catalogJsonLoader.getCustomers());
        return response;
    }
}
