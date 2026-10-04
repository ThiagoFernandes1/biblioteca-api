/* =====================================================================
   Biblioteca: livros, membros e emprestimos (SQL Server)
   ===================================================================== */

CREATE TABLE dbo.livro (
    id              BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Livro PRIMARY KEY,
    isbn            NVARCHAR(13)  NOT NULL CONSTRAINT UQ_Livro_isbn UNIQUE,
    titulo          NVARCHAR(200) NOT NULL,
    autor           NVARCHAR(150) NOT NULL,
    ano_publicacao  INT           NULL,
    exemplares      INT           NOT NULL CONSTRAINT CK_Livro_exemplares CHECK (exemplares >= 0)
);

CREATE TABLE dbo.membro (
    id         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Membro PRIMARY KEY,
    nome       NVARCHAR(150) NOT NULL,
    email      NVARCHAR(200) NOT NULL CONSTRAINT UQ_Membro_email UNIQUE,
    ativo      BIT           NOT NULL CONSTRAINT DF_Membro_ativo DEFAULT 1,
    criado_em  DATETIME2(0)  NOT NULL CONSTRAINT DF_Membro_criado_em DEFAULT SYSDATETIME()
);

CREATE TABLE dbo.emprestimo (
    id               BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Emprestimo PRIMARY KEY,
    livro_id         BIGINT        NOT NULL CONSTRAINT FK_Emprestimo_Livro  REFERENCES dbo.livro(id),
    membro_id        BIGINT        NOT NULL CONSTRAINT FK_Emprestimo_Membro REFERENCES dbo.membro(id),
    data_emprestimo  DATE          NOT NULL,
    data_prevista    DATE          NOT NULL,
    data_devolucao   DATE          NULL,
    multa            DECIMAL(10,2) NOT NULL CONSTRAINT DF_Emprestimo_multa DEFAULT 0,

    CONSTRAINT CK_Emprestimo_prazo     CHECK (data_prevista >= data_emprestimo),
    CONSTRAINT CK_Emprestimo_devolucao CHECK (data_devolucao IS NULL OR data_devolucao >= data_emprestimo),
    CONSTRAINT CK_Emprestimo_multa     CHECK (multa >= 0)
);

/* Indices filtrados: so os emprestimos em aberto entram, que sao exatamente
   os que as checagens de disponibilidade e de limite consultam. */
CREATE INDEX IX_Emprestimo_livro_em_aberto
    ON dbo.emprestimo (livro_id) WHERE data_devolucao IS NULL;

CREATE INDEX IX_Emprestimo_membro_em_aberto
    ON dbo.emprestimo (membro_id, data_prevista) WHERE data_devolucao IS NULL;

CREATE INDEX IX_Emprestimo_membro_historico
    ON dbo.emprestimo (membro_id, data_emprestimo DESC);
GO

/* CREATE VIEW precisa ser o unico comando do lote, por isso o GO acima. */
CREATE VIEW dbo.vw_disponibilidade_livro AS
SELECT  l.id,
        l.isbn,
        l.titulo,
        l.exemplares,
        COUNT(e.id)                AS emprestados,
        l.exemplares - COUNT(e.id) AS disponiveis
FROM    dbo.livro l
LEFT JOIN dbo.emprestimo e
       ON e.livro_id = l.id
      AND e.data_devolucao IS NULL
GROUP BY l.id, l.isbn, l.titulo, l.exemplares;
GO
