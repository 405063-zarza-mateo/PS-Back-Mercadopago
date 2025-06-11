package org.utn.tup.psbackmercadopago.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.utn.tup.psbackmercadopago.Exceptions.PaymentProcessingException;
import org.utn.tup.psbackmercadopago.dto.PaymentStatusDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentClient paymentClient = new PaymentClient();

    public PaymentStatusDto getPaymentStatus(String paymentId) {
        try {
            log.info("Getting payment status for payment ID: {}", paymentId);

            Payment payment = paymentClient.get(Long.valueOf(paymentId));

            return buildPaymentStatusDto(payment);

        } catch (Exception e) {
            log.error("Error getting payment status for ID: {}", paymentId, e);
            throw new PaymentProcessingException("Error al obtener el estado del pago", e);
        }
    }

    public PaymentStatusDto processPaymentCallback(Map<String, String> params) {
        try {
            String paymentId = params.get("payment_id");
            String status = params.get("status");
            String preferenceId = params.get("preference_id");
            String externalReference = params.get("external_reference");

            log.info("Processing payment callback - Payment ID: {}, Status: {}, Preference ID: {}",
                    paymentId, status, preferenceId);

            PaymentStatusDto.PaymentStatusDtoBuilder builder = PaymentStatusDto.builder()
                    .paymentId(paymentId)
                    .preferenceId(preferenceId)
                    .status(status)
                    .externalReference(externalReference)
                    .dateCreated(LocalDateTime.now());

            // Si tenemos el ID del pago, obtenemos información detallada
            if (paymentId != null && !paymentId.isEmpty()) {
                try {
                    Payment payment = paymentClient.get(Long.valueOf(paymentId));
                    return buildPaymentStatusDto(payment);
                } catch (Exception e) {
                    log.warn("Could not get detailed payment info for ID: {}, using basic info", paymentId, e);
                }
            }

            // Agregar parámetros adicionales
            Map<String, Object> additionalInfo = new HashMap<>(params);
            builder.additionalInfo(additionalInfo);

            return builder.build();

        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            throw new PaymentProcessingException("Error al procesar callback de pago", e);
        }
    }

    private PaymentStatusDto buildPaymentStatusDto(Payment payment) {
        PaymentStatusDto.PaymentStatusDtoBuilder builder = PaymentStatusDto.builder()
                .paymentId(payment.getId().toString())
                .status(payment.getStatus())
                .statusDetail(payment.getStatusDetail())
                .transactionAmount(payment.getTransactionAmount())
                .paymentMethodId(payment.getPaymentMethodId())
                .paymentMethodType(payment.getPaymentTypeId())
                .externalReference(payment.getExternalReference())
                .merchantOrderId(payment.getOrder() != null ? payment.getOrder().getId().toString() : null);

        // Fechas
        if (payment.getDateCreated() != null) {
            builder.dateCreated(payment.getDateCreated().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if (payment.getDateApproved() != null) {
            builder.dateApproved(payment.getDateApproved().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        // Información del pagador
        if (payment.getPayer() != null) {
            builder.payerEmail(payment.getPayer().getEmail());
            if (payment.getPayer().getIdentification() != null) {
                builder.payerIdentification(payment.getPayer().getIdentification().getNumber());
            }
        }

        // Información adicional
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("description", payment.getDescription());
        additionalInfo.put("installments", payment.getInstallments());
        additionalInfo.put("issuer_id", payment.getIssuerId());
        additionalInfo.put("currency_id", payment.getCurrencyId());

        if (payment.getFeeDetails() != null && !payment.getFeeDetails().isEmpty()) {
            additionalInfo.put("fees", payment.getFeeDetails());
        }

        builder.additionalInfo(additionalInfo);

        return builder.build();
    }
}
