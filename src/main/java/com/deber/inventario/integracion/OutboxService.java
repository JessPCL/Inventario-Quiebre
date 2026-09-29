package com.deber.inventario.integracion;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

    public record Evento(long id, String tipo, String payload) {
    }

    private static final RowMapper<Evento> MAPPER = (rs, i) ->
            new Evento(rs.getLong("id"), rs.getString("tipo"), rs.getString("payload"));

    private final JdbcClient jdbc;

    public OutboxService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void encolar(String tipo, String payload) {
        jdbc.sql("INSERT INTO outbox (tipo, payload) VALUES (:tipo, :payload)")
                .param("tipo", tipo)
                .param("payload", payload)
                .update();
    }

    public List<Evento> pendientes(int limite) {
        return jdbc.sql("SELECT id, tipo, payload FROM outbox WHERE publicado_en IS NULL ORDER BY id LIMIT :l")
                .param("l", limite)
                .query(MAPPER)
                .list();
    }

    public void marcarPublicado(long id) {
        jdbc.sql("UPDATE outbox SET publicado_en = now() WHERE id = :id").param("id", id).update();
    }
}
