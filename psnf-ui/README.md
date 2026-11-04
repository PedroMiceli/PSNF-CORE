# PSNF UI

Protótipo do front-end da PSNF em **React + TypeScript + Vite**, para testar a usabilidade da `psnf-api`.

## Como rodar

Pré-requisitos: **Node 22** (mínimo 20.19 — exigência do Vite 8; há um `.nvmrc`) e a `psnf-api` rodando em `http://localhost:8080` (com o Postgres).

```bash
cd psnf-ui
nvm use          # usa o Node do .nvmrc (22)
npm install
npm run dev
```

> **Erro `does not provide an export named 'styleText'`?** O Node está desatualizado (ex.: 18).
> Instale o 22 com `nvm install 22 && nvm use 22`, apague `node_modules` e rode `npm install` de novo.
> O `npm run dev` já verifica a versão antes de iniciar e avisa se ela não for suportada.

Abra http://localhost:5173.

O Vite repassa as chamadas `/api/*` para `http://localhost:8080` (veja `vite.config.ts`), então não é preciso configurar CORS na API.

## Login simulado

A API ainda não tem autenticação: o usuário "logado" vai no header `X-Usuario-Id`.
No canto superior direito, o seletor **Logado como** troca entre:

- **Vendedor Mock** (azul): cria anúncios, gera ordens, anexa NF, despacha.
- **Comprador Mock** (verde): vê a vitrine, paga, confirma a entrega.
- Usuários extras cadastrados em **Perfil → Cadastrar outro usuário**.

## Roteiro de teste sugerido

1. **Categorias**: cadastre uma categoria.
2. **Meus anúncios → Novo anúncio** (como vendedor): faixas de preço, imagens (URLs) e variações.
3. No anúncio, clique em **Gerar ordem de pedido**, escolha as variações e quantidades e ajuste o valor negociado.
4. Troque para **Comprador Mock** → **Ordens de pedido → Minhas compras** → abra a ordem e registre os pagamentos (PIX, boleto ou transferência).
5. Troque para o vendedor → anexe a **nota fiscal** → **Pedido despachado**.
6. Troque para o comprador → **Confirmar entrega**.
7. Use **Incidentes e mensagens** a qualquer momento.

## Estrutura

```
src/
  api/          client.ts (fetch + X-Usuario-Id + erros), endpoints.ts, types.ts, useApi.ts
  context/      UsuarioContext.tsx (usuário logado simulado)
  components/   Layout, componentes de UI e formatação
  pages/        Vitrine, AnuncioDetalhe, AnuncioForm, MeusAnuncios,
                NovaOrdem, Ordens, OrdemDetalhe, Categorias, Perfil
```

## Ainda não existe na API (aparece desabilitado ou omitido)

- Chat entre comprador e vendedor (botão "Iniciar chat").
- Upload de imagens e NF: hoje são apenas caminhos/URLs.
- Status mais detalhados das telas do Excalidraw (rascunho, aguardando aceite, em preparação...).
