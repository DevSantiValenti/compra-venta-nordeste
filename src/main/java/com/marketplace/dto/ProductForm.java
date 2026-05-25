package com.marketplace.dto;

import com.marketplace.entity.ProductCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    private ProductCondition condition = ProductCondition.USED;

    private String brand;
    private String size;

    @NotBlank
    private String city;

    @NotBlank
    private String province;

    @NotNull
    private Long categoryId;
}
