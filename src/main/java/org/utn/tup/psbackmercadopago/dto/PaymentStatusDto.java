package org.utn.tup.psbackmercadopago.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusDto {

    private String paymentId;
    private String preferenceId;
    private String status;
    private String statusDetail;
    private BigDecimal transactionAmount;
    private String paymentMethodId;
    private String paymentMethodType;
    private String externalReference;
    private String merchantOrderId;
    private LocalDateTime dateCreated;
    private LocalDateTime dateApproved;
    private Map<String, Object> additionalInfo;

    private String payerEmail;
    private String payerIdentification;

    private String errorMessage;
    private String errorCode;
}
