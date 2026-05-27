package com.marketplace.dto;

import com.marketplace.entity.ProductCondition;
import com.marketplace.entity.ProductCurrency;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearch {
    private String q;
    private List<Long> categoryIds = new ArrayList<>();
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductCurrency currency;
    private String city;
    private String province;
    private ProductCondition condition;
    private String brand;
    private String size;
    private String wheelSize;
}
