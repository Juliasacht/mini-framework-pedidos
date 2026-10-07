# Mini-Framework de Processamento de Pedidos

Atividade em Java que combina **Template Method**, **Strategy** e **Observer** no processamento de pedidos físicos e digitais.

## Como executar no IntelliJ IDEA

1. Selecione **File → Open** e abra a pasta do projeto ou seu `pom.xml` como projeto Maven.
2. Use um **JDK 17 ou superior** e aguarde a importação das dependências.
3. Abra `src/main/java/br/com/atividade/pedidos/Main.java`.
4. Clique no botão verde ao lado de `main` e escolha **Run 'Main.main()'**. Também há uma configuração compartilhada chamada **Executar pedidos**.
5. Para rodar os testes, clique com o botão direito em `src/test/java` e selecione **Run 'All Tests'**. Outra opção é **Maven → Lifecycle → test**.

O programa funciona no console. E-mail, baixa de estoque e auditoria são simulações locais: não enviam mensagens nem alteram sistemas externos.

### Pelo terminal

Com Java 17 ou superior e Maven instalados:

```sh
mvn verify
java -jar target/mini-framework-pedidos-1.0.0.jar
```

## Organização

```text
br.com.atividade.pedidos
├── Main
├── modelo
│   ├── ItemPedido
│   ├── Pedido
│   └── ResultadoPedido
├── desconto
│   ├── CalculadoraDesconto
│   ├── DescontoClienteComum
│   └── DescontoClienteVip
├── observador
│   ├── ObservadorPedido
│   ├── NotificadorEmail
│   ├── BaixaEstoque
│   └── LogAuditoria
└── processador
    ├── ProcessadorPedido
    ├── ProcessadorPedidoFisico
    └── ProcessadorPedidoDigital
```

## Aplicação dos padrões

### Template Method

`ProcessadorPedido` é uma classe abstrata. Seu método `final processar(Pedido)` define a sequência:

```text
validar(pedido) → calcularTotal(pedido) → notificar(resultado)
```

`validar` e `calcularTotal` são abstratos. As subclasses implementam as regras específicas, mas não podem sobrescrever `processar` nem alterar a ordem das etapas. Se a validação falhar, uma exceção interrompe o fluxo antes do cálculo e das notificações.

O processador físico exige endereço de entrega e soma o frete ao valor dos itens após o desconto. O digital exige um e-mail válido, em uma verificação simples de formato, e não cobra frete. Ambos exigem ao menos um item.

### Strategy

`CalculadoraDesconto` define o cálculo do **valor do desconto**. A estratégia é recebida pelo construtor do processador e pode ser substituída por `definirCalculadoraDesconto`.

- `DescontoClienteComum`: sem desconto.
- `DescontoClienteVip`: desconto de 10% sobre o subtotal dos itens.

O desconto não incide sobre o frete. O processador depende da interface, permitindo incluir novas políticas de desconto sem mudar o algoritmo de processamento.

### Observer

`ObservadorPedido` recebe o resultado do processamento pelo método `aoProcessar`. O processador mantém a lista de interessados e permite registrá-los ou removê-los.

`notificar` percorre os observadores registrados e entrega a cada um o mesmo resultado já calculado. Os exemplos incluem e-mail, baixa de estoque e log de auditoria. Um pedido também pode ser processado sem observadores. O registro repetido da mesma instância é ignorado.

As notificações são síncronas. Para manter o exemplo simples, falhas lançadas por um observador são propagadas e interrompem os seguintes; não há fila, transação, persistência ou retentativa automática.

## Regras e exemplos

Valores monetários usam `BigDecimal`, com duas casas decimais e arredondamento `HALF_UP`. O preço unitário é normalizado antes de multiplicar pela quantidade. Quantidades devem ser positivas e preços não podem ser negativos. O resultado é separado do pedido original, evitando acumular desconto ou frete ao recalcular um pedido.

O `main` executa os seguintes cenários:

| Pedido | Subtotal | Desconto | Frete | Total | Observadores |
| --- | ---: | ---: | ---: | ---: | --- |
| Físico / comum | R$ 100,00 | R$ 0,00 | R$ 15,00 | R$ 115,00 | E-mail, estoque e auditoria |
| Físico / VIP | R$ 100,00 | R$ 10,00 | R$ 15,00 | R$ 105,00 | Estoque e auditoria |
| Digital / comum | R$ 80,00 | R$ 0,00 | R$ 0,00 | R$ 80,00 | E-mail |
| Digital / VIP | R$ 80,00 | R$ 8,00 | R$ 0,00 | R$ 72,00 | Auditoria |
| Digital / VIP, sem interessados | R$ 80,00 | R$ 8,00 | R$ 0,00 | R$ 72,00 | Nenhum |

Entre os exemplos há um pedido digital com e-mail inválido, cuja validação é tratada. A mesma instância de cada processador passa de cliente comum para VIP, com alteração dos observadores, sem mudar o esqueleto do algoritmo.

## Avaliação de padrões criacionais

Neste escopo, **não é necessário acrescentar um padrão criacional**. Há duas subclasses de processador e poucas dependências, cuja construção direta no `main`, com injeção por construtor, permanece simples e explícita.

Uma fábrica passaria a ser útil se a seleção do processador por tipo se repetisse em várias partes da aplicação. O padrão Factory Method faria sentido se subclasses de um criador precisassem decidir qual processador instanciar. Abstract Factory seria adequado caso surgissem famílias completas de componentes relacionados, como processadores e observadores específicos para diferentes canais de venda.

Builder só seria necessário se `Pedido` ganhasse muitos campos opcionais ou etapas complexas de construção. Singleton não foi usado: o processador mantém estratégia e observadores mutáveis, e compartilhá-lo globalmente poderia misturar configurações de pedidos distintos. Prototype também não é necessário, pois não há criação custosa nem configuração complexa a clonar.

## Testes

Os testes JUnit 5 verificam os totais, as estratégias, as regras de validação, o registro e a remoção de observadores, a ordem das etapas, o reprocessamento, o arredondamento e a cópia defensiva dos itens.

## Entrega

No GitHub, o equivalente ao merge request é o **pull request** da branch `atividade/processamento-pedidos` para `main`. As alterações da atividade ficam nessa branch para revisão.
