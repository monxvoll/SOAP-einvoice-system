package com.einvoice.soap.service;

import com.einvoice.soap.gen.Customer;
import com.einvoice.soap.gen.Einvoice;
import com.einvoice.soap.gen.Employee;
import com.einvoice.soap.gen.Product;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    // Define corporate colors for an aesthetic design
    private static final BaseColor PRIMARY_COLOR = new BaseColor(31, 73, 125); // Deep Blue
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final BaseColor BORDER_COLOR = new BaseColor(200, 200, 200);

    /**
     * Generates a PDF file based on the invoice data (Einvoice).
     * 
     * @param invoice The invoice object generated from the XSD
     * @return The byte array representing the PDF file
     */
    public byte[] generateInvoicePdf(Einvoice invoice) {
        // A4 format with margins
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header (Logo + Title)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 2f});

            // Logo
            try {
                ClassPathResource logoResource = new ClassPathResource("images/dian_logo.png");
                if (logoResource.exists()) {
                    Image logo = Image.getInstance(logoResource.getURL());
                    logo.scaleToFit(120, 120);
                    PdfPCell logoCell = new PdfPCell(logo);
                    logoCell.setBorder(Rectangle.NO_BORDER);
                    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    headerTable.addCell(logoCell);
                } else {
                    headerTable.addCell(getCell(" ", PdfPCell.NO_BORDER));
                }
            } catch (Exception e) {
                headerTable.addCell(getCell(" ", PdfPCell.NO_BORDER));
            }

            // Title and basic invoice info
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PRIMARY_COLOR);
            Paragraph title = new Paragraph("FACTURA ELECTRÓNICA", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(title);
            
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph("República de Colombia — DIAN", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(subtitle);
            
            headerTable.addCell(titleCell);
            document.add(headerTable);

            // Divider Line
            document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(1f, 100f, PRIMARY_COLOR, Element.ALIGN_CENTER, -10f)));
            document.add(new Paragraph(" ")); // Spacer
            document.add(new Paragraph(" "));

            // 2. Vendor and Customer Information (Side-by-Side Grid)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});
            infoTable.setSpacingAfter(20f);

            Employee vendor = invoice.getEmployee();
            Customer customer = invoice.getCustomer();

            PdfPCell vendorBox = createInfoBox("DATOS DEL VENDEDOR", vendor);
            vendorBox.setPaddingRight(10f); // Spacing between boxes
            vendorBox.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(vendorBox);

            PdfPCell customerBox = createInfoBox("DATOS DEL CLIENTE", customer);
            customerBox.setPaddingLeft(10f);
            customerBox.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(customerBox);

            document.add(infoTable);

            // 3. Products Table
            List<Product> products = invoice.getProduct();
            List<Integer> quantities = invoice.getQuantities();

            if (products != null && !products.isEmpty()) {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.5f, 4f, 2f, 1.5f, 2f});
                table.setSpacingBefore(10f);

                // Table Headers
                addTableHeader(table, "Código");
                addTableHeader(table, "Producto");
                addTableHeader(table, "Precio Unit.");
                addTableHeader(table, "Cant.");
                addTableHeader(table, "Subtotal");

                double totalInvoice = 0.0;
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    int qty = (quantities != null && quantities.size() > i) ? quantities.get(i) : 1;
                    
                    double price = 0.0;
                    try {
                        price = Double.parseDouble(p.getUnitPrice());
                    } catch (NumberFormatException ignored) {}

                    double subtotal = price * qty;
                    totalInvoice += subtotal;

                    // Zebra-striping colors
                    boolean isAlternate = (i % 2 != 0);
                    BaseColor rowColor = isAlternate ? LIGHT_GRAY : BaseColor.WHITE;

                    addTableCell(table, p.getID(), cellFont, rowColor, Element.ALIGN_CENTER);
                    addTableCell(table, p.getName(), cellFont, rowColor, Element.ALIGN_LEFT);
                    addTableCell(table, "$" + String.format("%,.2f", price), cellFont, rowColor, Element.ALIGN_RIGHT);
                    addTableCell(table, String.valueOf(qty), cellFont, rowColor, Element.ALIGN_CENTER);
                    addTableCell(table, "$" + String.format("%,.2f", subtotal), cellFont, rowColor, Element.ALIGN_RIGHT);
                }
                document.add(table);

                // 4. Final Total Box
                PdfPTable totalTable = new PdfPTable(2);
                totalTable.setWidthPercentage(100);
                totalTable.setWidths(new float[]{6f, 4f}); // Push total to the right side
                
                PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                totalTable.addCell(emptyCell);

                PdfPCell totalBox = new PdfPCell();
                totalBox.setBorder(Rectangle.NO_BORDER);
                totalBox.setPaddingTop(15f);
                
                PdfPTable innerTotalTable = new PdfPTable(2);
                innerTotalTable.setWidthPercentage(100);
                innerTotalTable.setWidths(new float[]{1f, 1.8f});
                
                Font totalLblFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR);
                PdfPCell lblCell = new PdfPCell(new Phrase("TOTAL:", totalLblFont));
                lblCell.setBorder(Rectangle.NO_BORDER);
                lblCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                lblCell.setPadding(8f);
                innerTotalTable.addCell(lblCell);

                Font totalValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
                PdfPCell valCell = new PdfPCell(new Phrase("$" + String.format("%,.2f", totalInvoice), totalValFont));
                valCell.setBackgroundColor(LIGHT_GRAY);
                valCell.setBorderColor(BORDER_COLOR);
                valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                valCell.setPadding(8f);
                innerTotalTable.addCell(valCell);

                totalBox.addElement(innerTotalTable);
                totalTable.addCell(totalBox);
                
                document.add(totalTable);
            }

            // 5. Footer
            document.add(new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100f, BORDER_COLOR, Element.ALIGN_CENTER, 20f)));

            // 6. Watermark (Text)
            try {
                PdfContentByte canvas = writer.getDirectContentUnder();
                PdfGState state = new PdfGState();
                state.setFillOpacity(0.15f); // Transparent text
                canvas.setGState(state);
                
                Font wmFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 90, BaseColor.GRAY);
                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, 
                    new Phrase("VALIDADO DIAN", wmFont), 
                    PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2, 45f);
            } catch (Exception e) {}

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    /**
     * Creates a formatted card-like box for Vendor or Customer info.
     */
    private PdfPCell createInfoBox(String title, Object infoObj) {
        PdfPCell cell = new PdfPCell();
        
        // Header of the box
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        PdfPCell headerCell = new PdfPCell(new Phrase(title, titleFont));
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setPadding(6f);
        headerCell.setBorderColor(BORDER_COLOR);
        
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.addCell(headerCell);

        // Content of the box
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Font boldContentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        
        PdfPCell contentCell = new PdfPCell();
        contentCell.setPadding(8f);
        contentCell.setBorderColor(BORDER_COLOR);
        contentCell.setMinimumHeight(90f); // Forces equal height for both boxes

        if (infoObj instanceof Employee) {
            Employee vendor = (Employee) infoObj;
            contentCell.addElement(new Phrase(vendor.getName(), boldContentFont));
            contentCell.addElement(new Phrase("NIT/ID: " + vendor.getID(), contentFont));
            contentCell.addElement(new Phrase("Actividad: " + vendor.getProductiveActivity(), contentFont));
            contentCell.addElement(new Phrase("Teléfono: " + vendor.getPhoneNumber(), contentFont));
            contentCell.addElement(new Phrase("Email: " + vendor.getEmail(), contentFont));
        } else if (infoObj instanceof Customer) {
            Customer customer = (Customer) infoObj;
            contentCell.addElement(new Phrase(customer.getName(), boldContentFont));
            contentCell.addElement(new Phrase("NIT/ID: " + customer.getID(), contentFont));
            contentCell.addElement(new Phrase("Teléfono: " + customer.getPhoneNumber(), contentFont));
            contentCell.addElement(new Phrase("Email: " + customer.getEmail(), contentFont));
        }

        table.addCell(contentCell);
        cell.addElement(table);
        return cell;
    }

    /**
     * Helper method to add a formatted header cell to the table.
     */
    private void addTableHeader(PdfPTable table, String headerTitle) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        PdfPCell header = new PdfPCell(new Phrase(headerTitle, headerFont));
        header.setBackgroundColor(PRIMARY_COLOR);
        header.setBorderColor(BORDER_COLOR);
        header.setPadding(8f);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(header);
    }

    /**
     * Helper method to add a formatted data cell to the table.
     */
    private void addTableCell(PdfPTable table, String text, Font font, BaseColor bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private PdfPCell getCell(String text, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBorder(border);
        return cell;
    }
}
