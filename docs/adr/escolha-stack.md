# ADR 001: Escolha de Stack Tecnológica

# Contexto

O grupo precisava definir as tecnologias fundamentais que serão usadas para construir o Loc Imóveis App. A escolha precisou levar em conta o conhecimento técnico da equipe, a viabilidade de entrega dentro do semestre e a restrição arquitetural de que a primeira versão operará como um sistema desktop.
## Decisão

Adotaremos a seguinte stack tecnológica:

| Camada | Tecnologia   | Versão (Aprox.) |
|---|--------------|-----------------|
| **Frontend (UI)** | React (VITE) | 8               |
| **Backend (API)** | Java + Spring Boot | 3.x / Java 21   |
| **Banco de Dados (Prod/Principal)** | PostgreSQL   | 16+             |
| **Banco de Dados (Dev/Testes)** | H2 Database  | -               |
| **Autenticação** | JWT (JSON Web Token) | -               |

---

## Alternativas Consideradas

### Frontend: React vs. Vue.js

- **React** foi escolhido por possuir um ecossistema gigantesco, vasta documentação e alta compatibilidade com ferramentas de empacotamento desktop, como **Electron** e **Tauri**.
- **Vue.js** foi considerado e até citado como opção no Guia 2, mas a equipe possui maior familiaridade e recursos prontos em React para acelerar a construção da interface.

### Backend: Spring Boot vs. Node.js

- **Spring Boot (Java)** foi escolhido por ser o forte da equipe. João Victor e Erivan são focados em **Back-end Java** e a tecnologia oferece segurança, estrutura robusta e excelentes bibliotecas para a geração de relatórios em PDF, requisito crítico do projeto.
- **Node.js** foi descartado para não dividir o foco tecnológico da equipe no back-end.

### Banco de Dados: PostgreSQL + H2 vs. MySQL

- **PostgreSQL** foi o banco relacional definido desde o Guia 2 por sua robustez, integridade de dados e conformidade com os padrões de mercado.
- O **H2 Database** foi adicionado à stack para rodar em memória durante o desenvolvimento e a execução de testes automatizados, permitindo maior agilidade sem precisar subir um banco robusto a todo instante.
- **MySQL** foi descartado para evitar mudança na arquitetura já validada no Projeto 1.
---

## Consequências

### Positivas

- **Alinhamento de Habilidades:** A stack extrai o melhor do perfil técnico da equipe, reduzindo o risco de atrasos por curva de aprendizado.
- **Produtividade no Back-end:** O uso do H2 em conjunto com o Spring Boot acelera significativamente os testes locais.
- **Reaproveitamento (Web/Desktop):** Desenvolver a interface em React encapsulada para desktop garante que o front-end já nasça preparado para a web quando o projeto expandir.

### Negativas / Riscos

- **Complexidade de Distribuição:** Empacotar um sistema desktop que dependa de um servidor Spring Boot rodando por trás, ou localmente, e uma interface React exige um fluxo de build mais complexo.

---

## Revisão

Esta decisão será revisada apenas se houver **impedimento técnico grave**, como dificuldades extremas de empacotar o Spring Boot junto ao Electron para rodar offline no cliente.

Pequenos ajustes de versão de bibliotecas **não justificam a troca estrutural da stack**.