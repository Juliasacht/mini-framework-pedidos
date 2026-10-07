package br.com.atividade.pedidos;

import br.com.atividade.pedidos.desconto.CalculadoraDesconto;
import br.com.atividade.pedidos.desconto.DescontoClienteComum;
import br.com.atividade.pedidos.desconto.DescontoClienteVip;
import br.com.atividade.pedidos.modelo.ItemPedido;
import br.com.atividade.pedidos.modelo.Pedido;
import br.com.atividade.pedidos.modelo.ResultadoPedido;
import br.com.atividade.pedidos.observador.ObservadorPedido;
import br.com.atividade.pedidos.processador.ProcessadorPedido;
import br.com.atividade.pedidos.processador.ProcessadorPedidoDigital;
import br.com.atividade.pedidos.processador.ProcessadorPedidoFisico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessamentoPedidoTest {

    @Test
    void pedidoFisicoComumSomaProdutosEFrete() {
        var processador = new ProcessadorPedidoFisico(new DescontoClienteComum(), valor("15.00"));

        ResultadoPedido resultado = processador.processar(pedidoValido());

        assertAll(
                () -> assertEquals(valor("100.00"), resultado.subtotal()),
                () -> assertEquals(valor("0.00"), resultado.desconto()),
                () -> assertEquals(valor("15.00"), resultado.frete()),
                () -> assertEquals(valor("115.00"), resultado.total())
        );
    }

    @Test
    void pedidoFisicoVipAplicaDescontoSomenteAosProdutos() {
        var processador = new ProcessadorPedidoFisico(new DescontoClienteVip(), valor("15.00"));

        ResultadoPedido resultado = processador.processar(pedidoValido());

        assertAll(
                () -> assertEquals(valor("10.00"), resultado.desconto()),
                () -> assertEquals(valor("15.00"), resultado.frete()),
                () -> assertEquals(valor("105.00"), resultado.total())
        );
    }

    @Test
    void pedidoDigitalComumNaoCobraFrete() {
        var processador = new ProcessadorPedidoDigital(new DescontoClienteComum());

        ResultadoPedido resultado = processador.processar(pedidoValido());

        assertAll(
                () -> assertEquals(valor("100.00"), resultado.subtotal()),
                () -> assertEquals(valor("0.00"), resultado.desconto()),
                () -> assertEquals(valor("0.00"), resultado.frete()),
                () -> assertEquals(valor("100.00"), resultado.total())
        );
    }

    @Test
    void estrategiaPodeSerTrocadaNoMesmoProcessador() {
        var processador = new ProcessadorPedidoDigital(new DescontoClienteComum());
        Pedido pedido = pedidoValido();

        ResultadoPedido comum = processador.processar(pedido);
        processador.definirCalculadoraDesconto(new DescontoClienteVip());
        ResultadoPedido vip = processador.processar(pedido);

        assertAll(
                () -> assertEquals(valor("100.00"), comum.total()),
                () -> assertEquals(valor("10.00"), vip.desconto()),
                () -> assertEquals(valor("90.00"), vip.total()),
                () -> assertEquals(valor("0.00"), vip.frete()),
                () -> assertEquals(valor("100.00"), pedido.subtotal())
        );
    }

    @Test
    void estrategiaPersonalizadaRecebeSubtotalSemFrete() {
        List<BigDecimal> subtotaisRecebidos = new ArrayList<>();
        CalculadoraDesconto estrategia = subtotal -> {
            subtotaisRecebidos.add(subtotal);
            return subtotal.multiply(valor("0.25"));
        };
        var processador = new ProcessadorPedidoFisico(estrategia, valor("20.00"));

        ResultadoPedido resultado = processador.processar(pedidoValido());

        assertEquals(List.of(valor("100.00")), subtotaisRecebidos);
        assertEquals(valor("95.00"), resultado.total());
    }

    @Test
    void descontoUsaArredondamentoHalfUpEmDuasCasas() {
        var processador = new ProcessadorPedidoDigital(new DescontoClienteVip());
        Pedido pedido = new Pedido("ARREDONDAMENTO", "cliente@exemplo.com", null,
                List.of(new ItemPedido("Curso", valor("100.05"), 1)));

        ResultadoPedido resultado = processador.processar(pedido);

        assertEquals(valor("10.01"), resultado.desconto());
        assertEquals(valor("90.04"), resultado.total());
    }

    @Test
    void reprocessamentoNaoAcumulaDescontoNemAlteraPedido() {
        var processador = new ProcessadorPedidoFisico(new DescontoClienteVip(), valor("15.00"));
        Pedido pedido = pedidoValido();

        ResultadoPedido primeiro = processador.processar(pedido);
        ResultadoPedido segundo = processador.processar(pedido);

        assertEquals(primeiro, segundo);
        assertEquals(valor("105.00"), segundo.total());
        assertEquals(valor("100.00"), pedido.subtotal());
    }

    @Test
    void observadoresRecebemResultadoEDeixamDeSerChamadosAposRemocao() {
        var processador = new ProcessadorPedidoDigital(new DescontoClienteComum());
        List<ResultadoPedido> notificacoesEmail = new ArrayList<>();
        List<ResultadoPedido> notificacoesAuditoria = new ArrayList<>();
        ObservadorPedido email = notificacoesEmail::add;
        ObservadorPedido auditoria = notificacoesAuditoria::add;
        processador.registrarObservador(email);
        processador.registrarObservador(auditoria);

        ResultadoPedido primeiro = processador.processar(pedidoValido());
        processador.removerObservador(email);
        ResultadoPedido segundo = processador.processar(pedidoValido());

        assertEquals(1, notificacoesEmail.size());
        assertEquals(2, notificacoesAuditoria.size());
        assertSame(primeiro, notificacoesEmail.get(0));
        assertSame(primeiro, notificacoesAuditoria.get(0));
        assertSame(segundo, notificacoesAuditoria.get(1));
    }

    @Test
    void templateExecutaValidacaoCalculoENotificacaoNessaOrdem() {
        List<String> etapas = new ArrayList<>();
        var processador = new ProcessadorProbe(etapas, false);
        processador.registrarObservador(resultado -> etapas.add("notificar"));

        processador.processar(pedidoValido());

        assertEquals(List.of("validar", "calcularTotal", "notificar"), etapas);
    }

    @Test
    void templateFinalImpedeSubclassesDeAlterarEsqueleto() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(ProcessadorPedido.class
                .getDeclaredMethod("processar", Pedido.class).getModifiers()));
    }

    @Test
    void erroDeValidacaoInterrompeCalculoENotificacao() {
        List<String> etapas = new ArrayList<>();
        var processador = new ProcessadorProbe(etapas, true);
        processador.registrarObservador(resultado -> etapas.add("notificar"));

        assertThrows(IllegalArgumentException.class, () -> processador.processar(pedidoValido()));

        assertEquals(List.of("validar"), etapas);
    }

    @Test
    void pedidoFisicoExigeEnderecoAntesDeCalcularOuNotificar() {
        List<String> chamadas = new ArrayList<>();
        var processador = new ProcessadorPedidoFisico(subtotal -> {
            chamadas.add("calcular desconto");
            return BigDecimal.ZERO;
        }, valor("15.00"));
        processador.registrarObservador(resultado -> chamadas.add("notificar"));
        Pedido pedido = new Pedido("SEM-ENDERECO", "cliente@exemplo.com", "  ", itens());

        assertThrows(IllegalArgumentException.class, () -> processador.processar(pedido));
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void pedidoDigitalExigeEmailValidoAntesDeCalcularOuNotificar() {
        List<String> chamadas = new ArrayList<>();
        var processador = new ProcessadorPedidoDigital(subtotal -> {
            chamadas.add("calcular desconto");
            return BigDecimal.ZERO;
        });
        processador.registrarObservador(resultado -> chamadas.add("notificar"));
        Pedido pedido = new Pedido("EMAIL-INVALIDO", "email-invalido", null, itens());

        assertThrows(IllegalArgumentException.class, () -> processador.processar(pedido));
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void regrasEspecificasPermitemFisicoSemEmailEDigitalSemEndereco() {
        var fisico = new ProcessadorPedidoFisico(new DescontoClienteComum(), valor("15.00"));
        var digital = new ProcessadorPedidoDigital(new DescontoClienteComum());
        Pedido semEmail = new Pedido("FISICO", null, "Rua Principal, 10", itens());
        Pedido semEndereco = new Pedido("DIGITAL", "cliente@exemplo.com", null, itens());

        assertDoesNotThrow(() -> fisico.processar(semEmail));
        assertDoesNotThrow(() -> digital.processar(semEndereco));
    }

    @Test
    void ambosProcessadoresRejeitamPedidoSemItens() {
        var fisico = new ProcessadorPedidoFisico(new DescontoClienteComum(), valor("15.00"));
        var digital = new ProcessadorPedidoDigital(new DescontoClienteComum());
        Pedido vazio = new Pedido("VAZIO", "cliente@exemplo.com", "Rua Principal, 10", List.of());

        assertThrows(IllegalArgumentException.class, () -> fisico.processar(vazio));
        assertThrows(IllegalArgumentException.class, () -> digital.processar(vazio));
    }

    private static Pedido pedidoValido() {
        return new Pedido("PED-001", "cliente@exemplo.com", "Rua Principal, 10", itens());
    }

    private static List<ItemPedido> itens() {
        return List.of(
                new ItemPedido("Produto A", valor("40.00"), 2),
                new ItemPedido("Produto B", valor("20.00"), 1)
        );
    }

    private static BigDecimal valor(String valor) {
        return new BigDecimal(valor);
    }

    private static final class ProcessadorProbe extends ProcessadorPedido {
        private final List<String> etapas;
        private final boolean falhaNaValidacao;

        private ProcessadorProbe(List<String> etapas, boolean falhaNaValidacao) {
            super(new DescontoClienteComum());
            this.etapas = etapas;
            this.falhaNaValidacao = falhaNaValidacao;
        }

        @Override
        protected void validar(Pedido pedido) {
            etapas.add("validar");
            if (falhaNaValidacao) {
                throw new IllegalArgumentException("Pedido inválido para o teste");
            }
        }

        @Override
        protected ResultadoPedido calcularTotal(Pedido pedido) {
            etapas.add("calcularTotal");
            return new ResultadoPedido(pedido, pedido.subtotal(), valor("0.00"),
                    valor("0.00"), pedido.subtotal());
        }
    }
}
