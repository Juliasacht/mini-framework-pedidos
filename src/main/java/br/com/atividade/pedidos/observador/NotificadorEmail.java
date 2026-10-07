package br.com.atividade.pedidos.observador;

import br.com.atividade.pedidos.modelo.ResultadoPedido;

/** Demonstração local: não envia e-mails reais. */
public class NotificadorEmail implements ObservadorPedido {

    @Override
    public void aoProcessar(ResultadoPedido resultado) {
        System.out.printf("[E-mail simulado] Pedido %s para %s: total R$ %s.%n",
                resultado.pedido().id(), resultado.pedido().emailCliente(),
                resultado.total().toPlainString());
    }
}
