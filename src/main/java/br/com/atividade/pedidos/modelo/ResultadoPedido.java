package br.com.atividade.pedidos.modelo;

import java.math.BigDecimal;

/** Os valores calculados são compartilhados com todos os observadores. */
public record ResultadoPedido(
        Pedido pedido,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal frete,
        BigDecimal total) {
}
