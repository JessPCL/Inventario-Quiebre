package com.deber.inventario.integracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adaptador hacia el sistema de compras existente. Cualquier error lanza excepción para reintentar. */
@Component
public class ComprasClient {

    private final RestClient http;

    public ComprasClient(RestClient.Builder builder,
                         @Value("${compras.url}") String url,
                         @Value("${compras.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.http = builder.baseUrl(url).requestFactory(factory).build();
    }

    public void enviar(EventoAprobada evento) {
        http.post()
                .uri("/ordenes")
                .header("Idempotency-Key", "rec-" + evento.recomendacionId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(evento)
                .retrieve()
                .toBodilessEntity();
    }
}
