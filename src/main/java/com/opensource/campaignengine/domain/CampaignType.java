package com.opensource.campaignengine.domain;

public enum CampaignType {
    // Ürün Bazlı
    PRODUCT_PERCENTAGE_DISCOUNT,
    PRODUCT_FIXED_DISCOUNT,
    BUY_X_PAY_Y,

    // Sepet Bazlı
    BASKET_TOTAL_PERCENTAGE_DISCOUNT,
    BASKET_TOTAL_FIXED_DISCOUNT,

    // Koşullu
    TRIGGER_PRODUCT_GET_DISCOUNT


}