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

        String subject = "Factura Electrónica DIAN - " + customer.getName();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>")
            .append("body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #333333; }")
            .append(".container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }")
            .append(".header { background-color: #1f497d; color: #ffffff; padding: 25px 20px; text-align: center; }")
            .append(".header h1 { margin: 0; font-size: 22px; font-weight: 600; letter-spacing: 0.5px; }")
            .append(".content { padding: 35px 30px; }")
            .append(".greeting { font-size: 18px; margin-bottom: 20px; color: #1f497d; font-weight: bold; }")
            .append(".info-box { background-color: #f8f9fa; border-left: 4px solid #1f497d; padding: 15px 20px; margin-bottom: 30px; border-radius: 0 4px 4px 0; }")
            .append(".info-box p { margin: 5px 0; font-size: 14px; }")
            .append(".table { width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 14px; }")
            .append(".table th, .table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eeeeee; }")
            .append(".table th { background-color: #f8f9fa; color: #555555; font-weight: 600; text-transform: uppercase; font-size: 12px; }")
            .append(".total-row td { font-weight: bold; color: #1f497d; font-size: 16px; border-top: 2px solid #1f497d; border-bottom: none; }")
            .append(".footer { background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #777777; border-top: 1px solid #dddddd; }")
            .append("</style></head><body>")
            .append("<div class='container'>")
            .append("<div class='header'><h1>Documento Electrónico DIAN</h1></div>")
            .append("<div class='content'>")
            .append("<div class='greeting'>Hola, ").append(customer.getName()).append("</div>")
            .append("<p style='line-height: 1.6; color: #555;'>Le informamos que se ha emitido un nuevo documento electrónico a su nombre. Puede encontrar la representación gráfica (PDF) adjunta a este correo.</p>")
            .append("<div class='info-box'>")
            .append("<p><strong>NIT / ID:</strong> ").append(customer.getID()).append("</p>")
            .append("<p><strong>Atendido por:</strong> ").append(employee.getName()).append("</p>")
            .append("</div>")
            .append("<h3 style='color: #333; font-size: 16px; margin-bottom: 15px;'>Resumen de la Transacción</h3>")
            .append("<table class='table'>")
            .append("<thead><tr><th>Producto</th><th>Cant.</th><th>Precio Unit.</th><th>Subtotal</th></tr></thead><tbody>");

        double totalInvoice = 0.0;
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int qty = i < quantities.size() ? quantities.get(i) : 1;
            
            double price = 0.0;
            try {
                price = Double.parseDouble(product.getUnitPrice());
            } catch (Exception e) {}
            
            double subtotal = price * qty;
            totalInvoice += subtotal;
            
            html.append("<tr>")
                .append("<td>").append(product.getName()).append("</td>")
                .append("<td>").append(qty).append("</td>")
                .append("<td>$").append(String.format("%,.2f", price)).append("</td>")
                .append("<td>$").append(String.format("%,.2f", subtotal)).append("</td>")
                .append("</tr>");
        }

        html.append("<tr class='total-row'>")
            .append("<td colspan='3' style='text-align: right;'>TOTAL:</td>")
            .append("<td>$").append(String.format("%,.2f", totalInvoice)).append("</td>")
            .append("</tr>")
            .append("</tbody></table>")
            .append("<p style='line-height: 1.6; color: #555; text-align: center; margin-top: 30px;'>Agradecemos su confianza y esperamos servirle nuevamente.</p>")
            .append("</div>")
            .append("<div class='footer'>Este es un mensaje automático generado por el Sistema de Facturación Electrónica DIAN.<br>Por favor no responda a este correo.</div>")
            .append("</div></body></html>");

        return new InvoiceEmailContent(subject, html.toString());
    }

    public record InvoiceEmailContent(String subject, String body) {
    }
}
