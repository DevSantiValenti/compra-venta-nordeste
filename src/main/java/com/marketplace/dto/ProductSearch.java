package com.marketplace.dto;

import com.marketplace.entity.ProductCondition;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearch {
    private String q;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String city;
    private String province;
    private ProductCondition condition;
    private String brand;
    private String size;
}
