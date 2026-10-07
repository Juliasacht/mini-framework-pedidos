package br.com.atividade.pedidos.desconto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescontoClienteVip implements CalculadoraDesconto {

    private static final BigDecimal TAXA = new BigDecimal("0.10");

    @Override
    public BigDecimal calcular(BigDecimal subtotal) {
        return subtotal.multiply(TAXA).setScale(2, RoundingMode.HALF_UP);
    }
}
