# Story Map: Loc Imóveis App

## Estrutura do mapa

O mapa está organizado em 3 linhas:

| **Linha** | **O que representa** |
|---|---|
| **Atividades (linha do topo)** | O que o usuário faz em alto nível (a grande jornada do usuário) |
| **Tarefas (linha do meio)** | As ações específicas dentro de cada atividade |
| **Histórias (linhas abaixo)** | As histórias de usuário, ordenadas por prioridade de cima para baixo |

## Atividades mapeadas

1. **Acessar o sistema:** Login e Logout.
2. **Gerenciar Cadastros:** Cadastrar imóveis, inquilinos e formalizar contratos de locação.
3. **Acompanhar Finanças:** Visualizar painel de controle (dashboard) com lucros e inadimplência.
4. **Gerenciar Recebimentos:** Confirmar pagamentos e gerar recibos em PDF.
5. **Gerenciar Manutenções:** Abertura, acompanhamento e resolução de chamados no imóvel.

## Definição do MVP

O MVP (Produto Mínimo Viável) do Loc Imóveis App cobre o fluxo essencial e crítico para que o proprietário (Locador) consiga organizar seus imóveis, registrar seus inquilinos, dar baixa em pagamentos e emitir recibos com segurança jurídica.

**Histórias incluídas no MVP:**

| **História** | **Descrição** |
|---|---|
| **HU001** | Login no sistema |
| **HU002** | Logout |
| **HU004** | Cadastrar novo imóvel |
| **HU005** | Cadastrar locatário (inquilino) |
| **HU006** | Criar contrato de locação |
| **HU003** | Visualizar Dashboard Financeiro |
| **HU007** | Confirmar recebimento de aluguel |
| **HU008** | Gerar e baixar recibo em PDF |

**Histórias fora do MVP (incremento seguinte):**

| **História** | **Descrição** |
|---|---|
| **HU009** | Abrir chamado de manutenção (Visão Inquilino) |
| **HU010** | Atualizar status do chamado (Visão Locador) |

## Critério de MVP

O MVP foi definido com base na seguinte pergunta:

> **"Qual é a funcionalidade mínima necessária para resolver a dor principal do locador (perda de tempo com planilhas e falta de profissionalismo na emissão de recibos)?"**

A resposta foi: O locador precisa conseguir entrar no sistema, ter seus imóveis e inquilinos registrados e, principalmente, conseguir cruzar o recebimento do aluguel com a geração automática do PDF. O módulo de comunicação para reparos e manutenções, embora importante, não é o núcleo financeiro e foi movido para o incremento seguinte.