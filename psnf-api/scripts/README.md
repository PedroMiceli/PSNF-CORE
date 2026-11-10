# Scripts para popular o banco

Os scripts usam a própria API: os dados passam pelas mesmas validações do sistema.
Pode rodá-los várias vezes: o que já existe é pulado. Rode a partir da raiz do repositório,
com a API no ar (e o MinIO, para as imagens: `docker compose up -d minio`).

| Script | O que faz |
|---|---|
| `popular-usuarios.py` | cria 12 empresas compradoras de teste, com telefone e endereço (senha `123456`) |
| `popular-anuncios.py` | cria 6 fornecedores e 175 anúncios nas categorias da plataforma (faixas de preço, variações e 2 imagens cada) |

```bash
python3 psnf-api/scripts/popular-usuarios.py
python3 psnf-api/scripts/popular-anuncios.py
```

Os usuários criados aparecem no seletor **"Logado como"** do front (lista vinda de `GET /dev/usuarios`,
endpoint só de desenvolvimento, ativo junto com os usuários mock).

As **categorias são fixas** e não são criadas por script nem por usuários: a API as carrega ao iniciar,
a partir de `psnf-api/src/main/resources/db/migration/R__categorias.sql` (migração repetível do Flyway). Para incluir uma categoria, adicione-a
nesse arquivo e reinicie a API.

Opções do `popular-anuncios.py`:

- `--imagens url`: não envia imagens ao MinIO; usa fotos de exemplo do picsum.photos
  (se o MinIO estiver fora do ar, o script já faz isso sozinho)
- `--limite N`: cria no máximo N anúncios (teste rápido)
- `--api URL` ou `API_URL=...`: endereço da API (padrão http://localhost:8080)

Os fornecedores são usuários comuns, com senha `123456`; os anúncios do Vendedor Mock aparecem em
"Meus anúncios". Os ids dos fornecedores criados ficam em `.fornecedores-cache.json` (ignorado pelo git).
