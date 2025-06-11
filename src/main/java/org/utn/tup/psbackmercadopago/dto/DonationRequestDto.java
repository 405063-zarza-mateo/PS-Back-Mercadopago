package org.utn.tup.psbackmercadopago.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequestDto {

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal amount;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String description;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String payerEmail;

    @NotBlank(message = "El nombre del pagador es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String payerName;


    private String customSuccessUrl;
    private String customFailureUrl;
    private String customPendingUrl;

    // Referencia externa opcional
    private String externalReference;
}
