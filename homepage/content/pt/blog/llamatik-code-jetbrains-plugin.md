---
title: "Apresentamos o Llamatik Code: um assistente de programação com IA, focado em privacidade, para IntelliJ e Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "O Llamatik Code é um assistente de programação com IA, local em primeiro lugar, para as IDEs baseadas em IntelliJ. Toda a inferência roda na sua máquina com modelos GGUF — sem nuvem, sem chaves de API e sem nenhum dado saindo do seu dispositivo."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Desde o primeiro dia, construímos o Llamatik como uma plataforma de IA privada e no dispositivo. Hoje levamos essa mesma filosofia para a sua IDE.

O **Llamatik Code** já está disponível no [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code) — um assistente de programação com IA totalmente local e focado em privacidade para o IntelliJ IDEA, o Android Studio e toda a família de IDEs da JetBrains.

Sem nuvem. Sem chaves de API. Nenhum dado saindo da sua máquina. Só o seu modelo GGUF local, rodando dentro da sua IDE.

---

## Por que IA local na sua IDE?

A maioria dos assistentes de programação com IA funciona do mesmo jeito: o seu código é enviado a um servidor remoto, um modelo na nuvem o processa e o resultado volta. Isso funciona — mas tem seus contrapontos:

- O seu código e o contexto saem da sua máquina a cada requisição
- Você depende de conexão de rede e da disponibilidade de terceiros
- Os custos de assinatura crescem com o uso
- Ambientes corporativos e regulados muitas vezes não podem usar IA na nuvem de forma alguma

O Llamatik Code adota outra abordagem. Tudo roda no dispositivo usando o [llama.cpp](https://github.com/ggerganov/llama.cpp) — o mesmo runtime, já testado em batalha, que move os apps Llamatik para celular e desktop. Você carrega um modelo GGUF uma única vez e, a partir daí, a sua IDE passa a ter um assistente de IA completo que nunca envia nada para fora.

---

## O que o Llamatik Code faz

### Ask Mode — converse sobre qualquer coisa, sem alterar arquivos

Faça perguntas, explore APIs, peça explicações, gere trechos de código. O Ask Mode é uma interface de conversa completa, com histórico persistente em várias sessões e ciente dos seus arquivos atuais, do texto selecionado e da estrutura do projeto.

O Ask Mode é **ilimitado no plano gratuito** — sem limite diário, sem créditos.

### Agent Mode — deixe o modelo conduzir as alterações de código

O Agent Mode vai além. Você descreve o que quer em linguagem natural, e o agente propõe edições de código, mostra um diff e espera a sua aprovação antes de tocar em qualquer arquivo.

O agente conhece o projeto: entende a estrutura de arquivos, consegue navegar entre eles e encadeia várias chamadas de ferramentas para concluir tarefas de várias etapas. O plano gratuito inclui tarefas de agente em sessão única. O Pro libera a execução paralela de várias tarefas e o **Aggressive Mode**, em que o agente pode tomar decisões autônomas sem pedir aprovação a cada etapa.

### Edição de vários arquivos (Pro)

Aplique alterações de código em vários arquivos de uma só vez. Útil para refatorações, migrações de API e qualquer coisa que envolva mais de um arquivo.

### Integração com servidores MCP

O Llamatik Code é compatível com o [Model Context Protocol](https://modelcontextprotocol.io/), permitindo conectar o agente a ferramentas e serviços externos. O plano gratuito aceita um servidor MCP com aprovação manual das ações. O Pro libera servidores MCP ilimitados e aprovação automática.

### External Knowledge — busca na web integrada

Desde a versão 1.5.0, o Llamatik Code inclui o sistema External Knowledge, que permite ao modelo pesquisar na web e ler documentação sem nenhuma chave de API.

Ele usa o DuckDuckGo por baixo dos panos e entende a intenção do desenvolvedor:

- Perguntas sobre Kotlin ou Coroutines → pesquisa no kotlinlang.org
- Perguntas sobre Android ou Compose → pesquisa no developer.android.com
- Mensagens de erro → pesquisa nas Issues do GitHub
- Perguntas sobre bibliotecas → pesquisa em registros de pacotes e changelogs

Cada resultado vem com citações estruturadas — tipo de fonte, nível de confiança e URL direta. O modelo planeja fluxos de pesquisa de várias etapas automaticamente e sintetiza as descobertas em uma resposta coerente.

O plano gratuito inclui 5 buscas por dia (5 resultados cada). O Pro eleva isso para 50 buscas por dia, com 10 resultados cada e até 10 fontes de documentação privadas.

### Code Health Monitor

Um modo de análise em segundo plano que aponta problemas de qualidade do código. Usuários do plano gratuito podem executá-lo manualmente; o Pro libera modos automatizados avançados.

---

## Gratuito vs. Pro

| Recurso | Gratuito | Pro |
|---|---|---|
| Ask Mode (chat) | Ilimitado | Ilimitado |
| Sessões de agente | Sessão única | Várias tarefas, em paralelo |
| Aggressive agent mode | — | ✓ |
| Aplicação de código em vários arquivos | — | ✓ |
| Servidores MCP | 1, aprovação manual | Ilimitados, aprovação automática |
| Buscas do External Knowledge por dia | 5 | 50 |
| Resultados por busca | 5 | 10 |
| Fontes de documentação privadas | 1 | 10 |
| Code Health Monitor | Manual | Automatizado avançado |

As assinaturas Pro estão disponíveis em [llamatik.com](https://www.llamatik.com).

---

## Primeiros passos

**Requisitos:**
- IntelliJ IDEA 2026.1 ou qualquer IDE da JetBrains na build de plataforma 252+
- macOS, Linux ou Windows (64 bits, JDK 21+)
- Um arquivo de modelo GGUF armazenado localmente

**Instalação:**
1. Abra a sua IDE → Settings → Plugins → Marketplace
2. Pesquise por **Llamatik Code** e instale
3. Reinicie a IDE

**Configuração do modelo:**
1. Vá em Settings → Llamatik Code → Inference
2. Aponte para o seu arquivo `.gguf` local
3. Ajuste o tamanho do contexto e o número de threads para o seu hardware

Qualquer modelo GGUF compatível com o llama.cpp funciona. Para a maioria das tarefas de desenvolvimento, um modelo instruct de 7B quantizado em Q4_K_M ou Q5_K_M é um bom ponto de partida. Para trabalho mais pesado com agentes ou bases de código complexas, um modelo de 13B–14B oferece um raciocínio visivelmente melhor.

As instruções completas de configuração e as recomendações de modelos estão na [documentação](https://docs.llamatik.com/llamatik-code/).

---

## Construído sobre o motor do Llamatik

O Llamatik Code é movido pela mesma integração do llama.cpp, pensada primeiro para Kotlin, que move os apps Llamatik para celular e desktop. Isso significa a mesma qualidade de inferência, a mesma compatibilidade com GGUF e as mesmas garantias de privacidade — seja conversando no celular ou editando código na sua IDE.

O acesso externo à rede só acontece por meio de servidores MCP configurados explicitamente ou de buscas do External Knowledge. Nada além disso sai da sua máquina.

---

## Instale hoje mesmo

O Llamatik Code já está disponível no JetBrains Marketplace.

→ [Instalar o Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Ler a documentação](https://docs.llamatik.com/llamatik-code/)
