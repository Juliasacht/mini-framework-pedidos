package br.com.atividade.pedidos.processador;

import br.com.atividade.pedidos.desconto.CalculadoraDesconto;
import br.com.atividade.pedidos.modelo.Pedido;
import br.com.atividade.pedidos.modelo.ResultadoPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class ProcessadorPedidoDigital extends ProcessadorPedido {

    // Validação didática do formato; não verifica a existência da caixa postal.
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public ProcessadorPedidoDigital(CalculadoraDesconto calculadoraDesconto) {
        super(calculadoraDesconto);
    }

    @Override
    protected void validar(Pedido pedido) {
        validarItens(pedido);
        if (pedido.emailCliente() == null || !EMAIL.matcher(pedido.emailCliente()).matches()) {
            throw new IllegalArgumentException("O pedido digital exige um e-mail válido para a entrega.");
        }
    }

    @Override
    protected ResultadoPedido calcularTotal(Pedido pedido) {
        BigDecimal subtotal = pedido.subtotal();
        BigDecimal desconto = calcularDesconto(subtotal);
        BigDecimal total = subtotal.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
        return new ResultadoPedido(pedido, subtotal, desconto, new BigDecimal("0.00"), total);
    }
}
