package com.deber.inventario.integracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "quiebre.eventos";
    public static final String ROUTING_KEY = "recomendacion.aprobada";
    public static final String COLA = "compras.ejecutar";
    public static final String COLA_DLQ = "compras.ejecutar.dlq";

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    Queue cola() {
        return QueueBuilder.durable(COLA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COLA_DLQ)
                .build();
    }

    @Bean
    Queue colaMuerta() {
        return QueueBuilder.durable(COLA_DLQ).build();
    }

    @Bean
    Binding enlace() {
        return BindingBuilder.bind(cola()).to(exchange()).with(ROUTING_KEY);
    }
}
