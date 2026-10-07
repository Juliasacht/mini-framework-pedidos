package br.com.atividade.pedidos.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Pedido(String id, String emailCliente, String enderecoEntrega, List<ItemPedido> itens) {

    public Pedido {
        // A cópia impede mudanças externas nos itens após a criação do pedido.
        itens = List.copyOf(itens);
    }

    public BigDecimal subtotal() {
        return itens.stream()
                .map(item -> item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
