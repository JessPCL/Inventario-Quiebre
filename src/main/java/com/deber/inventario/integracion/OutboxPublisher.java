package com.deber.inventario.integracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Publica en RabbitMQ los eventos guardados en la tabla outbox (entrega al menos una vez). */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxService outbox;
    private final RabbitTemplate rabbit;

    public OutboxPublisher(OutboxService outbox, RabbitTemplate rabbit) {
        this.outbox = outbox;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelay = 2000)
    public void publicar() {
        for (OutboxService.Evento ev : outbox.pendientes(50)) {
            try {
                rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, ev.payload());
                outbox.marcarPublicado(ev.id());
            } catch (AmqpException e) {
                log.warn("RabbitMQ no disponible, se reintenta luego: {}", e.getMessage());
                return;
            }
        }
    }
}
