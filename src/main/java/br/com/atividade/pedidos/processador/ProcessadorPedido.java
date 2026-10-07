package br.com.atividade.pedidos.processador;

import br.com.atividade.pedidos.desconto.CalculadoraDesconto;
import br.com.atividade.pedidos.modelo.Pedido;
import br.com.atividade.pedidos.modelo.ResultadoPedido;
import br.com.atividade.pedidos.observador.ObservadorPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ProcessadorPedido {

    private CalculadoraDesconto calculadoraDesconto;
    private final List<ObservadorPedido> observadores = new ArrayList<>();

    protected ProcessadorPedido(CalculadoraDesconto calculadoraDesconto) {
        definirCalculadoraDesconto(calculadoraDesconto);
    }

    /** Template Method: as subclasses não podem alterar a ordem destas etapas. */
    public final ResultadoPedido processar(Pedido pedido) {
        validar(pedido);
        ResultadoPedido resultado = calcularTotal(pedido);
        notificar(resultado);
        return resultado;
    }

    protected abstract void validar(Pedido pedido);

    protected abstract ResultadoPedido calcularTotal(Pedido pedido);

    protected final void notificar(ResultadoPedido resultado) {
        // Usa os observadores registrados no início desta notificação.
        for (ObservadorPedido observador : List.copyOf(observadores)) {
            observador.aoProcessar(resultado);
        }
    }

    public final void registrarObservador(ObservadorPedido observador) {
        Objects.requireNonNull(observador, "O observador é obrigatório.");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public final void removerObservador(ObservadorPedido observador) {
        observadores.remove(observador);
    }

    public final void definirCalculadoraDesconto(CalculadoraDesconto calculadoraDesconto) {
        this.calculadoraDesconto = Objects.requireNonNull(
                calculadoraDesconto, "A estratégia de desconto é obrigatória.");
    }

    protected final CalculadoraDesconto getCalculadoraDesconto() {
        return calculadoraDesconto;
    }

    protected final void validarItens(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("O pedido é obrigatório.");
        }
        if (pedido.itens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }
    }

    protected final BigDecimal calcularDesconto(BigDecimal subtotal) {
        BigDecimal desconto = calculadoraDesconto.calcular(subtotal);
        if (desconto == null || desconto.signum() < 0 || desconto.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("O desconto deve estar entre zero e o subtotal dos itens.");
        }
        return desconto.setScale(2, RoundingMode.HALF_UP);
    }
}
