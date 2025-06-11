package org.utn.tup.psbackmercadopago.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationResponseDto {
    private String preferenceId;
    private String checkoutUrl;
    private String status;
    private BigDecimal amount;
    private String description;
    private String payerEmail;
    private String payerName;
    private String externalReference;
    private LocalDateTime createdAt;

    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
}
