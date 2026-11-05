#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Cadastra categorias padrão na PSNF API (POST /categorias).
#
# Uso:
#   ./psnf-api/scripts/popular-categorias.sh
#   API_URL=http://localhost:8080 ./psnf-api/scripts/popular-categorias.sh
#
# Pode ser executado várias vezes: categorias que já existem são apenas
# informadas (a API recusa nomes repetidos, sem diferenciar maiúsculas).
# Para adicionar/remover categorias, edite a lista CATEGORIAS abaixo.
# -----------------------------------------------------------------------------
set -uo pipefail

API_URL="${API_URL:-http://localhost:8080}"

CATEGORIAS=(
  "Alimentos e Bebidas"
  "Embalagens"
  "Limpeza e Higiene"
  "Material de Escritório"
  "Papelaria"
  "Informática"
  "Eletrônicos"
  "Eletrodomésticos"
  "Celulares e Acessórios"
  "Ferramentas"
  "Construção"
  "Elétrica"
  "Hidráulica"
  "Iluminação"
  "Móveis"
  "Casa e Decoração"
  "Cama, Mesa e Banho"
  "Utilidades Domésticas"
  "Roupas"
  "Calçados"
  "Moda Íntima"
  "Bolsas e Acessórios"
  "Uniformes e EPIs"
  "Beleza e Perfumaria"
  "Saúde"
  "Esportes e Fitness"
  "Brinquedos"
  "Bebês"
  "Pet Shop"
  "Automotivo"
  "Agro e Jardinagem"
  "Industrial"
  "Logística e Armazenagem"
  "Festas e Eventos"
)

command -v curl >/dev/null || { echo "Erro: o curl não está instalado." >&2; exit 1; }

if ! curl -s -o /dev/null --max-time 5 "$API_URL/categorias"; then
  echo "Erro: a API não respondeu em $API_URL. Ela está rodando?" >&2
  exit 1
fi

# escapa aspas e barras para montar o JSON com segurança
json_string() {
  local s=${1//\\/\\\\}
  printf '"%s"' "${s//\"/\\\"}"
}

criadas=0; existentes=0; falhas=0
echo "Cadastrando ${#CATEGORIAS[@]} categorias em $API_URL ..."
echo

for nome in "${CATEGORIAS[@]}"; do
  resposta=$(curl -s -w '\n%{http_code}' --max-time 10 -X POST "$API_URL/categorias" \
    -H 'Content-Type: application/json' \
    -d "{\"nome\": $(json_string "$nome")}")
  status=${resposta##*$'\n'}
  corpo=${resposta%$'\n'*}

  case "$status" in
    201) echo "  ✔ criada      $nome"; criadas=$((criadas + 1)) ;;
    422) echo "  • já existia  $nome"; existentes=$((existentes + 1)) ;;
    *)   echo "  ✘ erro $status  $nome  $corpo"; falhas=$((falhas + 1)) ;;
  esac
done

echo
echo "Resumo: $criadas criada(s), $existentes já existia(m), $falhas falha(s)."
[ "$falhas" -eq 0 ]
