package com.deber.inventario.auditoria;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    public record Registro(long id, String entidad, long entidadId, String accion,
                           String usuario, String detalle, LocalDateTime creadaEn) {
    }

    private static final RowMapper<Registro> MAPPER = (rs, i) -> new Registro(
            rs.getLong("id"),
            rs.getString("entidad"),
            rs.getLong("entidad_id"),
            rs.getString("accion"),
            rs.getString("usuario"),
            rs.getString("detalle"),
            rs.getObject("creada_en", LocalDateTime.class));

    private final JdbcClient jdbc;

    public AuditoriaService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(String entidad, long entidadId, String accion, String usuario, String detalle) {
        jdbc.sql("""
                INSERT INTO auditoria (entidad, entidad_id, accion, usuario, detalle)
                VALUES (:entidad, :id, :accion, :usuario, :detalle)
                """)
                .param("entidad", entidad)
                .param("id", entidadId)
                .param("accion", accion)
                .param("usuario", usuario)
                .param("detalle", detalle == null ? "" : detalle)
                .update();
    }

    public List<Registro> ultimos(int limite) {
        return jdbc.sql("SELECT * FROM auditoria ORDER BY id DESC LIMIT :limite")
                .param("limite", limite)
                .query(MAPPER)
                .list();
    }
}
