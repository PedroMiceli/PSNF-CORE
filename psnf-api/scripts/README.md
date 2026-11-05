# Scripts para popular o banco

Os scripts usam a própria API: os dados passam pelas mesmas validações do sistema.
Pode rodá-los várias vezes: o que já existe é pulado. Rode a partir da raiz do repositório,
com a API no ar (e o MinIO, para as imagens: `docker compose up -d minio`).

| Script | O que faz |
|---|---|
| `popular-categorias.sh` | cadastra 34 categorias |
| `popular-anuncios.py` | cria as categorias que faltarem, 6 fornecedores e 175 anúncios (faixas de preço, variações e 2 imagens cada) |

```bash
./psnf-api/scripts/popular-categorias.sh
python3 psnf-api/scripts/popular-anuncios.py
```

Opções do `popular-anuncios.py`:

- `--imagens url`: não envia imagens ao MinIO; usa fotos de exemplo do picsum.photos
  (se o MinIO estiver fora do ar, o script já faz isso sozinho)
- `--limite N`: cria no máximo N anúncios (teste rápido)
- `--api URL` ou `API_URL=...`: endereço da API (padrão http://localhost:8080)

Os fornecedores são usuários comuns, com senha `123456`; os anúncios do Vendedor Mock aparecem em
"Meus anúncios". Os ids dos fornecedores criados ficam em `.fornecedores-cache.json` (ignorado pelo git).
