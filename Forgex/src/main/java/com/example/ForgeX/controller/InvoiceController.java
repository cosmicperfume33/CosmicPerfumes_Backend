package com.example.ForgeX.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ForgeX.service.InvoiceService;
import com.example.ForgeX.util.AuthUtil;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AuthUtil authUtil;

    // Customer invoice
    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<byte[]> myInvoice(
            @PathVariable Long orderId) {

        byte[] pdf = invoiceService.generate(
                orderId,
                authUtil.loggedInEmail());

        return pdfResponse(
                pdf,
                "ForgeX-invoice-" + orderId + ".pdf");
    }

    // Admin can generate invoice for any order
    @GetMapping("/admin/orders/{orderId}/invoice")
    public ResponseEntity<byte[]> adminInvoice(
            @PathVariable Long orderId) {

        byte[] pdf = invoiceService.generate(
                orderId,
                null);

        return pdfResponse(
                pdf,
                "ForgeX-invoice-" + orderId + ".pdf");
    }

    // Common PDF response
    private ResponseEntity<byte[]> pdfResponse(
            byte[] pdf,
            String fileName) {

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(pdf);
    }
}