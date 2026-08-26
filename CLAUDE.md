# Regras do projeto

- Comentários e mensagens de commit sempre em português.
- Nunca mencione assistentes de IA em código, comentários ou commits.
- Comentários apenas onde explicam uma decisão não óbvia.
- As decisões de arquitetura estão em docs/CONTEXTO-PROJETO.md — leia antes
  de propor mudanças estruturais e não revisite o que está marcado [DECIDIDO].
- Não crie nem troque de branch sem eu pedir.

## Contexto
As decisões de arquitetura estão em `docs/CONTEXTO-PROJETO.md`. Leia antes de
propor qualquer mudança estrutural. Itens marcados [DECIDIDO] não devem ser
revisitados; itens [EM ABERTO] devem ser trazidos para discussão, não decididos
sozinho.

## Autoria
- Nunca mencione assistentes de IA em código, comentários, documentação ou
  mensagens de commit.
- Não adicione trailers de co-autoria.

## Código
- Comentários e mensagens de commit em português.
- Comente apenas onde o comentário explica uma decisão não óbvia. Não comente
  o que o código já diz.
- Siga a estrutura de pacotes e o estilo já existentes no projeto.
- Controller não tem regra de negócio. Service tem @Transactional e a regra.
  Repository só faz query.
- Somente o BookingService altera setor.ocupados.

## Git
- Não crie nem troque de branch sem eu pedir.
- Commits pequenos e separados por assunto, nunca um commit único no fim.
- Nunca commite arquivos .env ou credenciais.

## Escopo
Implemente apenas o que foi pedido. Se identificar algo fora do escopo que
pareça necessário, avise no final em vez de implementar.
EOF