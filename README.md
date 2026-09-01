# Loc Imóveis App

## Sobre o Loc Imóveis App

O **Loc Imóveis App** é um sistema desktop de gestão imobiliária focado em pequenos proprietários e seus inquilinos.

O problema central, o público-alvo e a solução detalhada do projeto estão descritos no [Documento de Visão](docs/visao.md).

**Detalhe:** O projeto foi dividido em dois repositórios, front-end e back-end, para facilitar o desenvolvimento e a separação de atividades.

**Equipe:** João Victor de Lima Araujo Gonçalves, Mateus Rodrigues Lima e Erivan Santos.

---

## Índice de Artefatos

### Fase 1: Descoberta

| **Artefato**       | **Arquivo**         |
| ------------------ | ------------------- |
| Documento de Visão | `docs/visao.md`     |
| Personas           | `docs/personas.md`  |
| Entrevistas        | `docs/entrevistas.md` |

### Fase 2: Definição

| **Artefato**           | **Arquivo**                     |
| ---------------------- | ------------------------------- |
| Histórias de Usuário   | `docs/historias.md`              |
| Story Map              | `docs/story-map.md`              |

### Fase 3: Decisão Técnica

| **Artefato**              | **Arquivo**                  |
| ------------------------- | ---------------------------- |
| ADR 001: Escolha de Stack | `docs/adr/escolha-de-stack.md` |
| ADR 002: Banco de Dados   | `docs/adr/banco-de-dados.md` |

### Fase 4: Backlog Final

| **Artefato**            | **Arquivo / Link** |
| ----------------------- | ------------------ |
| Definition of Ready     | `docs/definition-of-ready.md` |
| Definition of Done      | `docs/definition-of-done.md`  |
| GitHub Projects (board) | [Acessar board](https://github.com/users/jvictorADS/projects/1) |

---

## Estrutura do Repositório

```text
docs/
├── visao.md                       # Documento de Visão do produto
├── personas.md                    # Personas do projeto
├── entrevistas.md                 # Entrevistas com o cliente principal
├── historias.md                   # Histórias de usuário por épico
├── story-map.md                   # Link para o Story Map e definição do MVP
├── validacao-prototipo.md         # Resultado da validação do protótipo
├── definition-of-ready.md         # Critérios de Definition of Ready
├── definition-of-done.md          # Critérios de Definition of Done
└── adr/
    ├── 001-escolha-de-stack.md    # ADR sobre a stack tecnológica
    └── 002-banco-de-dados.md      # ADR sobre o banco de dados

README.md                          # Este arquivo
```

---

## GitHub Projects

O backlog do projeto é gerenciado no **GitHub Projects**:

[**Acessar board**](https://github.com/users/jvictorADS/projects/1)

O board utiliza os seguintes campos customizados para organizar as entregas:

| **Campo**  | **Tipo**      | **Valores**                                                                                              |
| ---------- | ------------- |----------------------------------------------------------------------------------------------------------|
| **Épico**  | Single select | Autenticação, Gestão de Cadastros, Dashboard e Visão Geral, Financeiro e Recibos, Manutenções e Chamados |
| **MoSCoW** | Single select | Must, Should, Could, Won't                                                                               |
| **Size**   | Single select | XS, S, M, L, XL                                                                                          |
| **Sprint** | Iteration     | Sprint 1, Sprint 2, Sprint 3, Sprint 4, + ...                                                            |
