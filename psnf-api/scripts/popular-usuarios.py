#!/usr/bin/env python3
"""
Cadastra usuários prontos para testes na PSNF API: 12 empresas compradoras fictícias, cada uma com
telefone e endereço de entrega real (CEPs conferidos na BrasilAPI), em vários estados.

Uso (com a API rodando):
    python3 psnf-api/scripts/popular-usuarios.py
    API_URL=http://localhost:8081 python3 psnf-api/scripts/popular-usuarios.py

Todos usam a senha 123456. Depois de rodar, os usuários aparecem no seletor "Logado como" do front.
Pode ser executado várias vezes: usuários com email já cadastrado são apenas informados.
Só usa a biblioteca padrão do Python 3 (nada para instalar).
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request

SENHA = "123456"

# nome, email, telefone, endereço (cep, logradouro, número, complemento, bairro, cidade, uf)
USUARIOS = [
    ("Mercadinho Bom Preço", "compras@bompreco.psnf.dev", "11987650001",
     ("01310-100", "Avenida Paulista", "1000", "Loja 3", "Bela Vista", "São Paulo", "SP")),
    ("Restaurante Sabor Caseiro", "contato@saborcaseiro.psnf.dev", "11987650002",
     ("04538-132", "Avenida Brigadeiro Faria Lima", "2500", "Térreo", "Itaim Bibi", "São Paulo", "SP")),
    ("Construtora Alicerce", "suprimentos@alicerce.psnf.dev", "21987650003",
     ("20040-020", "Praça Pio X", "15", "Sala 801", "Centro", "Rio de Janeiro", "RJ")),
    ("Academia Corpo em Forma", "gerencia@corpoemforma.psnf.dev", "31987650004",
     ("30130-010", "Praça Sete de Setembro", "120", None, "Centro", "Belo Horizonte", "MG")),
    ("Hotel Recanto Verde", "compras@recantoverde.psnf.dev", "71987650005",
     ("40020-000", "Rua Chile", "22", None, "Centro", "Salvador", "BA")),
    ("Clínica Bem Estar", "administrativo@bemestar.psnf.dev", "81987650006",
     ("50030-230", "Cais do Apolo", "222", "Bloco B", "Recife", "Recife", "PE")),
    ("Papelaria Ponto Escolar", "pedidos@pontoescolar.psnf.dev", "85987650007",
     ("60060-090", "Avenida Dom Manuel", "1020", None, "Centro", "Fortaleza", "CE")),
    ("Escritório Prisma Contábil", "financeiro@prisma.psnf.dev", "61987650008",
     ("70040-010", "Quadra SBN Quadra 1", "Bloco A", "Sala 1203", "Asa Norte", "Brasília", "DF")),
    ("Pet Shop Amigo Fiel", "loja@amigofiel.psnf.dev", "41987650009",
     ("80010-000", "Rua José Loureiro", "340", None, "Centro", "Curitiba", "PR")),
    ("Oficina Motor Forte", "oficina@motorforte.psnf.dev", "51987650010",
     ("90010-000", "Rua Siqueira Campos", "890", "Galpão 2", "Centro Histórico", "Porto Alegre", "RS")),
    ("Loja Casa Bonita", "compras@casabonita.psnf.dev", "48987650011",
     ("88010-000", "Rua Felipe Schmidt", "455", None, "Centro", "Florianópolis", "SC")),
    ("Escola Pequeno Saber", "secretaria@pequenosaber.psnf.dev", "92987650012",
     ("69005-040", "Rua Miranda Leão", "77", None, "Centro", "Manaus", "AM")),
]


class ApiErro(Exception):
    def __init__(self, status, corpo):
        super().__init__(f"HTTP {status}: {corpo}")
        self.status = status


def chamar(metodo, caminho, corpo=None, tentativa=0):
    args, kwargs = (metodo, caminho), {"corpo": corpo}
    dados = json.dumps(corpo).encode("utf-8") if corpo is not None else None
    req = urllib.request.Request(API + caminho, data=dados, method=metodo,
                                 headers={"Content-Type": "application/json"} if dados else {})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            texto = resp.read().decode("utf-8")
            return json.loads(texto) if texto else None
    except urllib.error.HTTPError as e:
        if e.code == 429 and tentativa < 5:
            # rate limit da API: espera o tempo pedido no Retry-After e tenta de novo
            espera = int(e.headers.get("Retry-After", "5"))
            print(f"    (limite de requisições da API: aguardando {espera} s)")
            time.sleep(espera)
            return chamar(*args, tentativa=tentativa + 1, **kwargs)
        raise ApiErro(e.code, e.read().decode("utf-8", "replace")) from None


def main():
    global API
    parser = argparse.ArgumentParser(description="Cadastra usuários de teste na PSNF API.")
    parser.add_argument("--api", default=os.environ.get("API_URL", "http://localhost:8080"), help="URL da API")
    API = parser.parse_args().api.rstrip("/")

    try:
        chamar("GET", "/auth/csrf")
    except (urllib.error.URLError, OSError):
        sys.exit(f"Erro: a API não respondeu em {API}. Ela está rodando?")

    print(f"Cadastrando {len(USUARIOS)} usuários em {API} ...\n")
    criados = existentes = falhas = 0
    for nome, email, telefone, (cep, logradouro, numero, complemento, bairro, cidade, uf) in USUARIOS:
        corpo = {
            "nome": nome, "email": email, "senha": SENHA, "telefone": telefone,
            "enderecos": [{"cep": cep, "logradouro": logradouro, "numero": numero, "complemento": complemento,
                           "bairro": bairro, "cidade": cidade, "uf": uf}],
        }
        try:
            chamar("POST", "/usuarios", corpo)
            criados += 1
            print(f"  ✔ criado      {nome:<28} {email}")
        except ApiErro as e:
            if e.status == 422:
                existentes += 1
                print(f"  • já existia  {nome:<28} {email}")
            else:
                falhas += 1
                print(f"  ✘ erro        {nome:<28} {e}")

    print(f"\nResumo: {criados} criado(s), {existentes} já existia(m), {falhas} falha(s). Senha de todos: {SENHA}")

    # confere o login de cada um e mostra o id (útil em testes)
    print("\nLogin (POST /auth/token):")
    for nome, email, *_ in USUARIOS:
        try:
            r = chamar("POST", "/auth/token", {"email": email, "senha": SENHA})
            print(f"  ✔ {r['usuario']['id']}  {nome}")
        except ApiErro as e:
            print(f"  ✘ {email}: {e}")

    sys.exit(1 if falhas else 0)


API = ""
if __name__ == "__main__":
    main()
