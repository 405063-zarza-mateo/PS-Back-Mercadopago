package org.utn.tup.psbackmercadopago.Controller;

import com.mercadopago.net.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.utn.tup.psbackmercadopago.dto.DonationRequestDto;
import org.utn.tup.psbackmercadopago.dto.DonationResponseDto;
import org.utn.tup.psbackmercadopago.dto.PaymentStatusDto;
import org.utn.tup.psbackmercadopago.service.DonationService;
import org.utn.tup.psbackmercadopago.service.PaymentService;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/donation")
@Tag(name = "Donation", description = "Api para la gestion de donaciones con mercadopago")
public class DonationController {


    private final DonationService donationService;
    private final PaymentService paymentService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Operation(summary = "Crear nueva donación", description = "Crea una preferencia de pago en MercadoPago para una donación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = DonationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping
    public ResponseEntity<DonationResponseDto> createDonation(
            @Valid @RequestBody DonationRequestDto request) {

        log.info("Received donation request for amount: {} from: {}",
                request.getAmount(), request.getPayerEmail());

        DonationResponseDto response = donationService.createDonation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Callback de pago exitoso", description = "Endpoint llamado por MercadoPago cuando el pago es exitoso")
    @GetMapping("/success")
    public RedirectView handleSuccess(
            @Parameter(description = "ID del pago") @RequestParam(required = false) String payment_id,
            @Parameter(description = "Estado del pago") @RequestParam(required = false) String status,
            @Parameter(description = "ID de la preferencia") @RequestParam(required = false) String preference_id,
            @Parameter(description = "Referencia externa") @RequestParam(required = false) String external_reference,
            @RequestParam Map<String, String> allParams) {

        log.info("Payment success callback - Payment ID: {}, Status: {}", payment_id, status);

        try {
            PaymentStatusDto paymentStatus = paymentService.processPaymentCallback(allParams);
            log.info("Payment processed successfully: {}", paymentStatus);

            // Redirigir al frontend con parámetros de éxito
            String redirectUrl = String.format("%s/payment/success?payment_id=%s&status=%s&reference=%s",
                    frontendUrl, payment_id, status, external_reference);

            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing success callback", e);
            return new RedirectView(frontendUrl + "/payment/error");
        }
    }

    @Operation(summary = "Callback de pago fallido", description = "Endpoint llamado por MercadoPago cuando el pago falla")
    @GetMapping("/failure")
    public RedirectView handleFailure(
            @Parameter(description = "ID del pago") @RequestParam(required = false) String payment_id,
            @Parameter(description = "Estado del pago") @RequestParam(required = false) String status,
            @Parameter(description = "ID de la preferencia") @RequestParam(required = false) String preference_id,
            @Parameter(description = "Referencia externa") @RequestParam(required = false) String external_reference,
            @RequestParam Map<String, String> allParams) {

        log.warn("Payment failure callback - Payment ID: {}, Status: {}", payment_id, status);

        try {
            PaymentStatusDto paymentStatus = paymentService.processPaymentCallback(allParams);
            log.info("Payment failure processed: {}", paymentStatus);

            // Redirigir al frontend con parámetros de fallo
            String redirectUrl = String.format("%s/payment/failure?payment_id=%s&status=%s&reference=%s",
                    frontendUrl, payment_id, status, external_reference);

            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing failure callback", e);
            return new RedirectView(frontendUrl + "/payment/error");
        }
    }

    @Operation(summary = "Callback de pago pendiente", description = "Endpoint llamado por MercadoPago cuando el pago queda pendiente")
    @GetMapping("/pending")
    public RedirectView handlePending(
            @Parameter(description = "ID del pago") @RequestParam(required = false) String payment_id,
            @Parameter(description = "Estado del pago") @RequestParam(required = false) String status,
            @Parameter(description = "ID de la preferencia") @RequestParam(required = false) String preference_id,
            @Parameter(description = "Referencia externa") @RequestParam(required = false) String external_reference,
            @RequestParam Map<String, String> allParams) {

        log.info("Payment pending callback - Payment ID: {}, Status: {}", payment_id, status);

        try {
            PaymentStatusDto paymentStatus = paymentService.processPaymentCallback(allParams);
            log.info("Payment pending processed: {}", paymentStatus);

            // Redirigir al frontend con parámetros de pendiente
            String redirectUrl = String.format("%s/payment/pending?payment_id=%s&status=%s&reference=%s",
                    frontendUrl, payment_id, status, external_reference);

            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing pending callback", e);
            return new RedirectView(frontendUrl + "/payment/error");
        }
    }

    @Operation(summary = "Webhook de notificaciones", description = "Endpoint para recibir notificaciones IPN de MercadoPago")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id) {

        log.info("Received webhook notification - Type: {}, ID: {}, Payload: {}", type, id, payload);

        try {
            // Procesar webhook según el tipo
            if ("payment".equals(type) && id != null) {
                PaymentStatusDto paymentStatus = paymentService.getPaymentStatus(id);
                log.info("Webhook payment status: {}", paymentStatus);

                // Aquí puedes agregar lógica adicional como:
                // - Actualizar base de datos
                // - Enviar notificaciones
                // - Triggers de negocio
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }

    @Operation(summary = "Obtener estado de pago", description = "Consulta el estado actual de un pago por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pago obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentStatusDto.class))),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<PaymentStatusDto> getPaymentStatus(
            @Parameter(description = "ID del pago a consultar") @PathVariable String paymentId) {

        log.info("Getting payment status for ID: {}", paymentId);

        PaymentStatusDto paymentStatus = paymentService.getPaymentStatus(paymentId);

        return ResponseEntity.ok(paymentStatus);
    }

    @Operation(summary = "Health check", description = "Endpoint para verificar el estado del servicio")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MercadoPago Donation Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
}
}
