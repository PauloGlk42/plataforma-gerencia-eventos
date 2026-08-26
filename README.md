## Credenciais de demonstração (seed)

Os quatro usuários semeados usam a mesma senha

| Papel       | Login                    | Senha      
| Organizador | organizador@evento.com   | senha123   
| Cliente     | cliente1@evento.com      | senha123   
| Cliente     | cliente2@evento.com      | senha123  
| Portaria    | portaria@evento.com      | senha123   

Login: `POST /auth/login` com `{ "login": "...", "password": "senha123" }`,
retorna `{ "token": "..." }` (JWT, usar como `Authorization: Bearer <token>`).

## Pagamento simulado

`POST /api/pedidos/{id}/pagamento`, com o cliente dono do pedido autenticado. Não existe
gateway real: o resultado é determinístico pelo final do número do cartão, para o
avaliador conseguir exercitar os dois caminhos sem depender de nenhum serviço externo.

| Final do cartão | Resultado                     |
| `0000`          | Recusado — saldo insuficiente |
| `1111`          | Recusado — suspeita de fraude |
| Qualquer outro  | Aprovado 

Toda tentativa gera uma linha em `pagamento` (aprovado ou recusado, com o motivo). Recusa
**não cancela** o pedido: ele continua `PENDENTE` até o prazo da reserva vencer, e o
cliente pode tentar outro cartão. Aprovação muda o pedido para `PAGO` e emite os
ingressos na mesma transação; pagar de novo um pedido já `PAGO` é rejeitado (400), sem
emitir ingresso duplicado.

## Ingresso e QR

Para permitir o compartilhamento unitário dos ingressos, quando um lote é comprado, o usuário tem acesso a unidade, com `codigo` no formato
`<identificador>.<assinatura>` — uma assinatura HMAC-SHA256 do identificador, em
base64url, calculada com a chave `INGRESSO_SECRET` (própria, separada do `JWT_SECRET`).
A imagem do QR é responsabilidade do frontend; a API só devolve essa string.

- `GET /api/meus-ingressos` — ingressos do cliente autenticado, agrupados por evento.
- `POST /api/ingressos/{id}/compartilhar` — devolve o `token_publico` do ingresso (dono).
- `GET /api/publico/ingressos/{token}` — sem autenticação; devolve evento, data, local,
  setor e o código do QR, e **nenhum dado do comprador**. `token_publico` é um UUID
  separado do `codigo`: a URL compartilhada não é, ela mesma, o payload de validação.

A verificação de assinatura acontece antes de qualquer consulta ao banco — um código
forjado é rejeitado sem custo de I/O.

### Mais detalhes da aplicação
- Controllers manipulando DTOs.
- Services com as regras de negócio e @Transactional
- Repository implementando consulta e escrita
- Domain contendo entidades JPA, enums e dtos
- Infra implementando segurança, tratamento de erro, cache, agendamento...

- Criação de tabelas usando o Flyway migrations
- Um contador único. setor.ocupados é a fonte da verdade do estoque, incrementado já na reserva. para não permitir venda de ingressos a mais.
- Constraint no banco como rede de segurança: CHECK (ocupados >= 0 AND ocupados <= capacidade)
- Liberação idempotente. Devolver estoque só acontece se a transição de status do pedido afetar exatamente uma linha
- Foi implementada também, uma chave de assinatura para o QRCode. "<identificador aleatório>.<HMAC-SHA256 do identificador, em base64url>"
- O qrcode usa svg, foi uma alternativa encontrada para garantir a nitidez independente da escala
- Como comentado em ideias.md, busca de eventos é flexível (só filtra pelo que você preencher no front-end),ignora maiúsculas/minúsculas e de ótimo desempenho no PostgreSQL (pois usa um índice especial de trigramas para acelerar a busca por partes do texto)
-Para o mapa de ocupação, foi feita a união dados de ocupação do banco à geometria SVG do front-end usando o slug como contrato, sem precisar guardar coordenadas no banco de dados. Essa estrutura permite carregamento leve, suporte nativo a dark mode e texturas para setores esgotados.
-A ideia foi popular os shows e filmes com dados diretamente do banco e quando o organizador vai criar o evento, daí sim puxa alguns da api(tipo um catálogo), que se forem usados, aparecerão para o cliente na tela principal.
- liberarReservasVencidas, task com anotação: @Scheduled(fixedDelay = 30_000), sendo a responsável por devolver reservas com status pendente já vencidas.

### Uso de IA
- Usei o claude. Primeiro escrevi o contexto do projeto e passei cada decisão que havia tomado sobre a estrutura, depois, dei acesso a pasta do projeto. Após isso, gerei um markdown de contexto que pudesse ser consultado para execução das tarefas. Pedi para manter a estrutura do projeto como repositorios, servicos, a parte de autenticação... e fui fazendo os requisitos aos poucos, em branches separadas, as quais testava e depois fazia merge. 

### O que não foi implementado
- Cancelamento com devolução ao estoque
- Mapa de assentos

### Stack
- React + vite
- Spring boot + postgresql

links:
https://plataforma-gerencia-eventos.vercel.app/

https://railway.com/project/ef72d828-63f4-4791-bed0-d6e928331d80/service/fc0030e1-1191-4ce4-86be-800fbbe5227d/variables?environmentId=ea5d58c7-88bb-43d7-b683-adcdb3a5fcad

https://github.com/PauloGlk42/plataforma-gerencia-eventos.git