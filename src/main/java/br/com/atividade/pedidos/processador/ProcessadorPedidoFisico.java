package br.com.atividade.pedidos.processador;

import br.com.atividade.pedidos.desconto.CalculadoraDesconto;
import br.com.atividade.pedidos.modelo.Pedido;
import br.com.atividade.pedidos.modelo.ResultadoPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProcessadorPedidoFisico extends ProcessadorPedido {

    private final BigDecimal frete;

    public ProcessadorPedidoFisico(CalculadoraDesconto calculadoraDesconto, BigDecimal frete) {
        super(calculadoraDesconto);
        if (frete == null || frete.signum() < 0) {
            throw new IllegalArgumentException("O frete deve ser maior ou igual a zero.");
        }
        this.frete = frete.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    protected void validar(Pedido pedido) {
        validarItens(pedido);
        if (pedido.enderecoEntrega() == null || pedido.enderecoEntrega().isBlank()) {
            throw new IllegalArgumentException("O pedido físico exige um endereço de entrega.");
        }
    }

    @Override
    protected ResultadoPedido calcularTotal(Pedido pedido) {
        BigDecimal subtotal = pedido.subtotal();
        BigDecimal desconto = calcularDesconto(subtotal);
        BigDecimal total = subtotal.subtract(desconto).add(frete).setScale(2, RoundingMode.HALF_UP);
        return new ResultadoPedido(pedido, subtotal, desconto, frete, total);
    }
}
