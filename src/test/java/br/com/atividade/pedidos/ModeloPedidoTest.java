package br.com.atividade.pedidos;

import br.com.atividade.pedidos.modelo.ItemPedido;
import br.com.atividade.pedidos.modelo.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModeloPedidoTest {

    @Test
    void subtotalConsideraQuantidadeDeCadaItem() {
        Pedido pedido = new Pedido("001", null, null, List.of(
                new ItemPedido("Livro", new BigDecimal("19.90"), 3),
                new ItemPedido("Caderno", new BigDecimal("7.50"), 2)
        ));

        assertEquals(new BigDecimal("74.70"), pedido.subtotal());
    }

    @Test
    void pedidoFazCopiaDefensivaDaListaDeItens() {
        ItemPedido item = new ItemPedido("Livro", new BigDecimal("20.00"), 1);
        List<ItemPedido> listaOriginal = new ArrayList<>(List.of(item));
        Pedido pedido = new Pedido("001", null, null, listaOriginal);

        listaOriginal.clear();

        assertEquals(List.of(item), pedido.itens());
        assertEquals(new BigDecimal("20.00"), pedido.subtotal());
        assertThrows(UnsupportedOperationException.class, () -> pedido.itens().clear());
    }

    @Test
    void itemRejeitaQuantidadeZeroOuNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("Livro", BigDecimal.TEN, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("Livro", BigDecimal.TEN, -1));
    }

    @Test
    void itemRejeitaPrecoNegativoOuAusente() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("Livro", new BigDecimal("-0.01"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("Livro", null, 1));
    }

    @Test
    void itemRejeitaDescricaoAusenteOuEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(null, BigDecimal.TEN, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido("  ", BigDecimal.TEN, 1));
    }

    @Test
    void itemGratuitoEPermitido() {
        ItemPedido item = new ItemPedido("Material gratuito", BigDecimal.ZERO, 1);

        assertEquals(new BigDecimal("0.00"), item.precoUnitario());
    }

    @Test
    void precoUnitarioUsaDuasCasasEArredondamentoHalfUp() {
        ItemPedido item = new ItemPedido("Livro", new BigDecimal("19.995"), 1);

        assertEquals(new BigDecimal("20.00"), item.precoUnitario());
    }
}
