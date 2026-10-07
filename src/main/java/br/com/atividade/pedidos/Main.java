package br.com.atividade.pedidos;

import br.com.atividade.pedidos.desconto.DescontoClienteComum;
import br.com.atividade.pedidos.desconto.DescontoClienteVip;
import br.com.atividade.pedidos.modelo.ItemPedido;
import br.com.atividade.pedidos.modelo.Pedido;
import br.com.atividade.pedidos.modelo.ResultadoPedido;
import br.com.atividade.pedidos.observador.BaixaEstoque;
import br.com.atividade.pedidos.observador.LogAuditoria;
import br.com.atividade.pedidos.observador.NotificadorEmail;
import br.com.atividade.pedidos.processador.ProcessadorPedido;
import br.com.atividade.pedidos.processador.ProcessadorPedidoDigital;
import br.com.atividade.pedidos.processador.ProcessadorPedidoFisico;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Mini-Framework de Processamento de Pedidos");
        System.out.println("Template Method: validar -> calcularTotal -> notificar.");
        System.out.println("Comum: 0% de desconto. VIP: 10% sobre os itens. Frete físico: R$ 15.00.");

        var comum = new DescontoClienteComum();
        var vip = new DescontoClienteVip();
        var email = new NotificadorEmail();
        var estoque = new BaixaEstoque();
        var auditoria = new LogAuditoria();

        var itensFisicos = List.of(new ItemPedido("Livro de padrões de projeto", new BigDecimal("50.00"), 2));
        var itensDigitais = List.of(new ItemPedido("Curso de Java", new BigDecimal("80.00"), 1));

        var fisico = new ProcessadorPedidoFisico(comum, new BigDecimal("15.00"));
        fisico.registrarObservador(email);
        fisico.registrarObservador(estoque);
        fisico.registrarObservador(auditoria);
        executar("Físico / comum / e-mail, estoque e auditoria", fisico,
                new Pedido("FIS-001", "ana@example.com", "Rua das Flores, 100", itensFisicos));

        // A mesma instância muda sua estratégia e seus interessados durante a execução.
        fisico.definirCalculadoraDesconto(vip);
        fisico.removerObservador(email);
        executar("Físico / VIP / estoque e auditoria (estratégia alterada)", fisico,
                new Pedido("FIS-002", "bruno@example.com", "Avenida Central, 200", itensFisicos));

        var digital = new ProcessadorPedidoDigital(comum);
        digital.registrarObservador(email);
        executar("Digital / comum / somente e-mail", digital,
                new Pedido("DIG-001", "carla@example.com", null, itensDigitais));

        digital.definirCalculadoraDesconto(vip);
        digital.removerObservador(email);
        digital.registrarObservador(auditoria);
        executar("Digital / VIP / somente auditoria (estratégia alterada)", digital,
                new Pedido("DIG-002", "diego@example.com", null, itensDigitais));

        System.out.println("\n=== Pedido inválido: cálculo e notificações não devem ocorrer ===");
        try {
            digital.processar(new Pedido("DIG-INVALIDO", "email-invalido", null, itensDigitais));
        } catch (IllegalArgumentException erro) {
            System.out.println("Validação interrompeu o processamento: " + erro.getMessage());
        }

        digital.removerObservador(auditoria);
        executar("Digital / VIP / sem observadores, após a falha de validação", digital,
                new Pedido("DIG-003", "elisa@example.com", null, itensDigitais));

        System.out.println("\nO método final processar preserva a mesma sequência em todos os exemplos.");
    }

    private static void executar(String titulo, ProcessadorPedido processador, Pedido pedido) {
        System.out.println("\n=== " + titulo + " ===");
        ResultadoPedido resultado = processador.processar(pedido);
        System.out.printf("Resultado: itens R$ %s - desconto R$ %s + frete R$ %s = total R$ %s.%n",
                resultado.subtotal().toPlainString(), resultado.desconto().toPlainString(),
                resultado.frete().toPlainString(), resultado.total().toPlainString());
    }
}
