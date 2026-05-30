package com.einvoice.soap.service;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Einvoice;
import com.einvoice.soap.gen.RegisterPurchaseResponse;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterPurchaseService {

    private static final Logger log = LoggerFactory.getLogger(RegisterPurchaseService.class);

    private final InvoiceEmailBuilder emailBuilder;
    private final EmailService emailService;

    public RegisterPurchaseService(InvoiceEmailBuilder emailBuilder, EmailService emailService) {
        this.emailBuilder = emailBuilder;
        this.emailService = emailService;
    }

    public RegisterPurchaseResponse registerPurchase(Einvoice einvoice) {
        validateLineItems(einvoice);

        String recipient = customerEmailFromRequest(einvoice);
        InvoiceEmailBuilder.InvoiceEmailContent emailContent = emailBuilder.build(einvoice);

        RegisterPurchaseResponse response = new RegisterPurchaseResponse();

        try {
            log.info("Enviando factura al email del cliente en el request: {}", recipient);
            emailService.sendInvoiceEmail(emailContent.subject(), emailContent.body(), einvoice);
            response.setEmailCheck(true);
        } catch (MessagingException e) {
            log.error("No se pudo enviar el correo de factura a {}", recipient, e);
            response.setEmailCheck(false);
        }

        return response;
    }

    private static String customerEmailFromRequest(Einvoice einvoice) {
        Customer customer = einvoice.getCustomer();
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("El cliente debe incluir un email válido en el request SOAP");
        }
        return customer.getEmail().trim();
    }

    private void validateLineItems(Einvoice einvoice) {
        int productCount = einvoice.getProduct().size();
        int quantityCount = einvoice.getQuantities().size();
        if (productCount > 0 && quantityCount > 0 && productCount != quantityCount) {
            throw new IllegalArgumentException(
                    "La cantidad de productos (" + productCount
                            + ") debe coincidir con la cantidad de valores quantities ("
                            + quantityCount + ")");
        }
    }
}
