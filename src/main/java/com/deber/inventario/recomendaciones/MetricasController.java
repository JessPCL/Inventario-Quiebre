package com.deber.inventario.recomendaciones;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metricas")
public class MetricasController {

    private final JdbcClient jdbc;

    public MetricasController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String, Object> metricas() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        jdbc.sql("SELECT estado, count(*) AS c FROM recomendacion GROUP BY estado ORDER BY estado")
                .query((rs, i) -> {
                    porEstado.put(rs.getString("estado"), rs.getLong("c"));
                    return i;
                })
                .list();
        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        long degradadas = jdbc.sql("SELECT count(*) FROM recomendacion WHERE degradada")
                .query(Long.class).single();
        double segundosAprobacion = jdbc.sql("""
                SELECT coalesce(avg(extract(epoch FROM (decidida_en - creada_en))), 0)
                FROM recomendacion WHERE decidida_en IS NOT NULL
                """).query(Double.class).single();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", total);
        r.put("porEstado", porEstado);
        r.put("degradadas", degradadas);
        r.put("porcentajeDegradadas", total == 0 ? 0.0 : Math.round(10000.0 * degradadas / total) / 100.0);
        r.put("tiempoAprobacionPromedioSeg", Math.round(segundosAprobacion * 10.0) / 10.0);
        return r;
    }
}
