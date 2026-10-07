package br.com.atividade.pedidos.desconto;

import java.math.BigDecimal;

/** Strategy: calcula o valor do desconto aplicado apenas ao subtotal dos itens. */
@FunctionalInterface
public interface CalculadoraDesconto {
    BigDecimal calcular(BigDecimal subtotal);
}
