package com.einvoice.soap.service;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Einvoice;
import com.einvoice.soap.gen.Employee;
import com.einvoice.soap.gen.Product;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    /**
     * Generates a PDF file based on the invoice data (Einvoice).
     * 
     * @param invoice The invoice object generated from the XSD
     * @return The byte array representing the PDF file
     */
    public byte[] generateInvoicePdf(Einvoice invoice) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Add the DIAN logo
            try {
                ClassPathResource logoResource = new ClassPathResource("images/dian_logo.png");
                if (logoResource.exists()) {
                    Image logo = Image.getInstance(logoResource.getURL());
                    logo.scaleToFit(150, 150);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception e) {
                System.out.println("Could not load the DIAN logo: " + e.getMessage());
            }

            // 2. Invoice Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("FACTURA ELECTRÓNICA DIAN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 3. Vendor Information (Employee)
            Employee vendor = invoice.getEmployee();
            if (vendor != null) {
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
                document.add(new Paragraph("Vendor Information", sectionFont));
                document.add(new Paragraph("ID: " + vendor.getID()));
                document.add(new Paragraph("Name: " + vendor.getName()));
                document.add(new Paragraph("Activity: " + vendor.getProductiveActivity()));
                document.add(new Paragraph("Phone: " + vendor.getPhoneNumber()));
                document.add(new Paragraph("Email: " + vendor.getEmail()));
                document.add(new Paragraph(" ")); // Blank space
            }

            // 4. Customer Information (Customer)
            Customer customer = invoice.getCustomer();
            if (customer != null) {
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
                document.add(new Paragraph("Customer Information", sectionFont));
                document.add(new Paragraph("ID: " + customer.getID()));
                document.add(new Paragraph("Name: " + customer.getName()));
                document.add(new Paragraph("Phone: " + customer.getPhoneNumber()));
                document.add(new Paragraph("Email: " + customer.getEmail()));
                document.add(new Paragraph(" ")); // Blank space
            }

            // 5. Products Table
            List<Product> products = invoice.getProduct();
            List<Integer> quantities = invoice.getQuantities();

            if (products != null && !products.isEmpty()) {
                PdfPTable table = new PdfPTable(5); // 5 columns
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Table Headers
                addTableHeader(table, "Code");
                addTableHeader(table, "Product");
                addTableHeader(table, "Unit Price");
                addTableHeader(table, "Quantity");
                addTableHeader(table, "Subtotal");

                double totalInvoice = 0.0;

                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    // Use quantity from the list if available, otherwise default to 1
                    int qty = (quantities != null && quantities.size() > i) ? quantities.get(i) : 1;
                    
                    double price = 0.0;
                    try {
                        price = Double.parseDouble(p.getUnitPrice());
                    } catch (NumberFormatException ignored) {}

                    double subtotal = price * qty;
                    totalInvoice += subtotal;

                    table.addCell(p.getID());
                    table.addCell(p.getName());
                    table.addCell(String.valueOf(price));
                    table.addCell(String.valueOf(qty));
                    table.addCell(String.valueOf(subtotal));
                }

                document.add(table);

                // Final Total
                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
                Paragraph total = new Paragraph("Total to Pay: $" + totalInvoice, totalFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);
            }

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    /**
     * Helper method to add a formatted header cell to the table.
     */
    private void addTableHeader(PdfPTable table, String headerTitle) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        PdfPCell header = new PdfPCell(new Phrase(headerTitle, headerFont));
        header.setBackgroundColor(BaseColor.DARK_GRAY);
        header.setBorderWidth(1);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }
}
