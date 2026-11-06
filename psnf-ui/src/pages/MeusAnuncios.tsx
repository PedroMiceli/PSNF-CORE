import { Link } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useAcao, useApi } from '../api/useApi';
import { formatarData, formatarMoeda } from '../components/format';
import {
  acoes, Badge, BADGE_ATIVO, BADGE_INATIVO, Botao, Carregando, ErroBox, ImagemAnuncio, LinkBotao, tabela, TituloPagina, Vazio,
} from '../components/ui';

export function MeusAnuncios() {
  const anuncios = useApi(() => api.meusAnuncios(), []);
  const acao = useAcao();

  const alternar = async (id: string, ativo: boolean) => {
    const r = await acao.executar(() => (ativo ? api.desativarAnuncio(id) : api.ativarAnuncio(id)));
    if (r) anuncios.setData((lista) => lista?.map((a) => (a.id === r.id ? r : a)) ?? null);
  };

  return (
    <section>
      <TituloPagina>
        <h1>Meus anúncios</h1>
        <div className={acoes}>
          <LinkBotao to="/ordens/nova">Gerar ordem de pedido</LinkBotao>
          <LinkBotao to="/anuncios/novo" variante="primario">+ Novo anúncio</LinkBotao>
        </div>
      </TituloPagina>
      <ErroBox erro={anuncios.error ?? acao.erro} />
      {anuncios.loading ? (
        <Carregando />
      ) : !anuncios.data?.length ? (
        <Vazio>Você ainda não tem anúncios. <Link to="/anuncios/novo">Criar o primeiro</Link></Vazio>
      ) : (
        <div className={tabela.caixa}>
          <table className={tabela.tabela}>
            <thead>
              <tr>
                {['', 'Título', 'A partir de', 'Variações', 'Criado em', 'Status', ''].map((t, i) => <th key={i} className={tabela.th}>{t}</th>)}
              </tr>
            </thead>
            <tbody>
              {anuncios.data.map((a) => (
                <tr key={a.id}>
                  <td className={tabela.td}>
                    <ImagemAnuncio caminho={(a.imagens.find((i) => i.principal) ?? a.imagens[0])?.caminho} alt="" compacta className="h-9 w-12 rounded-md" />
                  </td>
                  <td className={tabela.td}><Link to={`/anuncios/${a.id}`}>{a.titulo}</Link></td>
                  <td className={tabela.td}>{formatarMoeda(a.precoMenor)}</td>
                  <td className={tabela.td}>{a.variacoes.map((v) => v.variacao).join(', ') || '—'}</td>
                  <td className={tabela.td}>{formatarData(a.dataCadastro)}</td>
                  <td className={tabela.td}>
                    {a.ativo ? <Badge className={BADGE_ATIVO}>Ativo</Badge> : <Badge className={BADGE_INATIVO}>Inativo</Badge>}
                  </td>
                  <td className={tabela.td}>
                    <div className="flex justify-end gap-1.5">
                      <LinkBotao to={`/anuncios/${a.id}/editar`} tamanho="pequeno">Editar</LinkBotao>
                      <Botao tamanho="pequeno" disabled={acao.enviando} onClick={() => alternar(a.id, a.ativo)}>
                        {a.ativo ? 'Desativar' : 'Ativar'}
                      </Botao>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
