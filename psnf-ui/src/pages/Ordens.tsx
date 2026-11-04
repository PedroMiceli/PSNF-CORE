import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/endpoints';
import { useApi } from '../api/useApi';
import { formatarData, formatarMoeda } from '../components/format';
import { Carregando, ErroBox, StatusBadge, Vazio } from '../components/ui';

export function Ordens() {
  const [aba, setAba] = useState<'compras' | 'vendas'>('compras');
  const ordens = useApi(() => (aba === 'compras' ? api.compras() : api.vendas()), [aba]);
  const navigate = useNavigate();

  return (
    <section>
      <div className="titulo-pagina">
        <h1>Ordens de pedido</h1>
        <Link to="/ordens/nova" className="btn primario">+ Gerar ordem de pedido</Link>
      </div>
      <div className="abas">
        <button className={aba === 'compras' ? 'ativo' : ''} onClick={() => setAba('compras')}>Minhas compras</button>
        <button className={aba === 'vendas' ? 'ativo' : ''} onClick={() => setAba('vendas')}>Minhas vendas</button>
      </div>
      <ErroBox erro={ordens.error} />
      {ordens.loading ? (
        <Carregando />
      ) : !ordens.data?.length ? (
        <Vazio>
          {aba === 'compras'
            ? 'Nenhuma compra. Troque para o "Comprador Mock" no topo para ver as ordens geradas pelo vendedor.'
            : 'Nenhuma venda. Gere uma ordem de pedido para um comprador.'}
        </Vazio>
      ) : (
        <table className="tabela clicavel">
          <thead>
            <tr><th>Código</th><th>Status</th><th>Data</th><th>{aba === 'compras' ? 'Vendedor' : 'Comprador'}</th><th>Itens</th><th>Previsão</th><th className="num">Total</th></tr>
          </thead>
          <tbody>
            {ordens.data.map((o) => (
              <tr key={o.id} onClick={() => navigate(`/ordens/${o.id}`)}>
                <td><strong>{o.codigo}</strong></td>
                <td><StatusBadge status={o.status} /></td>
                <td>{formatarData(o.dataCadastro)}</td>
                <td>{aba === 'compras' ? o.vendedorNome : o.compradorNome}</td>
                <td>{o.quantidadeItens}</td>
                <td>{formatarData(o.previsaoDeEntrega)}</td>
                <td className="num">{formatarMoeda(o.valorTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
