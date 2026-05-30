package com.einvoice.soap.endpoint;

import com.einvoice.soap.catalog.CatalogJsonLoader;
import com.einvoice.soap.gen.ProductListRequest;
import com.einvoice.soap.gen.ProductListResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ProductListEndpoint {

    private static final String NAMESPACE_URI = "http://einvoice.com/soap/gen";

    private final CatalogJsonLoader catalogJsonLoader;

    public ProductListEndpoint(CatalogJsonLoader catalogJsonLoader) {
        this.catalogJsonLoader = catalogJsonLoader;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ProductListRequest")
    @ResponsePayload
    public ProductListResponse listProducts(@RequestPayload ProductListRequest request) {
        ProductListResponse response = new ProductListResponse();
        response.getProduct().addAll(catalogJsonLoader.getProducts());
        return response;
    }
}
