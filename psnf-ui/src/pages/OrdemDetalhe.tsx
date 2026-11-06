import { useState, type FormEvent, type ReactNode } from 'react';
import { Link, useParams } from 'react-router-dom';
import { urlArquivo } from '../api/client';
import { api } from '../api/endpoints';
import type { OrdemDePedido, StatusOrdem, TipoPagamento } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { DadosNotaFiscal, resumoItem } from '../components/DadosNotaFiscal';
import { formatarData, formatarMoeda, STATUS_LABEL } from '../components/format';
import { ACEITA_DOCUMENTOS, ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import {
  acoes, Badge, Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, InputMoeda, linhaForm, Painel, StatusBadge,
  tabela, TituloPagina, Voltar,
} from '../components/ui';
import { useUsuario } from '../context/UsuarioContext';

const FLUXO: StatusOrdem[] = ['AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'];

/** Duas colunas no computador, uma no celular. */
const grade2 = 'grid gap-4 md:grid-cols-2 [&>*]:min-w-0';
/** Lista com separadores (pagamentos, notas fiscais). */
const lista = 'divide-y divide-borda';
const itemLista = 'flex flex-wrap items-center gap-2 py-2.5';
const subForm = 'mt-4 border-t border-borda pt-2';

export function OrdemDetalhe() {
  const { id = '' } = useParams();
  const { usuarioId } = useUsuario();
  const ordem = useApi(() => api.ordem(id), [id]);
  const acao = useAcao();

  if (ordem.loading) return <Carregando />;
  if (ordem.error || !ordem.data) {
    return (
      <section>
        <Voltar to="/ordens">Ordens de pedido</Voltar>
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
    <section>
      <Voltar to="/ordens">Ordens de pedido</Voltar>
      <TituloPagina>
        <h1 className="break-words">Ordem de pedido {o.codigo} <StatusBadge status={o.status} /></h1>
        <span className="text-suave">Você é o <strong>{souComprador ? 'comprador' : souVendedor ? 'vendedor' : 'observador'}</strong></span>
      </TituloPagina>

      <LinhaDoTempo status={o.status} />
      <ErroBox erro={acao.erro} />

      <div className={grade2}>
        <Painel>
          <h3>Participantes</h3>
          <dl className="grid grid-cols-[max-content_minmax(0,1fr)] gap-x-4 gap-y-1.5">
            <Dado rotulo="Comprador">{o.comprador.nome} <small className="block break-words text-suave">{o.comprador.email}</small></Dado>
            <Dado rotulo="Vendedor">{o.vendedor.nome} <small className="block break-words text-suave">{o.vendedor.email}</small></Dado>
            <Dado rotulo="Criada em">{formatarData(o.dataCadastro)}</Dado>
            <Dado rotulo="Previsão de entrega">{formatarData(o.previsaoDeEntrega)}</Dado>
          </dl>
        </Painel>
        <Painel>
          <h3>Valores</h3>
          <div className={lista}>
            <Valor rotulo="Total">{formatarMoeda(o.valorTotal)}</Valor>
            <Valor rotulo="Pago" className="text-verde">{formatarMoeda(o.valorPago)}</Valor>
            <Valor rotulo="A pagar" className={o.valorRestante > 0 ? 'text-laranja' : ''}>{formatarMoeda(o.valorRestante)}</Valor>
          </div>
        </Painel>
      </div>

      <Acoes ordem={o} souComprador={souComprador} souVendedor={souVendedor} enviando={acao.enviando} executar={executar} />

      <Painel>
        <h3>Itens</h3>
        <div className="overflow-x-auto">
          <table className={tabela.tabela}>
            <thead>
              <tr>
                <th className={tabela.th}>Produto</th>
                <th className={tabela.th}>Variação</th>
                {['Qtd.', 'Valor unit.', 'Subtotal'].map((t) => <th key={t} className={cx(tabela.th, 'text-right')}>{t}</th>)}
              </tr>
            </thead>
            <tbody>
              {o.itens.map((i) => (
                <tr key={i.id}>
                  <td className={tabela.td}>
                    <Link to={`/anuncios/${i.anuncioId}`}>{i.tituloAnuncio}</Link>
                    {resumoItem(i) && <small className="block text-xs text-suave">{resumoItem(i)}</small>}
                  </td>
                  <td className={tabela.td}>{i.variacao ?? '—'}</td>
                  <td className={cx(tabela.td, tabela.num)}>{i.quantidade}</td>
                  <td className={cx(tabela.td, tabela.num)}>{formatarMoeda(i.valorUnidade)}</td>
                  <td className={cx(tabela.td, tabela.num)}>{formatarMoeda(i.subtotal)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan={4} className={tabela.td}>Total</td>
                <td className={cx(tabela.td, tabela.num, 'text-[1.05rem] font-extrabold')}>{formatarMoeda(o.valorTotal)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </Painel>

      <DadosNotaFiscal ordem={o} aberto={souVendedor && ['PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)} />

      <div className={grade2}>
        <Pagamentos ordem={o} podePagar={souComprador && o.status === 'AGUARDANDO_PAGAMENTO'} enviando={acao.enviando} executar={executar} />
        <NotasFiscais ordem={o} podeAdicionar={souVendedor && ['PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)} enviando={acao.enviando} executar={executar} />
      </div>

      <Mensagens ordem={o} usuarioId={usuarioId} podeEnviar={souComprador || souVendedor} enviando={acao.enviando} executar={executar} />
    </section>
  );
}

function Dado({ rotulo, children }: { rotulo: string; children: ReactNode }) {
  return (
    <>
      <dt className="text-sm text-suave">{rotulo}</dt>
      <dd className="min-w-0">{children}</dd>
    </>
  );
}

function Valor({ rotulo, className, children }: { rotulo: string; className?: string; children: ReactNode }) {
  return (
    <div className="flex justify-between py-2">
      <span>{rotulo}</span>
      <strong className={cx('text-lg', className)}>{children}</strong>
    </div>
  );
}

function LinhaDoTempo({ status }: { status: StatusOrdem }) {
  if (status === 'CANCELADO') {
    return <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 font-semibold text-vermelho">Esta ordem foi cancelada.</div>;
  }
  const atual = FLUXO.indexOf(status);
  return (
    <ol className="mb-4 flex gap-2 max-md:flex-col">
      {FLUXO.map((s, i) => {
        const feito = i < atual;
        const ehAtual = i === atual;
        return (
          <li key={s} aria-current={ehAtual ? 'step' : undefined}
            className={cx('flex flex-1 items-center gap-2 rounded-lg border p-3 text-sm',
              feito && 'border-green-200 bg-green-50 text-verde',
              ehAtual && 'border-primaria bg-blue-50 font-bold text-primaria',
              !feito && !ehAtual && 'border-borda bg-white text-suave')}>
            <span className={cx('inline-flex size-6 shrink-0 items-center justify-center rounded-full text-xs',
              feito ? 'bg-verde text-white' : ehAtual ? 'bg-primaria text-white' : 'bg-gray-200 text-suave')}>
              {feito ? '✔' : i + 1}
            </span>
            {STATUS_LABEL[s]}
          </li>
        );
      })}
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
    <Painel className="border-blue-200 bg-blue-50/40">
      <h3>Próximo passo</h3>
      <div className={acoes}>
        {podeDespachar && (
          <>
            <Campo label="Previsão de entrega" className="mb-0 max-w-[260px]">
              <input className={controle} type="date" value={previsao} min={new Date().toISOString().slice(0, 10)}
                onChange={(e) => setPrevisao(e.target.value)} />
            </Campo>
            <Botao variante="primario" disabled={enviando} onClick={() => executar(() => api.despachar(ordem.id, previsao || null))}>
              Pedido despachado
            </Botao>
          </>
        )}
        {podeConfirmar && (
          <Botao variante="primario" disabled={enviando} onClick={() => executar(() => api.confirmarEntrega(ordem.id))}>
            Confirmar entrega
          </Botao>
        )}
        {podeCancelar && (
          <Botao variante="perigo" disabled={enviando}
            onClick={() => window.confirm('Cancelar esta ordem de pedido?') && executar(() => api.cancelar(ordem.id))}>
            Cancelar ordem
          </Botao>
        )}
      </div>
      {ordem.status === 'AGUARDANDO_PAGAMENTO' && souVendedor && <p className={cx(dica, 'mb-0')}>Aguardando o comprador registrar o pagamento.</p>}
      {ordem.status === 'PAGO' && souComprador && <p className={cx(dica, 'mb-0')}>Aguardando o vendedor despachar o pedido.</p>}
    </Painel>
  );
}

function Pagamentos({ ordem, podePagar, enviando, executar }: { ordem: OrdemDePedido; podePagar: boolean; enviando: boolean; executar: Exec }) {
  const [tipo, setTipo] = useState<TipoPagamento>('PIX');
  const [valor, setValor] = useState(ordem.valorRestante.toFixed(2));
  const [extra, setExtra] = useState({ chavePix: '', boleto: '', codigoBanco: '', agencia: '', conta: '', nomeReferencia: '' });
  const set = (k: keyof typeof extra) => (e: { target: { value: string } }) => setExtra({ ...extra, [k]: e.target.value });

  const pagar = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await executar(() => api.pagar(ordem.id, { tipoPagamento: tipo, valor: Number(valor), ...extra }));
    if (ok) setExtra({ chavePix: '', boleto: '', codigoBanco: '', agencia: '', conta: '', nomeReferencia: '' });
  };

  const campo = (k: keyof typeof extra, rotulo: string, placeholder?: string) => (
    <Campo label={rotulo}><input className={controle} value={extra[k]} onChange={set(k)} placeholder={placeholder} /></Campo>
  );

  return (
    <Painel>
      <h3>Pagamentos</h3>
      {ordem.pagamentos.length === 0 ? <p className={dica}>Nenhum pagamento registrado.</p> : (
        <ul className={lista}>
          {ordem.pagamentos.map((p) => (
            <li key={p.id} className={itemLista}>
              <Badge>{p.tipoPagamento}</Badge> {formatarMoeda(p.valor)}
              {p.nomeReferencia && <small className="text-suave">· {p.nomeReferencia}</small>}
              <small className="ml-auto text-suave">{formatarData(p.dataCadastro)}</small>
            </li>
          ))}
        </ul>
      )}
      {podePagar && (
        <form onSubmit={pagar} className={subForm}>
          <h4>Registrar pagamento</h4>
          <div className={linhaForm}>
            <Campo label="Forma de pagamento">
              <select className={controle} value={tipo} onChange={(e) => setTipo(e.target.value as TipoPagamento)}>
                <option value="PIX">PIX</option>
                <option value="BOLETO">Boleto</option>
                <option value="TRANSFERENCIA">Transferência</option>
              </select>
            </Campo>
            <Campo label="Valor" dica={`Restante: ${formatarMoeda(ordem.valorRestante)}`}>
              <InputMoeda valor={valor} onValor={setValor} />
            </Campo>
          </div>
          <div className={linhaForm}>
            {tipo === 'PIX' && campo('chavePix', 'Chave PIX')}
            {tipo === 'BOLETO' && campo('boleto', 'Código do boleto')}
            {tipo === 'TRANSFERENCIA' && (
              <>
                {campo('codigoBanco', 'Banco', '001')}
                {campo('agencia', 'Agência', '0001')}
                {campo('conta', 'Conta')}
              </>
            )}
            {campo('nomeReferencia', 'Referência', 'Entrada, saldo...')}
          </div>
          <Botao type="submit" variante="primario" disabled={enviando}>Registrar pagamento</Botao>
        </form>
      )}
    </Painel>
  );
}

function NotasFiscais({ ordem, podeAdicionar, enviando, executar }: { ordem: OrdemDePedido; podeAdicionar: boolean; enviando: boolean; executar: Exec }) {
  const nomeArquivo = (caminho: string) => caminho.split('/').pop() ?? caminho;
  return (
    <Painel>
      <h3>Notas fiscais</h3>
      {ordem.notasFiscais.length === 0 ? <p className={dica}>Nenhuma nota fiscal anexada.</p> : (
        <ul className={lista}>
          {ordem.notasFiscais.map((n, i) => (
            <li key={n.id} className={itemLista}>
              📄 <a href={urlArquivo(n.caminho)} target="_blank" rel="noreferrer">Nota fiscal {i + 1}</a>
              <small className="text-suave" title={n.caminho}>{nomeArquivo(n.caminho).slice(-12)}</small>
              <small className="ml-auto text-suave">{formatarData(n.dataCadastro)}</small>
            </li>
          ))}
        </ul>
      )}
      {podeAdicionar && (
        <div className={subForm}>
          <UploadArquivo accept={ACEITA_DOCUMENTOS} rotulo={enviando ? 'Anexando...' : 'Anexar NF (PDF ou XML, até 10 MB)'}
            onEnviado={(a) => void executar(() => api.adicionarNotaFiscal(ordem.id, a.caminho))} />
        </div>
      )}
    </Painel>
  );
}

function Mensagens({ ordem, usuarioId, podeEnviar, enviando, executar }: {
  ordem: OrdemDePedido; usuarioId: string; podeEnviar: boolean; enviando: boolean; executar: Exec;
}) {
  const [texto, setTexto] = useState('');
  const [anexo, setAnexo] = useState<string | null>(null);
  const autor = (id: string) => (id === ordem.comprador.id ? `${ordem.comprador.nome} (comprador)` : id === ordem.vendedor.id ? `${ordem.vendedor.nome} (vendedor)` : id);
  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await executar(() => api.enviarMensagem(ordem.id, texto, anexo ?? undefined))) {
      setTexto('');
      setAnexo(null);
    }
  };
  return (
    <Painel>
      <h3>Incidentes e mensagens</h3>
      <div className="flex max-h-[360px] flex-col gap-2.5 overflow-y-auto">
        {ordem.mensagensIncidente.length === 0 && <p className={dica}>Nenhuma mensagem. Use para relatar problemas com a carga, entrega ou NF.</p>}
        {ordem.mensagensIncidente.map((m) => {
          const minha = m.usuarioCriadorId === usuarioId;
          return (
            <div key={m.id} className={cx('max-w-[75%] px-3.5 py-2.5 max-md:max-w-[90%]',
              minha ? 'self-end rounded-[12px_12px_2px_12px] bg-blue-100' : 'self-start rounded-[12px_12px_12px_2px] bg-slate-100')}>
              <small className="text-xs text-suave">{autor(m.usuarioCriadorId)} · {formatarData(m.dataCadastro)}</small>
              <p className="my-1 break-words">{m.mensagem}</p>
              {m.imagem && (
                <a href={urlArquivo(m.imagem)} target="_blank" rel="noreferrer">
                  <ImagemAnuncio caminho={m.imagem} alt="Anexo da mensagem" className="mt-1 max-h-40 max-w-[220px] rounded-lg" />
                </a>
              )}
            </div>
          );
        })}
      </div>
      {podeEnviar && (
        <form onSubmit={enviar} className={subForm}>
          <div className={linhaForm}>
            <Campo label="Mensagem">
              <input className={controle} value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Digite aqui a sua mensagem..." />
            </Campo>
            <Botao type="submit" className="mb-3" disabled={enviando || !texto.trim()}>Enviar</Botao>
          </div>
          {anexo ? (
            <div className="flex items-center gap-2.5 text-sm text-suave">
              <ImagemAnuncio caminho={anexo} alt="Anexo" compacta className="h-9 w-12 rounded-md" /> Imagem anexada
              <Botao variante="perigo" tamanho="pequeno" onClick={() => setAnexo(null)}>Remover</Botao>
            </div>
          ) : (
            <UploadArquivo accept={ACEITA_IMAGENS} compacto rotulo="Anexar foto (ex.: carga avariada)" onEnviado={(a) => setAnexo(a.caminho)} />
          )}
        </form>
      )}
    </Painel>
  );
}
