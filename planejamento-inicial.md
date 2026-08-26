# Dia 18/08 - Recebimento do PDF com a proposta do desafio - Primeiro dia

Estou escrevendo este markdown aqui para registrar as primeiras impressões, ideias, metas e planejamento do projeto.

Objetivo: Criar uma plataforma de eventos. Um gerente publica o evento e os clientes podem comprar o ingresso.

Mais detalhes: 
Ao montar o evento, o organizador tem acesso a um catálogo de shows ou filmes, que vem de uma API externa (Ticketmaster Discovery ou TMDb?). Ele define, então, qual o evento, a data, o local, a capacidade e o preço.
O cliente tem acesso a uma tela com os eventos publicados, reserva seu lugar, paga (simulação), recebe o ingresso com código QR e pode compartilhá-lo via link, ingresso esse que deve ser validado.

Para o compartilhamento do link, deve ser uma página aberta que apenas exibe o ingresso e informações básicas não pessoais. 
Lido e validado uma vez, um ingresso tem que ser desativado imediatamente. Além disso, tem que ter uma chave única.

### Requisitos Funcionais Frontend (Cliente):
- Uma tela principal que dá para navegar entre filmes e shows ativos, filtrando por data, local e preço.
- Em caso de cinema/teatro, selecionar o lugar ou a quantidade de ingressos. Interessante ter um tipo de ingresso, como pista, camarote.
- Pagamento simulado com confirmação e recusa.
- Área de meus ingressos: dados e QR Code, botão de compartilhar.
- Poder cancelar o ingresso.

### Requisitos Funcionais Frontend (Gerência):
- Criação e gerenciamento dos eventos pelo organizador, isto é, trazer para uma tela de Eventos, por exemplo, os eventos criados pelo mesmo.
- Aparecer para cada evento um feedback visual da ocupação do evento.
- Tela de portaria para validar os ingressos, podendo ser: válido, inválido, já utilizado ou evento errado.
- Leitura do QR Code na portaria, bem como colocar um código do ingresso. Biblioteca JS para habilitar a câmera e detectar o QR Code. Implica num login diferente, né?

### Requisitos Funcionais Backend (Gerência):
- Gestão das chamadas de API. Validar retorno e otimizar o número de chamadas, se possível.
- Login pode ser feito com 3 roles: Organizador, Cliente e Portaria. Cada um tendo acesso às suas informações exclusivamente.
- Armazenamento das reservas, ingressos e eventos.
- Um lugar não pode ser vendido duas vezes. Lidar com a race condition usando algum tipo de lock e/ou operação atômica.
- Dificultar ao máximo a forja de um QR Code.
- Compartilhamento do QR Code via link. Então, provavelmente, seria uma página aberta, sem validação.
- Validou o ingresso na portaria, finaliza ele para evitar fraudes na entrada.
- devolução ao estoque em caso de cancelamento.

Se basear em plataformas reais (tem no PDF), pegar o melhor de cada uma, evitando um frontend padrão demais.

### Tecnologias Obrigatórias:
- Front: React com ou sem framework.
- Back: NodeJS, Python ou Java. Fique à vontade com o framework.
- Banco de Dados: Utilize qualquer distribuição. Certifique-se de incluir no README...

### Quais ferramentas usar e por quê?
- Front: React + Vite sem framework.
- Back: Django ou Spring? Vou optar pelo Spring pela familiaridade.
- QR Code: biblioteca JS. Usei num projeto que validava notas fiscais de mercado e salvava os produtos obtidos por web scraping diretamente no banco de dados.
- Banco: relacional. Apesar de estar mais familiarizado com SQL Server, vou optar pelo PostgreSQL para sair das raízes Microsoft um pouco. Creio que faça mais sentido devido ao ecossistema em si.
- Creio que essa combinação faça sentido, seja interessante de trabalhar e tranquila de integrar tudo.

### Ordem de Execução:
- Operações básicas para evento, usuário e definição de roles.

### Ideia Geral:

A princípio, daria para começar com a ideia de que existe uma tela inicial que puxa diferentes eventos de acordo com a role (Organizador/Cliente). O organizador consegue visualizar rapidamente os dados básicos do evento, como num card, e ao clicar, aparece o feedback visual da ocupação. Meio complicado, mas alguma opção de edição também pode aparecer. Por outro lado, o cliente consegue, nessa tela inicial, ver os eventos disponíveis, bem como clicar neles, ação a qual redireciona para uma visualização daquele evento em específico, podendo selecionar o/os assentos e finalizar o pagamento. Tendo essas ações e registros, daria para partir para a tela de meus eventos, por exemplo, e para a parte do código responsável por gerar o QR Code. Depois, processamento e "validação" do pagamento, tela de portaria e validar os fluxos/possíveis falhas.

### Requisitos Não Funcionais
- Gerenciar branches de forma segura e precisa.
- Gerar logs e histórico pode ser legal.
- Terminar até o dia 25.
- README detalhado explicando o passo a passo para configurar e executar a aplicação.
- Dados de teste: "Deixe semeados um organizador, dois clientes, um usuário de portaria e ao menos um evento publicado com ingressos disponíveis, para que possamos percorrer o fluxo sem montar tudo do zero."
- Deploy: muito interessante. Torna o projeto completinho e ainda ganha mais pontos.

### Objetivo do dia 19/08:
- Esquemático do software para melhor compreensão dos fluxos.
- Diagrama de classes do banco.
- Inicializar o back com banco e front.
- Qual modelo de organização seguir?
- Definir conceitualmente qual o núcleo do software, que deve estar funcionando 100% antes de implementar os requisitos e incrementar com ideias, por exemplo.

### Resumo do dia 19/08: 
- Pensando em coisas como deploy, que queria muito fazer, acabei optando por react + vite devido à compatibilidade com a Vercel. Para o backend, que decidi fazer em Java seguindo o modelo organizacional de camadas, e para banco PostgreSQL, por questões de entidades relacionadas, consistencia(Unique para registros entre evento e assento ou check para quantidade de ingressos), compatibilidade e liberdade para uso. Seria interessante enviar ao Railway por questões de  compatibilidade.

Acabei fazendo na mão as inicializações e configurações iniciais do backend. Comecei pela autenticação por questão das roles, vendo tutoriais, documentação ou consultando IAs para ver as políticas de segurança aplicáveis no projeto. Com JWT e gerenciamento stateless e algumas classes, dei início a essa parte do projeto. Frontend e partes teóricas não deram tempo de fazer hoje.

### Objetivo do dia 20/08:
- Esquemático do software para melhor compreensão dos fluxos.
- Diagrama de classes do banco.
- Inicializar o frontend.
- Definir conceitualmente qual o núcleo do software, que deve estar funcionando 100% antes de implementar os requisitos e incrementar com ideias, por exemplo.
- Implementar algumas das principais funcionalidades do backend.

### Resumo do dias 20/08
- Esboço do banco feito, apenas para evitar grandes retrabalhos depois e entender melhor o fluxo interno mesmo
- Frontend inicializado com uma tela de registro
- Feito um markdown para fixar melhor o escopo pensando na parte de banco de dados. 
- Devido estar acompanhando uma atualização de um sistema de armazém de uma empresa presencialmente, tenho a parte da noite apenas, então terei que continuar as implementações amanhã e focar em ter o núcleo: conseguir criar um evento como organizar e comprar um ingresso como cliente.
- Analisei também as plataformas sugeridas para ter ideias do frontend, pois as cores que escolhi para a tela de registro não ficaram boas

### Objetivo do dia 21/08:
- Implementar as principais Entidades, controller, servicos e repositórios.
- Definir um estilo para as telas bem como suas funções.
- Migrations(flyway)
- Swagger

### Resumo do dia 21/08:
- Não tive tanto tempo quanto gostaria e vou ter que trabalhar no sábado. Então foi feito um repasse em algumas tecnicas para tentar afinar a arquitetura do software, inicio de configurações para auxilio da IA, criação das principais Entidades JPA, enums e suas repotorios, seed de alguns dados usando usandos as novas migrations
- Definir um estilo para as telas bem como suas funções.

### Objetivo do dia 22/08:
- Frontend para o que foi implementado anteriormente

### Objetivo do dia 22/08:
- Finalizar backend 

### Objetivo do dia 23/08:
- Finalizar frontend  

### Objetivo do dia 24/08:
- Deploy e testes.

### Resumo dos dias: 22, 23 e 24:
- Como estou acompanhando presencialmente uma troca de sistema numa empresa de outra cidade, o tempo que tive para trabalhar no projeto foi bem variável, então fui desenvolvendo o back e front paralelamente, tendo o deploy configurado desde sábado. Achei mais seguro e eficiente assim.

### Resumo do último dia
- Aproveitei o último dia para implementar mais algumas coisas. No fim, a melhor escolha pelo que vejo seria ter começado a desenvolver o projeto desde o primeiro dia do recebimento, não pude dedicar tanto tempo quanto gostaria mas dei meu máximo.