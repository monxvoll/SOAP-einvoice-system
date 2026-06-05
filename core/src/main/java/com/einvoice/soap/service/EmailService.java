package com.einvoice.soap.service;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Einvoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfService pdfService;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void sendInvoiceEmail(String subject, String text, Einvoice einvoice) throws MessagingException {
        String to = customerEmailFromRequest(einvoice);
        byte[] pdfBytes = pdfService.generateInvoicePdf(einvoice);
        String attachmentName = pdfFileName(einvoice);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        if (fromAddress != null && !fromAddress.isBlank()) {
            helper.setFrom(fromAddress);
        }
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.addAttachment(attachmentName, new ByteArrayResource(pdfBytes), "application/pdf");

        mailSender.send(message);
    }

    private static String customerEmailFromRequest(Einvoice einvoice) {
        Customer customer = einvoice.getCustomer();
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("El cliente debe incluir un email válido en el request SOAP");
        }
        return customer.getEmail().trim();
    }

    private static String pdfFileName(Einvoice einvoice) {
        Customer customer = einvoice.getCustomer();
        if (customer != null && customer.getID() != null && !customer.getID().isBlank()) {
            return "factura-" + customer.getID() + ".pdf";
        }
        return "factura.pdf";
    }
}
