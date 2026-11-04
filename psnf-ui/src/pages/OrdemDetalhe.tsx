import { useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/endpoints';
import type { OrdemDePedido, StatusOrdem, TipoPagamento } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { formatarData, formatarMoeda, STATUS_LABEL } from '../components/format';
import { Campo, Carregando, ErroBox, StatusBadge } from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const FLUXO: StatusOrdem[] = ['AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'];

export function OrdemDetalhe() {
  const { id = '' } = useParams();
  const { usuarioId } = useUsuario();
  const ordem = useApi(() => api.ordem(id), [id]);
  const acao = useAcao();

  if (ordem.loading) return <Carregando />;
  if (ordem.error || !ordem.data) {
    return (
      <section>
        <Link to="/ordens" className="voltar">← Ordens de pedido</Link>
        <ErroBox erro={ordem.error} />
      </section>
    );
  }

  const o = ordem.data;
  const souComprador = o.comprador.id === usuarioId;
  const souVendedor = o.vendedor.id === usuarioId;
  const executar = async (fn: () => Promise<OrdemDePedido>) => {
    const r = await acao.executar(fn);
    if (r) ordem.setData(r);
    return !!r;
  };

  return (
    <section className="ordem">
      <Link to="/ordens" className="voltar">← Ordens de pedido</Link>
      <div className="titulo-pagina">
        <h1>Ordem de pedido {o.codigo} <StatusBadge status={o.status} /></h1>
        <span className="papel">Você é o <strong>{souComprador ? 'comprador' : souVendedor ? 'vendedor' : 'observador'}</strong></span>
      </div>

      <Linha status={o.status} />
      <ErroBox erro={acao.erro} />

      <div className="ordem-grid">
        <div className="painel">
          <h3>Participantes</h3>
          <dl className="dados">
            <dt>Comprador</dt><dd>{o.comprador.nome} <small>{o.comprador.email}</small></dd>
            <dt>Vendedor</dt><dd>{o.vendedor.nome} <small>{o.vendedor.email}</small></dd>
            <dt>Criada em</dt><dd>{formatarData(o.dataCadastro)}</dd>
            <dt>Previsão de entrega</dt><dd>{formatarData(o.previsaoDeEntrega)}</dd>
          </dl>
        </div>
        <div className="painel valores">
          <h3>Valores</h3>
          <div><span>Total</span><strong>{formatarMoeda(o.valorTotal)}</strong></div>
          <div><span>Pago</span><strong className="verde">{formatarMoeda(o.valorPago)}</strong></div>
          <div><span>A pagar</span><strong className={o.valorRestante > 0 ? 'laranja' : ''}>{formatarMoeda(o.valorRestante)}</strong></div>
        </div>
      </div>

      <Acoes ordem={o} souComprador={souComprador} souVendedor={souVendedor} enviando={acao.enviando} executar={executar} />

      <div className="painel">
        <h3>Itens</h3>
        <table className="tabela">
          <thead><tr><th>Produto</th><th>Variação</th><th className="num">Qtd.</th><th className="num">Valor unit.</th><th className="num">Subtotal</th></tr></thead>
          <tbody>
            {o.itens.map((i) => (
              <tr key={i.id}>
                <td><Link to={`/anuncios/${i.anuncioId}`}>{i.tituloAnuncio}</Link></td>
                <td>{i.variacao ?? '—'}</td>
                <td className="num">{i.quantidade}</td>
                <td className="num">{formatarMoeda(i.valorUnidade)}</td>
                <td className="num">{formatarMoeda(i.subtotal)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot><tr><td colSpan={4}>Total</td><td className="num total">{formatarMoeda(o.valorTotal)}</td></tr></tfoot>
        </table>
      </div>

      <div className="ordem-grid">
        <Pagamentos ordem={o} podePagar={souComprador && o.status === 'AGUARDANDO_PAGAMENTO'} enviando={acao.enviando} executar={executar} />
        <NotasFiscais ordem={o} podeAdicionar={souVendedor && ['PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)} enviando={acao.enviando} executar={executar} />
      </div>

      <Mensagens ordem={o} usuarioId={usuarioId} podeEnviar={souComprador || souVendedor} enviando={acao.enviando} executar={executar} />
    </section>
  );
}

function Linha({ status }: { status: StatusOrdem }) {
  if (status === 'CANCELADO') return <div className="linha-status cancelada">Esta ordem foi cancelada.</div>;
  const atual = FLUXO.indexOf(status);
  return (
    <ol className="linha-status">
      {FLUXO.map((s, i) => (
        <li key={s} className={i < atual ? 'feito' : i === atual ? 'atual' : ''}>
          <span className="bolinha">{i < atual ? '✔' : i + 1}</span>
          {STATUS_LABEL[s]}
        </li>
      ))}
    </ol>
  );
}

type Exec = (fn: () => Promise<OrdemDePedido>) => Promise<boolean>;

function Acoes({ ordem, souComprador, souVendedor, enviando, executar }: {
  ordem: OrdemDePedido; souComprador: boolean; souVendedor: boolean; enviando: boolean; executar: Exec;
}) {
  const [previsao, setPrevisao] = useState('');
  const podeCancelar = (souComprador || souVendedor) && ['AGUARDANDO_PAGAMENTO', 'PAGO'].includes(ordem.status);
  const podeDespachar = souVendedor && ordem.status === 'PAGO';
  const podeConfirmar = souComprador && ordem.status === 'ENVIADO';
  if (!podeCancelar && !podeDespachar && !podeConfirmar) return null;

  return (
    <div className="painel acoes-ordem">
      <h3>Próximo passo</h3>
      <div className="acoes">
        {podeDespachar && (
          <>
            <Campo label="Previsão de entrega">
              <input type="date" value={previsao} min={new Date().toISOString().slice(0, 10)} onChange={(e) => setPrevisao(e.target.value)} />
            </Campo>
            <button className="btn primario" disabled={enviando} onClick={() => executar(() => api.despachar(ordem.id, previsao || null))}>
              Pedido despachado
            </button>
          </>
        )}
        {podeConfirmar && (
          <button className="btn primario" disabled={enviando} onClick={() => executar(() => api.confirmarEntrega(ordem.id))}>
            Confirmar entrega
          </button>
        )}
        {podeCancelar && (
          <button className="btn perigo" disabled={enviando}
            onClick={() => window.confirm('Cancelar esta ordem de pedido?') && executar(() => api.cancelar(ordem.id))}>
            Cancelar ordem
          </button>
        )}
      </div>
      {ordem.status === 'AGUARDANDO_PAGAMENTO' && souVendedor && <p className="dica">Aguardando o comprador registrar o pagamento.</p>}
      {ordem.status === 'PAGO' && souComprador && <p className="dica">Aguardando o vendedor despachar o pedido.</p>}
    </div>
  );
}

function Pagamentos({ ordem, podePagar, enviando, executar }: { ordem: OrdemDePedido; podePagar: boolean; enviando: boolean; executar: Exec }) {
  const [tipo, setTipo] = useState<TipoPagamento>('PIX');
  const [valor, setValor] = useState(String(ordem.valorRestante));
  const [extra, setExtra] = useState({ chavePix: '', boleto: '', codigoBanco: '', agencia: '', conta: '', nomeReferencia: '' });
  const set = (k: keyof typeof extra) => (e: { target: { value: string } }) => setExtra({ ...extra, [k]: e.target.value });

  const pagar = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await executar(() => api.pagar(ordem.id, { tipoPagamento: tipo, valor: Number(valor), ...extra }));
    if (ok) setExtra({ chavePix: '', boleto: '', codigoBanco: '', agencia: '', conta: '', nomeReferencia: '' });
  };

  return (
    <div className="painel">
      <h3>Pagamentos</h3>
      {ordem.pagamentos.length === 0 ? <p className="dica">Nenhum pagamento registrado.</p> : (
        <ul className="lista">
          {ordem.pagamentos.map((p) => (
            <li key={p.id}>
              <span className="badge">{p.tipoPagamento}</span> {formatarMoeda(p.valor)}
              {p.nomeReferencia && <small> · {p.nomeReferencia}</small>}
              <small className="direita">{formatarData(p.dataCadastro)}</small>
            </li>
          ))}
        </ul>
      )}
      {podePagar && (
        <form onSubmit={pagar} className="sub-form">
          <h4>Registrar pagamento</h4>
          <div className="linha-form">
            <Campo label="Forma de pagamento">
              <select value={tipo} onChange={(e) => setTipo(e.target.value as TipoPagamento)}>
                <option value="PIX">PIX</option>
                <option value="BOLETO">Boleto</option>
                <option value="TRANSFERENCIA">Transferência</option>
              </select>
            </Campo>
            <Campo label="Valor (R$)" dica={`Restante: ${formatarMoeda(ordem.valorRestante)}`}>
              <input type="number" step="0.01" value={valor} onChange={(e) => setValor(e.target.value)} />
            </Campo>
          </div>
          <div className="linha-form">
            {tipo === 'PIX' && <Campo label="Chave PIX"><input value={extra.chavePix} onChange={set('chavePix')} /></Campo>}
            {tipo === 'BOLETO' && <Campo label="Código do boleto"><input value={extra.boleto} onChange={set('boleto')} /></Campo>}
            {tipo === 'TRANSFERENCIA' && (
              <>
                <Campo label="Banco"><input value={extra.codigoBanco} onChange={set('codigoBanco')} placeholder="001" /></Campo>
                <Campo label="Agência"><input value={extra.agencia} onChange={set('agencia')} placeholder="0001" /></Campo>
                <Campo label="Conta"><input value={extra.conta} onChange={set('conta')} /></Campo>
              </>
            )}
            <Campo label="Referência"><input value={extra.nomeReferencia} onChange={set('nomeReferencia')} placeholder="Entrada, saldo..." /></Campo>
          </div>
          <button className="btn primario" disabled={enviando}>Registrar pagamento</button>
        </form>
      )}
    </div>
  );
}

function NotasFiscais({ ordem, podeAdicionar, enviando, executar }: { ordem: OrdemDePedido; podeAdicionar: boolean; enviando: boolean; executar: Exec }) {
  const [caminho, setCaminho] = useState('');
  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await executar(() => api.adicionarNotaFiscal(ordem.id, caminho))) setCaminho('');
  };
  return (
    <div className="painel">
      <h3>Notas fiscais</h3>
      {ordem.notasFiscais.length === 0 ? <p className="dica">Nenhuma nota fiscal anexada.</p> : (
        <ul className="lista">
          {ordem.notasFiscais.map((n) => (
            <li key={n.id}>📄 {n.caminho} <small className="direita">{formatarData(n.dataCadastro)}</small></li>
          ))}
        </ul>
      )}
      {podeAdicionar && (
        <form onSubmit={enviar} className="sub-form linha-form">
          <Campo label="Arquivo / caminho da NF"><input value={caminho} onChange={(e) => setCaminho(e.target.value)} placeholder="notas/nf-000123.pdf" /></Campo>
          <button className="btn" disabled={enviando}>Anexar</button>
        </form>
      )}
    </div>
  );
}

function Mensagens({ ordem, usuarioId, podeEnviar, enviando, executar }: {
  ordem: OrdemDePedido; usuarioId: string; podeEnviar: boolean; enviando: boolean; executar: Exec;
}) {
  const [texto, setTexto] = useState('');
  const autor = (id: string) => (id === ordem.comprador.id ? `${ordem.comprador.nome} (comprador)` : id === ordem.vendedor.id ? `${ordem.vendedor.nome} (vendedor)` : id);
  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await executar(() => api.enviarMensagem(ordem.id, texto))) setTexto('');
  };
  return (
    <div className="painel">
      <h3>Incidentes e mensagens</h3>
      <div className="chat">
        {ordem.mensagensIncidente.length === 0 && <p className="dica">Nenhuma mensagem. Use para relatar problemas com a carga, entrega ou NF.</p>}
        {ordem.mensagensIncidente.map((m) => (
          <div key={m.id} className={`msg ${m.usuarioCriadorId === usuarioId ? 'minha' : ''}`}>
            <small>{autor(m.usuarioCriadorId)} · {formatarData(m.dataCadastro)}</small>
            <p>{m.mensagem}</p>
            {m.imagem && <small>📎 {m.imagem}</small>}
          </div>
        ))}
      </div>
      {podeEnviar && (
        <form onSubmit={enviar} className="sub-form linha-form">
          <Campo label="Mensagem"><input value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Digite aqui a sua mensagem..." /></Campo>
          <button className="btn" disabled={enviando || !texto.trim()}>Enviar</button>
        </form>
      )}
    </div>
  );
}
