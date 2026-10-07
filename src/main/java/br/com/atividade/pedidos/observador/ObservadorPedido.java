package br.com.atividade.pedidos.observador;

import br.com.atividade.pedidos.modelo.ResultadoPedido;

/** Observer: reage ao processamento bem-sucedido de um pedido. */
@FunctionalInterface
public interface ObservadorPedido {
    void aoProcessar(ResultadoPedido resultado);
}
