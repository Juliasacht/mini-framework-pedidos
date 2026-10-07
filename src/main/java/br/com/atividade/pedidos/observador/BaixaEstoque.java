package br.com.atividade.pedidos.observador;

import br.com.atividade.pedidos.modelo.ResultadoPedido;

/** Demonstração local: apenas descreve a baixa, sem alterar um estoque real. */
public class BaixaEstoque implements ObservadorPedido {

    @Override
    public void aoProcessar(ResultadoPedido resultado) {
        resultado.pedido().itens().forEach(item ->
                System.out.printf("[Estoque simulado] Pedido %s: baixa de %d unidade(s) de %s.%n",
                        resultado.pedido().id(), item.quantidade(), item.descricao()));
    }
}
