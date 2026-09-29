package com.deber.inventario.inventario;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioRepository {

    private static final RowMapper<Existencia> MAPPER = (rs, i) -> new Existencia(
            rs.getLong("id"),
            rs.getLong("producto_id"),
            rs.getString("producto"),
            rs.getLong("bodega_id"),
            rs.getString("bodega"),
            rs.getDouble("cantidad"),
            rs.getDouble("consumo_7d"),
            rs.getDouble("consumo_14d"));

    private static final String SELECT = """
            SELECT e.id, e.producto_id, p.nombre AS producto, e.bodega_id, b.nombre AS bodega,
                   e.cantidad, e.consumo_7d, e.consumo_14d
            FROM existencia e
            JOIN producto p ON p.id = e.producto_id
            JOIN bodega b ON b.id = e.bodega_id
            """;

    private final JdbcClient jdbc;

    public InventarioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Existencia> todas() {
        return jdbc.sql(SELECT + " ORDER BY p.nombre, b.nombre").query(MAPPER).list();
    }
}
