# Scripts para popular o banco

Os scripts usam a própria API: os dados passam pelas mesmas validações do sistema.
Pode rodá-los várias vezes: o que já existe é pulado. Rode a partir da raiz do repositório,
com a API no ar (e o MinIO, para as imagens: `docker compose up -d minio`).

| Script | O que faz |
|---|---|
| `popular-usuarios.py` | cria 12 empresas compradoras de teste, com telefone e endereço (senha `teste123`) |
| `popular-anuncios.py` | cria 6 fornecedores e 175 anúncios nas categorias da plataforma (faixas de preço, variações e 2 imagens cada) |

```bash
python3 psnf-api/scripts/popular-usuarios.py
python3 psnf-api/scripts/popular-anuncios.py
```

Os scripts entram com o login de cada usuário (`POST /auth/token`) e mandam o token no
header `Authorization: Bearer`. Para usar o front, entre com qualquer um desses e-mails e a senha `teste123`
(criados antes da regra de 8 caracteres: `123456`), ou com `vendedor@mock.psnf` / `comprador@mock.psnf` (senha `123456`). Se a API responder 429 (rate limit), o script espera o
tempo do `Retry-After` e tenta de novo.

As **categorias são fixas** e não são criadas por script nem por usuários: a API as carrega ao iniciar,
a partir de `psnf-api/src/main/resources/db/migration/R__categorias.sql` (migração repetível do Flyway). Para incluir uma categoria, adicione-a
nesse arquivo e reinicie a API.

Opções do `popular-anuncios.py`:

- `--imagens url`: não envia imagens ao MinIO; usa fotos de exemplo do picsum.photos
  (se o MinIO estiver fora do ar, o script já faz isso sozinho)
- `--limite N`: cria no máximo N anúncios (teste rápido)
- `--api URL` ou `API_URL=...`: endereço da API (padrão http://localhost:8080)

Os fornecedores são usuários comuns, com senha `teste123` e CNPJ de teste (cerca de 1 em cada 3 anúncios novos
fica liberado para pessoa física; o resto é vendido só para quem tem CNPJ); os anúncios do Vendedor Mock aparecem em
"Meus anúncios".

**CNPJs de teste**: os scripts geram CNPJs com dígitos verificadores válidos e conferem na Receita
(`GET /cnpj`, BrasilAPI) que **não pertencem a nenhuma empresa**: se o número existir, tentam o próximo.
Contas criadas por versões antigas, com CNPJs `9000...` (alguns eram de empresas reais), são corrigidas ao
rodar os scripts de novo. Os usuários mock usam `00.000.001/0999-14` (vendedor) e `00.000.002/0999-69`
(comprador). Por não estarem na base pública, esses CNPJs aparecem como "não verificados".

