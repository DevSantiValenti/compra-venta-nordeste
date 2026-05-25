package com.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportForm {
    @NotBlank
    private String reason;

    private String detail;
}
