package com.deber.inventario.integracion;

import com.deber.inventario.auditoria.AuditoriaService;
import com.deber.inventario.recomendaciones.RecomendacionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume las aprobaciones y las envía al sistema de compras.
 * Si falla, Spring AMQP reintenta con backoff y luego manda el mensaje a la cola muerta.
 */
@Component
public class ComprasListener {

    private static final Logger log = LoggerFactory.getLogger(ComprasListener.class);

    private final ObjectMapper mapper;
    private final ComprasClient compras;
    private final RecomendacionRepository repo;
    private final AuditoriaService auditoria;

    public ComprasListener(ObjectMapper mapper, ComprasClient compras,
                           RecomendacionRepository repo, AuditoriaService auditoria) {
        this.mapper = mapper;
        this.compras = compras;
        this.repo = repo;
        this.auditoria = auditoria;
    }

    @RabbitListener(queues = RabbitConfig.COLA)
    public void ejecutar(String payload) throws JsonProcessingException {
        EventoAprobada ev = mapper.readValue(payload, EventoAprobada.class);
        compras.enviar(ev);
        if (repo.marcarEjecutada(ev.recomendacionId()) > 0) {
            auditoria.registrar("RECOMENDACION", ev.recomendacionId(), "EJECUTADA", "sistema",
                    "Orden enviada a compras");
        }
        log.info("Recomendación {} ejecutada", ev.recomendacionId());
    }

    @RabbitListener(queues = RabbitConfig.COLA_DLQ)
    public void fallida(String payload) throws JsonProcessingException {
        EventoAprobada ev = mapper.readValue(payload, EventoAprobada.class);
        if (repo.marcarFallida(ev.recomendacionId()) > 0) {
            auditoria.registrar("RECOMENDACION", ev.recomendacionId(), "FALLIDA", "sistema",
                    "Compras no respondió tras varios intentos");
        }
        log.error("Recomendación {} FALLIDA: requiere atención del jefe de compras", ev.recomendacionId());
    }
}
