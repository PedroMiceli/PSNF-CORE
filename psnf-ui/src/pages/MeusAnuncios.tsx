import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { formatarData, formatarMoeda } from '../components/format';
import { Carregando, ErroBox, ImagemAnuncio, Vazio } from '../components/ui';

export function MeusAnuncios() {
  const anuncios = useApi(() => api.meusAnuncios(), []);
  const acao = useAcao();

  const alternar = async (id: string, ativo: boolean) => {
    const r = await acao.executar(() => (ativo ? api.desativarAnuncio(id) : api.ativarAnuncio(id)));
    if (r) anuncios.setData((lista) => lista?.map((a) => (a.id === r.id ? r : a)) ?? null);
  };

  return (
    <section>
      <div className="titulo-pagina">
        <h1>Meus anúncios</h1>
        <div className="acoes">
          <Link to="/ordens/nova" className="btn">Gerar ordem de pedido</Link>
          <Link to="/anuncios/novo" className="btn primario">+ Novo anúncio</Link>
        </div>
      </div>
      <ErroBox erro={anuncios.error ?? acao.erro} />
      {anuncios.loading ? (
        <Carregando />
      ) : !anuncios.data?.length ? (
        <Vazio>Você ainda não tem anúncios. <Link to="/anuncios/novo">Criar o primeiro</Link></Vazio>
      ) : (
        <table className="tabela">
          <thead>
            <tr>
              <th></th><th>Título</th><th>A partir de</th><th>Variações</th><th>Criado em</th><th>Status</th><th></th>
            </tr>
          </thead>
          <tbody>
            {anuncios.data.map((a) => (
              <tr key={a.id}>
                <td><ImagemAnuncio caminho={(a.imagens.find((i) => i.principal) ?? a.imagens[0])?.caminho} alt="" className="mini" /></td>
                <td><Link to={`/anuncios/${a.id}`}>{a.titulo}</Link></td>
                <td>{formatarMoeda(a.precoMenor)}</td>
                <td>{a.variacoes.map((v) => v.variacao).join(', ') || '—'}</td>
                <td>{formatarData(a.dataCadastro)}</td>
                <td>{a.ativo ? <span className="badge status-PAGO">Ativo</span> : <span className="badge status-CANCELADO">Inativo</span>}</td>
                <td className="acoes-linha">
                  <Link to={`/anuncios/${a.id}/editar`} className="btn pequeno">Editar</Link>
                  <button className="btn pequeno" disabled={acao.enviando} onClick={() => alternar(a.id, a.ativo)}>
                    {a.ativo ? 'Desativar' : 'Ativar'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
