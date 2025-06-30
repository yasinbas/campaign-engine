package com.opensource.campaignengine.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartDTO {
    private List<CartItemDTO> items;
}