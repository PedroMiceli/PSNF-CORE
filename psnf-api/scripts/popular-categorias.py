#!/usr/bin/env python3
"""
Cadastra as categorias da plataforma direto no banco (PostgreSQL) da PSNF API.

Normalmente não é preciso: a própria API insere as categorias ao iniciar (migração repetível do Flyway
db/migration/R__categorias.sql). Use este script quando o banco ficou sem elas, por exemplo um banco de
testes criado de outro jeito, ou se a API ainda não subiu com a versão atual das migrações.

A lista de categorias NÃO é repetida aqui: o script executa o próprio R__categorias.sql, que continua
sendo o único lugar onde as categorias são definidas. Ele é idempotente: só insere as que ainda não
existem (o nome é comparado sem diferenciar maiúsculas/minúsculas). Pode rodar quantas vezes quiser.

Uso (o banco precisa ter as tabelas, então suba a API pelo menos uma vez antes):
    python3 psnf-api/scripts/popular-categorias.py
    python3 psnf-api/scripts/popular-categorias.py --host 192.168.0.20 --senha minhasenha
    python3 psnf-api/scripts/popular-categorias.py --docker meu-postgres   # psql de dentro do container
    PGPASSWORD=minhasenha python3 psnf-api/scripts/popular-categorias.py

Os padrões são os mesmos do application.properties (localhost:5432, banco psnf, usuário postgres).
Precisa do psql instalado (pacote postgresql-client), ou de --docker com o nome do container do Postgres.
Só usa a biblioteca padrão do Python 3 (nada para instalar).
"""
import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

SQL = Path(__file__).resolve().parent.parent / "src/main/resources/db/migration/R__categorias.sql"

CONTAR = "SELECT count(*) FROM categoria;"
LISTAR = "SELECT nome FROM categoria ORDER BY nome;"
TEM_TABELA = "SELECT to_regclass('public.categoria') IS NOT NULL;"


def main():
    p = argparse.ArgumentParser(description="Cadastra as categorias da plataforma no banco da PSNF API.")
    p.add_argument("--host", default=os.getenv("PGHOST", "localhost"))
    p.add_argument("--porta", default=os.getenv("PGPORT", "5432"))
    p.add_argument("--banco", default=os.getenv("PGDATABASE", "psnf"))
    p.add_argument("--usuario", default=os.getenv("PGUSER", "postgres"))
    p.add_argument("--senha", default=os.getenv("PGPASSWORD", "opentech"))
    p.add_argument("--docker", metavar="CONTAINER",
                   help="roda o psql dentro deste container (quando o psql não está instalado na máquina)")
    args = p.parse_args()

    if not SQL.is_file():
        sys.exit(f"Arquivo não encontrado: {SQL}")
    sql = SQL.read_text(encoding="utf-8")

    if args.docker:
        if not shutil.which("docker"):
            sys.exit("docker não encontrado no PATH.")
        # dentro do container o banco é o local dele: host/porta da máquina não valem lá
        base = ["docker", "exec", "-i", "-e", f"PGPASSWORD={args.senha}", args.docker,
                "psql", "-U", args.usuario, "-d", args.banco]
        env = None
        destino = f"container {args.docker}, banco {args.banco}"
    else:
        if not shutil.which("psql"):
            sys.exit("psql não encontrado. Instale o cliente do PostgreSQL (ex.: apt install postgresql-client)\n"
                     "ou use --docker <container do postgres>.")
        base = ["psql", "-h", args.host, "-p", str(args.porta), "-U", args.usuario, "-d", args.banco]
        env = {**os.environ, "PGPASSWORD": args.senha}
        destino = f"{args.usuario}@{args.host}:{args.porta}/{args.banco}"

    def psql(comando=None, entrada=None):
        cmd = base + ["-v", "ON_ERROR_STOP=1", "-X", "-q", "-t", "-A"] + (["-c", comando] if comando else [])
        r = subprocess.run(cmd, input=entrada, capture_output=True, text=True, env=env)
        if r.returncode != 0:
            sys.exit(f"Erro ao executar no banco ({destino}):\n{r.stderr.strip()}")
        return r.stdout.strip()

    print(f"Banco: {destino}")
    if psql(TEM_TABELA) != "t":
        sys.exit("A tabela 'categoria' não existe neste banco. Suba a API uma vez para o Flyway criar o esquema\n"
                 "(ela já insere as categorias ao iniciar) e rode este script de novo se ainda faltar alguma.")

    antes = int(psql(CONTAR))
    psql(entrada=sql)
    depois = int(psql(CONTAR))

    print(f"Categorias: {depois - antes} inseridas, {antes} já existiam, {depois} no total.")
    for nome in psql(LISTAR).splitlines():
        print(f"  - {nome}")


if __name__ == "__main__":
    main()
