package com.einvoice.soap.endpoint;

import com.einvoice.soap.gen.Einvoice;
import com.einvoice.soap.gen.RegisterPurchaseResponse;
import com.einvoice.soap.service.RegisterPurchaseService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class RegisterPurchaseEndpoint {

    private static final String NAMESPACE_URI = "http://einvoice.com/soap/gen";

    private final RegisterPurchaseService registerPurchaseService;

    public RegisterPurchaseEndpoint(RegisterPurchaseService registerPurchaseService) {
        this.registerPurchaseService = registerPurchaseService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Einvoice")
    @ResponsePayload
    public RegisterPurchaseResponse registerPurchase(@RequestPayload Einvoice request) {
        return registerPurchaseService.registerPurchase(request);
    }
}
