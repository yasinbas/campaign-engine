package com.opensource.campaignengine.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data // Lombok'un getter, setter, toString vb. metotları oluşturması için
public class CartItemDTO {
    private String productId; // Ürünün barkodu veya benzersiz kodu
    private int quantity;     // Sepetteki adedi
    private BigDecimal unitPrice; // Ürünün birim fiyatı
}

