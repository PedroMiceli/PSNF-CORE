# PSNF UI

Protótipo do front-end da PSNF em **React + TypeScript + Vite**, para testar a usabilidade da `psnf-api`.

## Como rodar

Pré-requisitos: **Node 22** (mínimo 20.19 — exigência do Vite 8; há um `.nvmrc`) e a `psnf-api` rodando em `http://localhost:8080` (com o Postgres).
Para o **upload de arquivos** (fotos, NF, anexos) suba também o MinIO, na raiz do repositório:

```bash
docker compose up -d minio
```

Console do MinIO: http://localhost:9001 (usuário e senha `minioadmin`).

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
2. **Meus anúncios → Novo anúncio** (como vendedor): faixas de preço, fotos (upload ou URL) e variações.
3. No anúncio, clique em **Gerar ordem de pedido**, escolha as variações e quantidades e ajuste o valor negociado.
4. Troque para **Comprador Mock** → **Ordens de pedido → Minhas compras** → abra a ordem e registre os pagamentos (PIX, boleto ou transferência).
5. Troque para o vendedor → anexe a **nota fiscal** (PDF/XML) → **Pedido despachado**.
6. Troque para o comprador → **Confirmar entrega**.
7. Use **Incidentes e mensagens** a qualquer momento (é possível anexar fotos).

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
- Status mais detalhados das telas do Excalidraw (rascunho, aguardando aceite, em preparação...).

## Upload de arquivos

Os arquivos vão para `POST /api/arquivos` (MinIO em desenvolvimento, S3 em produção) e a API devolve um
`caminho` (`/arquivos/{uuid}.{ext}`), que é o que fica salvo no anúncio, na NF, na mensagem ou no perfil.
O componente `UploadArquivo` cuida da seleção/arrastar-e-soltar e dos erros (tipo não suportado, tamanho, storage fora do ar).
Limites: imagens JPG/PNG/WEBP/GIF até 5 MB; PDF/XML até 10 MB (o tipo é detectado pelo conteúdo do arquivo).
