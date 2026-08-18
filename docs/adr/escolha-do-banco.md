# ADR 002: Escolha do Banco de Dados

## Contexto

Com a stack de backend definida (**Spring Boot + Java**, conforme *ADR 001*), precisávamos escolher a tecnologia de banco de dados apropriada.

O **Loc Imóveis App** é um sistema que lida com dados críticos e exige:

- Forte consistência de dados, pois gerencia informações financeiras, como cobranças, recebimentos e recibos.
- Dados altamente estruturados com relacionamentos claros entre **Locadores, Imóveis, Inquilinos, Contratos, Cobranças e Chamados**.
- Volume de dados inicial moderado, considerando o foco em pequenos proprietários, sem necessidade de escala horizontal no curto prazo.

---

## Decisão

Usaremos o **PostgreSQL 15+** como banco de dados relacional principal para o ambiente de produção.

Para os ambientes de desenvolvimento local e testes automatizados, utilizaremos o **H2 Database** em memória.

O acesso ao banco será feito via **Spring Data JPA + Hibernate**, com migrações de esquema (*schema*) gerenciadas pelo **Flyway**.

---

## Alternativas Consideradas

### PostgreSQL vs. MySQL

- **PostgreSQL** foi escolhido por sua robustez, rigor na integridade de dados, essencial para dados financeiros, excelente suporte a transações complexas e por ser a escolha definida desde a fase de Análise (**Guia 2**).
- **MySQL** foi descartado, pois, embora seja excelente, não oferecia nenhuma vantagem técnica decisiva sobre o PostgreSQL que justificasse a mudança da arquitetura já desenhada.

### PostgreSQL vs. MongoDB

- **MongoDB**, ou outros bancos NoSQL, foi descartado imediatamente.
- O domínio do **Loc Imóveis App** é estritamente relacional: um **Contrato** une um **Imóvel** a um **Inquilino** e gera **N Cobranças**.
- Modelar dados financeiros e contratuais em um banco orientado a documentos aumentaria drasticamente o risco de inconsistência de dados, como atualizar o valor de um aluguel sem refletir nas cobranças, além de aumentar a complexidade da lógica de negócio para simular o comportamento **ACID**.

---

## Modelo de Dados Resumido

```text
users (locadores e locatários)
  id, email, password_hash, role, created_at

imoveis
  id, titulo, endereco, valor_base, status, locador_id (FK)

inquilinos
  id, nome, cpf, email, telefone, locador_id (FK)

contratos
  id, imovel_id (FK), inquilino_id (FK), valor_acordado, dia_vencimento, data_inicio, status

cobrancas
  id, contrato_id (FK), valor, vencimento, data_pagamento, status

chamados
  id, contrato_id (FK), titulo, descricao, status, created_at
```

---

## Consequências

### Positivas

- O modelo relacional com PostgreSQL garante propriedades **ACID**, fundamentais para a segurança jurídica e financeira do sistema, evitando a perda ou inconsistência do registro de um pagamento.
- A combinação **JPA + Hibernate** reduz o código *boilerplate*, acelerando a construção de CRUDs básicos, como Imóveis e Inquilinos.
- O uso do **H2 em memória** permite que o desenvolvimento da versão desktop ocorra de forma rápida, sem a necessidade de manter um container Docker do PostgreSQL rodando constantemente na máquina do desenvolvedor.

### Negativas / Riscos

- A geração de relatórios e os cálculos do Dashboard, como somar todos os ganhos e inadimplentes, podem gerar *queries* lentas e onerosas caso o proprietário tenha um volume muito grande de propriedades e um histórico longo.

#### Mitigação

Criar índices nas colunas `status` e `vencimento` da tabela `cobrancas` desde a primeira *sprint*.

---

## Revisão

Esta decisão será revisada apenas se o projeto evoluir de uma ferramenta para pequenos proprietários para um **SaaS de grande porte**, com foco em grandes imobiliárias.

Nesse cenário, poderia ser necessária uma arquitetura **multi-tenant**, utilizando bancos ou schemas isolados por cliente, além de abordagens diferentes para armazenamento de arquivos, como o **Amazon S3** para os recibos em PDF, em vez de gerá-los *on-the-fly*.