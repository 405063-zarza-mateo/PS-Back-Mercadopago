package org.utn.tup.psbackmercadopago.Config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("MercadoPago configuration initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing MercadoPago configuration", e);
            throw new RuntimeException("Failed to initialize MercadoPago", e);
        }
    }
}
