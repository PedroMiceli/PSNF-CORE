import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { formatarData, formatarMoeda } from '../components/format';
import { Carregando, cx, ErroBox, LinkBotao, PagamentoPendenteBadge, StatusBadge, tabela, TituloPagina, Vazio } from '../components/ui';

export function Ordens() {
  const [aba, setAba] = useState<'compras' | 'vendas'>('compras');
  const ordens = useApi(() => (aba === 'compras' ? api.compras() : api.vendas()), [aba]);
  const navigate = useNavigate();

  const botaoAba = (valor: typeof aba, texto: string) => (
    <button role="tab" aria-selected={aba === valor} onClick={() => setAba(valor)}
      className={cx('-mb-px cursor-pointer border-b-2 px-4 py-3 text-[0.95rem] font-semibold',
        aba === valor ? 'border-primaria text-primaria' : 'border-transparent text-suave hover:text-texto')}>
      {texto}
    </button>
  );

  return (
    <section>
      <TituloPagina>
        <h1>Ordens de pedido</h1>
        <LinkBotao to="/ordens/nova" variante="primario">+ Gerar ordem de pedido</LinkBotao>
      </TituloPagina>
      <div role="tablist" className="mb-4 flex gap-1 border-b border-borda">
        {botaoAba('compras', 'Minhas compras')}
        {botaoAba('vendas', 'Minhas vendas')}
      </div>
      <ErroBox erro={ordens.error} />
      {ordens.loading ? (
        <Carregando />
      ) : !ordens.data?.length ? (
        <Vazio>
          {aba === 'compras'
            ? 'Nenhuma compra. Troque de usuário no topo para ver as ordens geradas pelos vendedores.'
            : 'Nenhuma venda. Gere uma ordem de pedido para um comprador.'}
        </Vazio>
      ) : (
        <div className={tabela.caixa}>
          <table className={tabela.tabela}>
            <thead>
              <tr>
                {['Código', 'Status', 'Data', aba === 'compras' ? 'Vendedor' : 'Comprador', 'Itens', 'Previsão'].map((t) => (
                  <th key={t} className={tabela.th}>{t}</th>
                ))}
                <th className={cx(tabela.th, 'text-right')}>Total</th>
              </tr>
            </thead>
            <tbody>
              {ordens.data.map((o) => (
                <tr key={o.id} onClick={() => navigate(`/ordens/${o.id}`)} className="cursor-pointer hover:bg-slate-50">
                  <td className={tabela.td}><Link to={`/ordens/${o.id}`} className="font-bold text-texto">{o.codigo}</Link></td>
                  <td className={tabela.td}>
                    <span className="inline-flex flex-wrap gap-1"><StatusBadge status={o.status} />
                      <PagamentoPendenteBadge status={o.status} pendente={o.pagamentoPendente} /></span>
                  </td>
                  <td className={cx(tabela.td, 'whitespace-nowrap')}>{formatarData(o.dataCadastro)}</td>
                  <td className={tabela.td}>{aba === 'compras' ? o.vendedorNome : o.compradorNome}</td>
                  <td className={tabela.td}>{o.quantidadeItens}</td>
                  <td className={tabela.td}>{formatarData(o.previsaoDeEntrega)}</td>
                  <td className={cx(tabela.td, tabela.num)}>{formatarMoeda(o.valorTotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
