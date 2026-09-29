package com.deber.inventario.integracion;

public record EventoAprobada(
        long recomendacionId,
        String tipo,
        long productoId,
        long bodegaDestinoId,
        Long bodegaOrigenId,
        double cantidad,
        String aprobadaPor) {
}
