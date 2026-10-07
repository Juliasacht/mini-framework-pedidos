package br.com.atividade.pedidos.desconto;

import java.math.BigDecimal;

public class DescontoClienteComum implements CalculadoraDesconto {

    @Override
    public BigDecimal calcular(BigDecimal subtotal) {
        return new BigDecimal("0.00");
    }
}
