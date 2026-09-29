package com.deber.inventario.pronostico;

import com.deber.inventario.inventario.Existencia;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Consulta el servicio de pronóstico con timeout y un circuito simple.
 * Si falla, usa la regla de respaldo (consumo promedio de 14 días) y marca el resultado como degradado.
 */
@Service
public class PronosticoService {

    public record Resultado(double consumoDiario, boolean degradado) {
    }

    private static final Logger log = LoggerFactory.getLogger(PronosticoService.class);
    private static final int FALLOS_PARA_ABRIR = 3;
    private static final long SEGUNDOS_ABIERTO = 30;

    private final RestClient http;
    private final AtomicInteger fallosSeguidos = new AtomicInteger();
    private volatile Instant abiertoHasta = Instant.EPOCH;

    public PronosticoService(RestClient.Builder builder,
                             @Value("${pronostico.url}") String url,
                             @Value("${pronostico.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.http = builder.baseUrl(url).requestFactory(factory).build();
    }

    public Resultado consumoDiario(Existencia e) {
        if (Instant.now().isBefore(abiertoHasta)) {
            return respaldo(e);
        }
        try {
            Map<?, ?> r = http.get()
                    .uri(u -> u.path("/pronostico")
                            .queryParam("c7", e.consumo7d())
                            .queryParam("c14", e.consumo14d())
                            .build())
                    .retrieve()
                    .body(Map.class);
            double valor = ((Number) r.get("consumoDiario")).doubleValue();
            fallosSeguidos.set(0);
            return new Resultado(valor, false);
        } catch (Exception ex) {
            if (fallosSeguidos.incrementAndGet() >= FALLOS_PARA_ABRIR) {
                abiertoHasta = Instant.now().plusSeconds(SEGUNDOS_ABIERTO);
                fallosSeguidos.set(0);
                log.warn("Pronóstico no disponible: circuito abierto {} s", SEGUNDOS_ABIERTO);
            }
            return respaldo(e);
        }
    }

    private Resultado respaldo(Existencia e) {
        return new Resultado(e.consumo14d(), true);
    }
}
