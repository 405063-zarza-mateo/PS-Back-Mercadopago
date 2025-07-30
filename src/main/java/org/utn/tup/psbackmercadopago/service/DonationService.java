package org.utn.tup.psbackmercadopago.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.PreferenceBackUrls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.utn.tup.psbackmercadopago.Exceptions.PaymentProcessingException;
import org.utn.tup.psbackmercadopago.dto.DonationRequestDto;
import org.utn.tup.psbackmercadopago.dto.DonationResponseDto;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class DonationService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    private PreferenceClient preferenceClient;

    @PostConstruct
    public void init() {
        try {
            // NO configurar aquí el AccessToken porque ya se hace en MercadoPagoConfiguration
            // Solo configurar timeouts adicionales si es necesario
            MercadoPagoConfig.setConnectionTimeout(5000);
            MercadoPagoConfig.setSocketTimeout(10000);

            // Inicializar el cliente
            this.preferenceClient = new PreferenceClient();

            log.info("MercadoPago SDK initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing MercadoPago SDK", e);
            throw new RuntimeException("Failed to initialize MercadoPago SDK", e);
        }
    }

    public DonationResponseDto createDonation(DonationRequestDto request) {
        try {
            log.info("Creating donation preference for amount: {} and payer: {}",
                    request.getAmount(), request.getPayerEmail());

            // Validar que el SDK esté inicializado
            if (preferenceClient == null) {
                throw new PaymentProcessingException("MercadoPago client not initialized");
            }

            // Generar referencia externa única
            String externalReference = "DONATION-" + UUID.randomUUID().toString().substring(0, 8);

            // Crear item de la preferencia - MÍNIMO REQUERIDO
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescription())
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(request.getAmount())
                    .build();


            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/donation/success")
                    .failure(frontendUrl + "/donation/failure")
                    .pending(frontendUrl + "/donation/pending")
                    .build();


            // PREFERENCIA MÍNIMA - Sin URLs de retorno para testear
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(Collections.singletonList(item))
                    .externalReference(externalReference)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .build();

            log.info("Creating minimal preference with external reference: {}", externalReference);

            // Crear preferencia en MercadoPago
            Preference preference = preferenceClient.create(preferenceRequest);

            log.info("Donation preference created successfully with ID: {}", preference.getId());

            // Construir respuesta
            return DonationResponseDto.builder()
                    .preferenceId(preference.getId())
                    .checkoutUrl(preference.getInitPoint())
                    .status("created")
                    .amount(request.getAmount())
                    .description(request.getDescription())
                    .payerEmail(request.getPayerEmail())
                    .externalReference(externalReference)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (MPApiException e) {
            log.error("MercadoPago API Error: Status: {}, Content: {}",
                    e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new PaymentProcessingException("Error de API de MercadoPago: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating donation preference", e);
            throw new PaymentProcessingException("Error inesperado al crear la preferencia de pago: " + e.getMessage(), e);
        }
    }
}