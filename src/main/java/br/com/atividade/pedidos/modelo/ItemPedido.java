package br.com.atividade.pedidos.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ItemPedido(String descricao, BigDecimal precoUnitario, int quantidade) {

    public ItemPedido {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do item é obrigatória.");
        }
        if (precoUnitario == null || precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("O preço unitário deve ser maior ou igual a zero.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        precoUnitario = precoUnitario.setScale(2, RoundingMode.HALF_UP);
    }
}
