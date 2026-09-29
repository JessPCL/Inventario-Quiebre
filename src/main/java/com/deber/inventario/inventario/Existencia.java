package com.deber.inventario.inventario;

public record Existencia(
        long id,
        long productoId,
        String producto,
        long bodegaId,
        String bodega,
        double cantidad,
        double consumo7d,
        double consumo14d) {
}
