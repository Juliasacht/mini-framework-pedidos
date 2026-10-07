package br.com.atividade.pedidos.observador;

import br.com.atividade.pedidos.modelo.ResultadoPedido;

/** Demonstração local: escreve o evento de auditoria no console. */
public class LogAuditoria implements ObservadorPedido {

    @Override
    public void aoProcessar(ResultadoPedido resultado) {
        System.out.printf("[Auditoria] Pedido %s processado; subtotal=%s, desconto=%s, frete=%s, total=%s.%n",
                resultado.pedido().id(), resultado.subtotal().toPlainString(),
                resultado.desconto().toPlainString(), resultado.frete().toPlainString(),
                resultado.total().toPlainString());
    }
}
