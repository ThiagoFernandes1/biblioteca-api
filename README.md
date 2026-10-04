# 📚 Biblioteca API — Spring Boot + SQL Server

API REST para os empréstimos de uma biblioteca: acervo, membros, empréstimos e devoluções com multa por atraso. As regras ficam na aplicação, mas o banco também se protege, com restrições `CHECK`, índices filtrados e uma view que calcula a disponibilidade de cada livro.

![CI](https://github.com/ThiagoFernandes1/biblioteca-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2022-CC2927?style=flat-square&logo=microsoftsqlserver&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=flat-square&logo=flyway&logoColor=white)

---

## Regras de empréstimo

| Regra | Padrão | Onde muda |
|---|---|---|
| Prazo de devolução | 14 dias | `biblioteca.prazo-dias` |
| Empréstimos em aberto por membro | 3 | `biblioteca.limite-emprestimos` |
| Multa por dia de atraso | R$ 2,00 | `biblioteca.multa-por-dia` |

Além disso:

- membro com algum empréstimo **atrasado** não pega outro livro até devolver;
- membro **desativado** não pega livros (desativar preserva o histórico, excluir não);
- um livro só é emprestado se ainda houver **exemplar livre**;
- não dá para reduzir o número de exemplares abaixo dos que estão emprestados.

### Concorrência

Duas pessoas pedindo o último exemplar ao mesmo tempo não podem levar as duas. O empréstimo lê o membro e o livro com `PESSIMISTIC_WRITE` (no SQL Server, `UPDLOCK`), sempre nessa ordem, então a segunda requisição espera a primeira terminar e só então confere a disponibilidade.

---

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| `GET` | `/api/livros?busca=` | Lista o acervo, com busca por título ou autor e paginação |
| `GET` | `/api/livros/{id}` | Detalhe do livro |
| `GET` | `/api/livros/disponibilidade` | Exemplares, emprestados e disponíveis (lido da view) |
| `POST` | `/api/livros` | Cadastra um livro |
| `PUT` | `/api/livros/{id}` | Atualiza um livro |
| `GET` | `/api/membros` | Lista os membros |
| `POST` | `/api/membros` | Cadastra um membro |
| `PATCH` | `/api/membros/{id}/desativar` | Desativa um membro |
| `GET` | `/api/membros/{id}/emprestimos` | Histórico de empréstimos do membro |
| `POST` | `/api/emprestimos` | Empresta um livro: `{"livroId": 1, "membroId": 1}` |
| `POST` | `/api/emprestimos/{id}/devolucao` | Registra a devolução e calcula a multa |
| `GET` | `/api/emprestimos/atrasados` | Empréstimos em aberto e fora do prazo |

A documentação interativa fica em `http://localhost:8080/swagger-ui.html`.

Os erros seguem o formato [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`):

| Status | Quando |
|---|---|
| `400` | Corpo inválido; o campo `campos` diz o que corrigir |
| `404` | Livro, membro ou empréstimo inexistente |
| `409` | ISBN ou e-mail já cadastrado |
| `422` | Pedido válido que as regras não permitem (sem exemplar, limite atingido, atraso…) |

---

## O banco

O schema é versionado com Flyway em [`src/main/resources/db/migration`](src/main/resources/db/migration), e o Hibernate roda com `ddl-auto=validate`: se uma entidade divergir do banco, a aplicação nem sobe.

- **Índices filtrados** (`WHERE data_devolucao IS NULL`): só os empréstimos em aberto entram no índice, que são justamente os que as checagens de disponibilidade e de limite consultam.
- **Restrições `CHECK`**: exemplares não negativos, devolução nunca antes do empréstimo, multa nunca negativa.
- **View `vw_disponibilidade_livro`**: faz a conta de disponíveis no próprio banco.
- **`NVARCHAR`** em todas as colunas de texto, para os acentos não dependerem da collation.

---

## Como rodar

Precisa de Java 21, Maven e Docker.

```bash
# sobe o SQL Server 2022 e cria o banco BibliotecaDB
docker compose up -d

# sobe a API em http://localhost:8080 (o Flyway cria as tabelas e os dados de exemplo)
DB_PASSWORD=Biblioteca@2026 mvn spring-boot:run
```

No PowerShell, troque a segunda linha por:

```powershell
$env:DB_PASSWORD = "Biblioteca@2026"; mvn spring-boot:run
```

Para usar outro SQL Server, defina `DB_URL`, `DB_USER` e `DB_PASSWORD`.

---

## Testes

```bash
mvn test
```

- **`EmprestimoServiceTest`**: as regras de empréstimo e devolução, com um relógio fixo para controlar prazo e multa.
- **`EmprestimoControllerTest`**: como cada erro vira status HTTP.
- **`FluxoSqlServerTest`**: o fluxo completo contra um SQL Server de verdade (migrations, validação do schema, view, empréstimo, devolução). Roda só quando `DB_URL` está definida, como no CI:

```bash
DB_URL="jdbc:sqlserver://localhost:1433;databaseName=BibliotecaDB;encrypt=true;trustServerCertificate=true" \
DB_PASSWORD=Biblioteca@2026 mvn test
```

O [CI](.github/workflows/ci.yml) sobe um SQL Server 2022 a cada push e roda todos os testes contra ele.

---

## Estrutura

```
src/main/java/com/thiago/biblioteca/
├── config/        # regras configuráveis e relógio injetável
├── domain/        # entidades JPA: Livro, Membro, Emprestimo
├── repository/    # Spring Data, incluindo as leituras com bloqueio e a view
├── service/       # regras de negócio e exceções
└── web/           # controllers, DTOs e tratamento de erros
```
