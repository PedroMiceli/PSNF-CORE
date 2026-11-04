// Roda antes do "npm run dev" e do "npm run build": o Vite 8 exige Node ^20.19.0 || >=22.12.0.
// Escrito em JS simples para funcionar (e dar uma mensagem clara) mesmo em versões antigas do Node.
function suportado(versao) {
  const [maior, menor] = versao.split('.').map(Number);
  return (maior === 20 && menor >= 19) || (maior === 22 && menor >= 12) || maior >= 23;
}

const versao = process.argv[2] || process.versions.node;
if (!suportado(versao)) {
  console.error(
    '\n  ✘ Node ' + versao + ' não é suportado pelo psnf-ui (Vite 8 exige Node 20.19+ ou 22.12+).\n\n' +
    '  Instale o Node 22, por exemplo com o nvm:\n' +
    '      nvm install 22 && nvm use 22\n' +
    '  (dentro de psnf-ui basta "nvm use", que lê o arquivo .nvmrc)\n'
  );
  process.exit(1);
}
