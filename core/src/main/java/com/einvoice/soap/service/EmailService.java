package com.einvoice.soap.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email with a PDF file attached to it.
     * This method is responsible for constructing a complex email message
     * that can hold both standard text and file attachments.
     *
     * @param to            The recipient's email address (e.g., the customer's
     *                      email)
     * @param subject       The main subject line of the email
     * @param text          The body content or message of the email
     * @param pdfAttachment The generated PDF invoice represented as a byte array
     * @param filename      The exact name the attached file will have (e.g.,
     *                      "invoice_123.pdf")
     * @throws MessagingException If there is a problem building the email or
     *                            connecting to the SMTP server
     */
    public void sendInvoiceEmail(String to, String subject, String text, byte[] pdfAttachment, String filename)
            throws MessagingException {
        // MimeMessage is a special email object that supports advanced features like
        // attachments
        MimeMessage message = mailSender.createMimeMessage();

        // The 'true' flag indicates that this is a multipart message, which is required
        // when sending attachments
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Set the basic email details: recipient, subject, and body text
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        // Check if the PDF byte array is valid and not empty
        // Then convert the raw bytes into a Spring resource so it can be attached to
        // the email
        if (pdfAttachment != null && pdfAttachment.length > 0) {
            ByteArrayResource pdfResource = new ByteArrayResource(pdfAttachment);
            helper.addAttachment(filename, pdfResource);
        }

        // Finally, tell the JavaMailSender to dispatch the fully constructed email over
        // the network
        mailSender.send(message);
    }
}
