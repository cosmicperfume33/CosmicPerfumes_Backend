package com.example.ForgeX.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdown {
    private Long subtotal;        
    private Long mrpTotal;        
    private Long productSavings;  
    private Long couponDiscount;
    private String couponCode;     
    private String couponMessage;  
    private Long shipping;
    private Long total;
}