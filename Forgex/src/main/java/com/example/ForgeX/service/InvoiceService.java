package com.example.ForgeX.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Address;
import com.example.ForgeX.model.Order;
import com.example.ForgeX.model.OrderItem;
import com.example.ForgeX.model.OrderStatus;
import com.example.ForgeX.model.StateCode;
import com.example.ForgeX.repository.OrderRepository;
import com.example.ForgeX.util.AmountInWords;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class InvoiceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Font H1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7);

    @Autowired
    private OrderRepository orderRepository;

    @Value("${shop.invoice.seller-name}")
    private String sellerName;
    @Value("${shop.invoice.address-line1}")
    private String addressLine1;
    @Value("${shop.invoice.address-line2}")
    private String addressLine2;
    @Value("${shop.invoice.state}")
    private String sellerState;
    @Value("${shop.invoice.state-code}")
    private String sellerStateCode;
    @Value("${shop.invoice.pan}")
    private String pan;
    @Value("${shop.invoice.gstin}")
    private String gstin;
    @Value("${shop.invoice.hsn}")
    private String hsn;
    @Value("${shop.invoice.gst-rate}")
    private int gstRate;
    @Value("${shop.invoice.prices-include-tax}")
    private boolean pricesIncludeTax;

    /** One line of the invoice table, all money in paise. */
    private record Line(String description, long unitNet, int qty,
            long netAmount, long taxAmount, long total) {
    }

    @Transactional(readOnly = true)
    public byte[] generate(Order order) {
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT || order.getStatus() == OrderStatus.CANCELLED) {
            throw new APIException("An invoice is only available for confirmed orders");
        }

        Address ship = order.getAddress();
        boolean intraState = StateCode.sameState(sellerState, ship == null ? null : ship.getState());

        List<Line> lines = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            lines.add(line(item.getProductName(), item.getUnitPrice(), item.getQuantity()));
        }
        if (order.getShipping() != null && order.getShipping() > 0) {
            lines.add(line("Shipping charges", order.getShipping(), 1));
        }

        long netTotal = lines.stream().mapToLong(Line::netAmount).sum();
        long taxTotal = lines.stream().mapToLong(Line::taxAmount).sum();
        long grossTotal = lines.stream().mapToLong(Line::total).sum();
        long discount = order.getDiscount() == null ? 0 : order.getDiscount();

        return render(order, ship, lines, intraState, netTotal, taxTotal, grossTotal, discount);
    }

    @Transactional(readOnly = true)
    public byte[] generate(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        if (email != null && !order.getEmail().equals(email)) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }
        return generate(order);
    }

    /**
     * Invoice number: FX/2026-27/000012, reset each financial year (April to
     * March).
     */
    public String invoiceNumber(Order order) {
        LocalDate date = order.getCreatedAt().toLocalDate();
        int startYear = date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
        String fy = startYear + "-" + String.format("%02d", (startYear + 1) % 100);
        return String.format("FX/%s/%06d", fy, order.getOrderId());
    }

    private Line line(String description, long unitGross, int qty) {
        long lineGross = unitGross * qty;
        long net;
        if (pricesIncludeTax) {
            net = BigDecimal.valueOf(lineGross)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(100L + gstRate), 0, RoundingMode.HALF_UP)
                    .longValue();
        } else {
            net = lineGross;
            lineGross = net + net * gstRate / 100;
        }
        long unitNet = net / qty;
        return new Line(description, unitNet, qty, net, lineGross - net, lineGross);
    }

    private static String money(long paise) {
        return "Rs. " + BigDecimal.valueOf(paise, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---------- PDF ----------

    private byte[] render(Order order, Address ship, List<Line> lines, boolean intraState,
            long netTotal, long taxTotal, long grossTotal, long discount) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            header(doc);
            parties(doc, order, ship);
            orderMeta(doc, order);
            itemTable(doc, lines, intraState);
            totals(doc, netTotal, taxTotal, grossTotal, discount, order.getTotal());
            footer(doc, order);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new APIException("The invoice could not be generated");
        }
    }

    private void header(Document doc) {
        PdfPTable t = borderless(2, new float[] { 1, 1 });
        t.addCell(left(new Paragraph("FORGEX", H1)));

        Paragraph title = new Paragraph();
        title.add(new Phrase("Tax Invoice/Bill of Supply/Cash Memo\n", BOLD_10));
        title.add(new Phrase("(Original for Recipient)", NORMAL));
        title.setAlignment(Element.ALIGN_RIGHT);
        t.addCell(right(title));

        doc.add(t);
        doc.add(spacer(14));
    }

    private void parties(Document doc, Order order, Address ship) {
        PdfPTable t = borderless(2, new float[] { 1, 1 });

        Paragraph seller = new Paragraph();
        seller.add(new Phrase("Sold By :\n", BOLD));
        seller.add(new Phrase(sellerName + "\n" + addressLine1 + "\n" + addressLine2 + "\nIN\n\n", NORMAL));
        seller.add(new Phrase("PAN No: ", BOLD));
        seller.add(new Phrase(pan + "\n", NORMAL));
        seller.add(new Phrase("GST Registration No: ", BOLD));
        seller.add(new Phrase(gstin, NORMAL));
        t.addCell(left(seller));

        Paragraph buyer = new Paragraph();
        buyer.setAlignment(Element.ALIGN_RIGHT);
        buyer.add(new Phrase("Billing / Shipping Address :\n", BOLD));
        buyer.add(new Phrase(addressBlock(ship, order.getAddress().getPhoneNumber()), NORMAL));
        if (ship != null) {
            buyer.add(new Phrase("\nState/UT Code: ", BOLD));
            buyer.add(new Phrase(StateCode.of(ship.getState()) + "\n", NORMAL));
            buyer.add(new Phrase("Place of supply: ", BOLD));
            buyer.add(new Phrase(ship.getState() + "\n", NORMAL));
            buyer.add(new Phrase("Place of delivery: ", BOLD));
            buyer.add(new Phrase(ship.getCity(), NORMAL));
        }
        t.addCell(right(buyer));

        doc.add(t);
        doc.add(spacer(14));
    }

    private void orderMeta(Document doc, Order order) {
        PdfPTable t = borderless(2, new float[] { 1, 1 });

        Paragraph left = new Paragraph();
        left.add(new Phrase("Order Number: ", BOLD));
        left.add(new Phrase(order.getOrderNumber() + "\n", NORMAL));
        left.add(new Phrase("Order Date: ", BOLD));
        left.add(new Phrase(order.getCreatedAt().format(DATE), NORMAL));
        t.addCell(left(left));

        Paragraph right = new Paragraph();
        right.setAlignment(Element.ALIGN_RIGHT);
        right.add(new Phrase("Invoice Number : ", BOLD));
        right.add(new Phrase(invoiceNumber(order) + "\n", NORMAL));
        right.add(new Phrase("Invoice Date : ", BOLD));
        right.add(new Phrase((order.getPaidAt() != null ? order.getPaidAt() : order.getCreatedAt()).format(DATE),
                NORMAL));
        t.addCell(right(right));

        doc.add(t);
        doc.add(spacer(12));
    }

    private void itemTable(Document doc, List<Line> lines, boolean intraState) throws Exception {
        PdfPTable t = new PdfPTable(new float[] { 0.5f, 4f, 1.2f, 0.6f, 1.2f, 0.9f, 0.8f, 1.1f, 1.2f });
        t.setWidthPercentage(100);
        t.setHeaderRows(1);

        for (String h : new String[] { "Sl No", "Description", "Unit Price", "Qty",
                "Net Amount", "Tax Rate", "Tax Type", "Tax Amount", "Total Amount" }) {
            t.addCell(th(h));
        }

        int i = 1;
        for (Line l : lines) {
            int rows = intraState ? 2 : 1;

            t.addCell(td(String.valueOf(i++), Element.ALIGN_CENTER, rows));
            t.addCell(td(l.description() + "  (HSN " + hsn + ")", Element.ALIGN_LEFT, rows));
            t.addCell(td(money(l.unitNet()), Element.ALIGN_RIGHT, rows));
            t.addCell(td(String.valueOf(l.qty()), Element.ALIGN_CENTER, rows));
            t.addCell(td(money(l.netAmount()), Element.ALIGN_RIGHT, rows));

            if (intraState) {
                long half = l.taxAmount() / 2;
                String halfRate = (gstRate / 2.0) + "%";
                t.addCell(td(halfRate, Element.ALIGN_CENTER, 1));
                t.addCell(td("CGST", Element.ALIGN_CENTER, 1));
                t.addCell(td(money(half), Element.ALIGN_RIGHT, 1));
                t.addCell(td(money(l.total()), Element.ALIGN_RIGHT, 2));

                t.addCell(td(halfRate, Element.ALIGN_CENTER, 1));
                t.addCell(td("SGST", Element.ALIGN_CENTER, 1));
                t.addCell(td(money(l.taxAmount() - half), Element.ALIGN_RIGHT, 1));
            } else {
                t.addCell(td(gstRate + "%", Element.ALIGN_CENTER, 1));
                t.addCell(td("IGST", Element.ALIGN_CENTER, 1));
                t.addCell(td(money(l.taxAmount()), Element.ALIGN_RIGHT, 1));
                t.addCell(td(money(l.total()), Element.ALIGN_RIGHT, 1));
            }
        }

        doc.add(t);
    }

    private void totals(Document doc, long netTotal, long taxTotal, long grossTotal,
            long discount, Long orderTotal) {
        PdfPTable t = new PdfPTable(new float[] { 6.9f, 1.1f, 1.2f });
        t.setWidthPercentage(100);

        t.addCell(totalCell("TOTAL:", Element.ALIGN_LEFT, BOLD));
        t.addCell(totalCell(money(taxTotal), Element.ALIGN_RIGHT, BOLD));
        t.addCell(totalCell(money(grossTotal), Element.ALIGN_RIGHT, BOLD));

        if (discount > 0) {
            t.addCell(totalCell("Less: discount", Element.ALIGN_LEFT, NORMAL));
            t.addCell(totalCell("", Element.ALIGN_RIGHT, NORMAL));
            t.addCell(totalCell("- " + money(discount), Element.ALIGN_RIGHT, NORMAL));

            t.addCell(totalCell("AMOUNT PAYABLE:", Element.ALIGN_LEFT, BOLD));
            t.addCell(totalCell("", Element.ALIGN_RIGHT, BOLD));
            t.addCell(totalCell(money(orderTotal), Element.ALIGN_RIGHT, BOLD));
        }

        doc.add(t);

        PdfPTable words = new PdfPTable(1);
        words.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.addElement(new Paragraph("Amount in Words:", BOLD));
        cell.addElement(new Paragraph(AmountInWords.fromPaise(orderTotal), BOLD_10));
        words.addCell(cell);
        doc.add(words);
    }

    private void footer(Document doc, Order order) {
        PdfPTable sign = new PdfPTable(1);
        sign.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_RIGHT);
        p.add(new Phrase("For " + sellerName + ":\n\n\n", BOLD));
        p.add(new Phrase("Authorized Signatory", BOLD));
        cell.addElement(p);
        sign.addCell(cell);
        doc.add(sign);

        doc.add(spacer(8));
        doc.add(new Paragraph("Whether tax is payable under reverse charge - No", NORMAL));
        doc.add(spacer(6));
        doc.add(new Paragraph("Payment: " + order.getPaymentMethod()
                + (order.getPaidAt() != null ? ", paid on " + order.getPaidAt().format(DATE) : ""), SMALL));
        doc.add(new Paragraph("This is a computer generated invoice and does not require a physical signature.",
                SMALL));
    }

    // ---------- small PDF helpers ----------

    private static String addressBlock(Address a, String email) {
        if (a == null)
            return email;
        return String.join("\n",
                a.getBuildingName(),
                a.getStreet(),
                a.getCity() + ", " + a.getState() + " " + a.getPincode(),
                a.getCountry(),
                email);
    }

    private static PdfPTable borderless(int cols, float[] widths) {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        return t;
    }

    private static PdfPCell left(Paragraph p) {
        PdfPCell c = new PdfPCell(p);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell right(Paragraph p) {
        PdfPCell c = left(p);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private static PdfPCell th(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, BOLD));
        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private static PdfPCell td(String text, int align, int rowspan) {
        PdfPCell c = new PdfPCell(new Phrase(text, NORMAL));
        c.setPadding(4);
        c.setHorizontalAlignment(align);
        c.setRowspan(rowspan);
        return c;
    }

    private static PdfPCell totalCell(String text, int align, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4);
        c.setHorizontalAlignment(align);
        return c;
    }

    private static Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }
}