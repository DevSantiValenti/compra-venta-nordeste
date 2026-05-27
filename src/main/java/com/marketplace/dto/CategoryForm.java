package com.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {
    @NotBlank
    private String name;

    private String slug;
    private String icon;
    private boolean active = true;
    private int displayOrder = 0;
}
