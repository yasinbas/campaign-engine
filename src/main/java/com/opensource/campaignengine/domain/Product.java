package com.opensource.campaignengine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class) // Otomatik tarih takibi için
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ürün Adı

    @Column(columnDefinition = "TEXT")
    private String description; // Uzun ürün açıklaması

    @Column(nullable = false, unique = true)
    private String sku; // Stok Takip Kodu (Stock Keeping Unit), benzersiz olmalı

    @Column(unique = true)
    private String barcode; // EAN/UPC barkod numarası

    @Column(nullable = false)
    private BigDecimal price; // Fiyat (Hatasız hesaplama için BigDecimal)

    @Column(nullable = false)
    private Integer stockQuantity; // Stok Miktarı

    @Column(nullable = false)
    private BigDecimal vatRate; // KDV Oranı (Örn: 18.00, 8.00)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSaleType saleType; // Satış Tipi (Adet, Kg, Lt)

    private boolean taxFree = false; // Tax Free'ye uygun mu?

    private boolean isActive = true; // Ürün satışta mı?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Ürünün kategorisi

    // --- Denetim Alanları ---
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // Oluşturulma Tarihi

    @LastModifiedDate
    private LocalDateTime updatedAt; // Son Güncellenme Tarihi
}