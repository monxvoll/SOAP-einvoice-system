package com.einvoice.soap.service;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Einvoice;
import com.einvoice.soap.gen.Employee;
import com.einvoice.soap.gen.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceEmailBuilder {

    public InvoiceEmailContent build(Einvoice einvoice) {
        Customer customer = einvoice.getCustomer();
        Employee employee = einvoice.getEmployee();
        List<Product> products = einvoice.getProduct();
        List<Integer> quantities = einvoice.getQuantities();

        String subject = "Factura electrónica - " + customer.getName();

        StringBuilder text = new StringBuilder();
        text.append("Estimado/a ").append(customer.getName()).append(",\n\n");
        text.append("Adjuntamos su factura electrónica.\n\n");
        text.append("Atendido por: ").append(employee.getName()).append("\n");
        text.append("Cliente ID: ").append(customer.getID()).append("\n\n");
        text.append("Resumen:\n");

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int qty = i < quantities.size() ? quantities.get(i) : 1;
            text.append(" - ").append(product.getName())
                    .append(" | Cant: ").append(qty)
                    .append(" | Precio unit: ").append(product.getUnitPrice())
                    .append("\n");
        }

        text.append("\nGracias por su compra.");

        return new InvoiceEmailContent(subject, text.toString());
    }

    public record InvoiceEmailContent(String subject, String body) {
    }
}
