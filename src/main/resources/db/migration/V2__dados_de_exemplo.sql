/* Acervo e membros de exemplo para testar a API logo apos subir.
   Os ISBNs sao ficticios. */

INSERT INTO dbo.livro (isbn, titulo, autor, ano_publicacao, exemplares) VALUES
    (N'9780000000001', N'Dom Casmurro',                  N'Machado de Assis',       1899, 3),
    (N'9780000000002', N'Memórias Póstumas de Brás Cubas', N'Machado de Assis',     1881, 2),
    (N'9780000000003', N'O Cortiço',                     N'Aluísio Azevedo',        1890, 2),
    (N'9780000000004', N'Vidas Secas',                   N'Graciliano Ramos',       1938, 1),
    (N'9780000000005', N'Grande Sertão: Veredas',        N'João Guimarães Rosa',    1956, 1),
    (N'9780000000006', N'A Hora da Estrela',             N'Clarice Lispector',      1977, 2);

INSERT INTO dbo.membro (nome, email) VALUES
    (N'Ana Souza',     N'ana.souza@exemplo.com'),
    (N'Bruno Lima',    N'bruno.lima@exemplo.com'),
    (N'Carla Mendes',  N'carla.mendes@exemplo.com');
