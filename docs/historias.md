# Histórias de Usuário: Loc Imóveis App

As histórias estão organizadas por épico. Cada história possui critérios de aceitação que definem quando ela pode ser considerada concluída, baseando-se nos requisitos levantados na fase de Análise e Projeto.

---

## Autenticação

### HU001: Login no sistema

**Como** usuário cadastrado (Locador ou Locatário),  
**quero** fazer login com e-mail e senha,  
**para** acessar meu painel de controle no sistema.

#### Critérios de aceitação

- O sistema valida e-mail e senha utilizando autenticação JWT.
- Em caso de erro, exibe mensagem genérica de credenciais inválidas.
- Após login bem-sucedido, o Locador é redirecionado para o **Dashboard Financeiro**.
- O Locatário é redirecionado para a tela **"Meus Aluguéis/Recibos"**.

### HU002: Logout

**Como** usuário logado,  
**quero** fazer logout,  
**para** sair da minha conta com segurança e proteger meus dados financeiros.

#### Critérios de aceitação

- O botão de logout está acessível no menu lateral do sistema desktop.
- Após logout, o token JWT é invalidado.
- O usuário é redirecionado para a tela inicial de login.

---

## Dashboard e Visão Geral

### HU003: Visualizar Dashboard Financeiro

**Como** Locador,  
**quero** visualizar um painel resumo na tela inicial,  
**para** acompanhar rapidamente meus ganhos e quem está inadimplente.

#### Critérios de aceitação

- O painel exibe cards com:
    - Total Recebido no mês;
    - Gastos em Reformas;
    - Total Pendente;
    - Quantidade de Imóveis Alugados/Vagos.
- Há uma tabela com a lista destacada de inquilinos com pagamento em atraso.
- Os dados são carregados automaticamente ao abrir a tela.

---

## Gestão de Cadastros (Imóveis e Inquilinos)

### HU004: Cadastrar novo imóvel

**Como** Locador,  
**quero** cadastrar um novo imóvel,  
**para** mantê-lo no meu portfólio de administração.

#### Critérios de aceitação

- O formulário exige:
    - Título/Identificação;
    - Endereço completo;
    - Valor base sugerido para aluguel.
- O imóvel recém-criado recebe o status padrão **"Disponível"**.
- O sistema exibe uma mensagem de sucesso após o cadastro no banco de dados.

### HU005: Cadastrar locatário (inquilino)

**Como** Locador,  
**quero** cadastrar os dados de um novo inquilino,  
**para** vinculá-lo futuramente a um contrato.

#### Critérios de aceitação

- O formulário exige:
    - Nome completo;
    - CPF;
    - E-mail;
    - Telefone (WhatsApp).
- O CPF deve ser validado para evitar duplicidade.
- O sistema gera automaticamente um acesso inicial para o inquilino com base no e-mail.

### HU006: Criar contrato de locação

**Como** Locador,  
**quero** criar um contrato vinculando um inquilino a um imóvel,  
**para** iniciar o ciclo de cobranças mensais.

#### Critérios de aceitação

- O sistema permite selecionar um imóvel com status **"Disponível"** e um inquilino cadastrado.
- Deve-se definir:
    - Data de vencimento mensal;
    - Valor acordado;
    - Possíveis taxas referentes às cláusulas.
- Após salvar, o status do imóvel muda automaticamente para **"Alugado"**.

---

## Financeiro e Recibos

### HU007: Confirmar recebimento de aluguel

**Como** Locador,  
**quero** confirmar manualmente o recebimento de um aluguel,  
**para** dar baixa na cobrança daquele mês.

#### Critérios de aceitação

- O sistema exibe uma lista de cobranças pendentes.
- Ao clicar em **"Confirmar Pagamento"**, o status da cobrança muda para **"Pago"**.
- O sistema registra a data exata da baixa.

### HU008: Gerar e baixar recibo em PDF

**Como** Locador ou Locatário,  
**quero** gerar e baixar o recibo de um aluguel pago,  
**para** ter um comprovante com validade jurídica.

#### Critérios de aceitação

- O botão **"Baixar Recibo"** só fica disponível para cobranças com status **"Pago"**.
- O sistema gera o arquivo PDF em até **5 segundos**.
- O PDF contém:
    - Valor;
    - Dados do Locador;
    - Dados do Locatário;
    - Endereço do Imóvel;
    - Data do pagamento.

---

## Manutenções e Chamados

### HU009: Abrir chamado de manutenção

**Como** Locatário,  
**quero** abrir um chamado relatando um problema no imóvel,  
**para** formalizar o pedido de conserto ao proprietário.

#### Critérios de aceitação

- O formulário exige:
    - Título;
    - Descrição do problema.
- O chamado é criado com a data atual e o status **"Aberto"**.
- O Locador consegue visualizar o chamado na sua aba de **Manutenções**.

### HU010: Atualizar status do chamado

**Como** Locador,  
**quero** alterar o status de um chamado de manutenção,  
**para** manter o inquilino informado sobre o andamento do conserto.

#### Critérios de aceitação

- O sistema permite alterar o status para:
    - **"Em Andamento"**;
    - **"Resolvido"**.
- O Locatário consegue ver a mudança de status na sua tela de acompanhamento.