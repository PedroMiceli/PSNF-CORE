import { useCallback, useEffect, useRef, useState, type FormEvent, type ReactNode } from 'react';
import { Link, useParams } from 'react-router-dom';
import { urlArquivo } from '../api/client';
import { api } from '../api/endpoints';
import type { OrdemDePedido, Pagamento, StatusOrdem, TipoPagamento } from '../api/types';
import { useAcao, useApi } from '../api/useApi';
import { CampoCopiavel } from '../components/CampoCopiavel';
import { DadosNotaFiscal, resumoItem } from '../components/DadosNotaFiscal';
import { Dialogo } from '../components/Dialogo';
import { formatarData, formatarMoeda, STATUS_LABEL } from '../components/format';
import { ACEITA_DOCUMENTOS, ACEITA_IMAGENS, UploadArquivo } from '../components/UploadArquivo';
import {
  acoes, Badge, Botao, Campo, Carregando, controle, cx, dica, ErroBox, ImagemAnuncio, InputMoeda, linhaForm, Painel, PagamentoPendenteBadge, pagamentoAtrasadoNoFluxo, StatusBadge,
  tabela, TituloPagina, Voltar,
} from '../components/ui';
import { useAlertas } from '../context/AlertasContext';
import { useUsuario } from '../context/UsuarioContext';

const FLUXO: StatusOrdem[] = ['AGUARDANDO_CONFIRMACAO', 'ACEITO', 'AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'];

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
  const { definirOrdemAberta, recarregar: recarregarAlertas } = useAlertas();
  // mensagens que chegaram sem o usuário ter visto: ficam destacadas enquanto ele está na página
  const [novas, setNovas] = useState<Set<string>>(new Set());
  const qtdMensagens = useRef(0);

  // esta ordem está na tela: o aviso no canto não aparece para ela
  useEffect(() => { definirOrdemAberta(id); return () => definirOrdemAberta(null); }, [id, definirOrdemAberta]);

  // separa as mensagens novas (as últimas do outro lado, segundo a API) e acompanha a quantidade
  useEffect(() => {
    const d = ordem.data;
    if (!d) return;
    qtdMensagens.current = d.mensagensIncidente.length;
    if (d.mensagensNaoLidas > 0) {
      const ids = d.mensagensIncidente.filter((m) => m.usuarioCriadorId !== usuarioId).slice(-d.mensagensNaoLidas).map((m) => m.id);
      setNovas((atual) => (ids.every((x) => atual.has(x)) ? atual : new Set([...atual, ...ids])));
    }
  }, [ordem.data, usuarioId]);

  // mensagens novas aparecem sem recarregar a página
  useEffect(() => {
    const t = setInterval(() => {
      if (document.hidden) return;
      api.ordem(id).then((n) => { if (n.mensagensIncidente.length !== qtdMensagens.current) ordem.setData(n); }).catch(() => {});
    }, 15000);
    return () => clearInterval(t);
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  // vindo do aviso ou da lista (#mensagens): rola até os incidentes
  useEffect(() => {
    if (ordem.data && window.location.hash === '#mensagens') {
      setTimeout(() => document.getElementById('mensagens')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 100);
    }
  }, [!!ordem.data]); // eslint-disable-line react-hooks/exhaustive-deps

  const marcarLidas = useCallback(() => {
    api.marcarMensagensLidas(id).then((r) => { ordem.setData(r); recarregarAlertas(); }).catch(() => {});
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

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
        <h1 className="break-words">Ordem de pedido {o.codigo} <StatusBadge status={o.status} />{' '}
          <PagamentoPendenteBadge status={o.status} pendente={o.pagamentoPendente} /></h1>
        <span className="text-suave">Você é o <strong>{souComprador ? 'comprador' : souVendedor ? 'vendedor' : 'observador'}</strong></span>
      </TituloPagina>

      {o.mensagensNaoLidas > 0 && (
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-900" role="status">
          <span>
            💬 <strong>{o.mensagensNaoLidas === 1 ? '1 mensagem nova' : `${o.mensagensNaoLidas} mensagens novas`}</strong> de{' '}
            {souComprador ? o.vendedor.nome : o.comprador.nome} em Incidentes e mensagens.
          </span>
          <Botao tamanho="pequeno" variante="primario"
            onClick={() => document.getElementById('mensagens')?.scrollIntoView({ behavior: 'smooth', block: 'start' })}>
            Ver mensagens
          </Botao>
        </div>
      )}
      <LinhaDoTempo status={o.status} pagamentoPendente={o.pagamentoPendente} />
      <AvisoPagamentoPendente ordem={o} souComprador={souComprador} />
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
            <Valor rotulo="Em formas de pagamento">{formatarMoeda(o.valorCadastrado)}</Valor>
            <Valor rotulo="Pago (com comprovante)" className="text-verde">{formatarMoeda(o.valorPago)}</Valor>
            {o.valorRestante > 0 && !['CANCELADO'].includes(o.status)
              ? <Valor rotulo="Falta cadastrar" className="text-laranja">{formatarMoeda(o.valorRestante)}</Valor>
              : <Valor rotulo="A pagar" className={o.valorTotal - o.valorPago > 0 ? 'text-laranja' : ''}>{formatarMoeda(o.valorTotal - o.valorPago)}</Valor>}
          </div>
        </Painel>
      </div>

      <Acoes ordem={o} souComprador={souComprador} souVendedor={souVendedor} enviando={acao.enviando} executar={executar} />

      <PainelEntrega ordem={o} souVendedor={souVendedor} enviando={acao.enviando} executar={executar} />

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

      <DadosNotaFiscal ordem={o} aberto={souVendedor && ['AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)} />

      <div className={grade2}>
        <Pagamentos ordem={o} podeCadastrar={souVendedor && o.status === 'ACEITO'}
          podeExcluir={souVendedor && ['ACEITO', 'AGUARDANDO_PAGAMENTO'].includes(o.status)}
          podeAnexar={souComprador && ['ACEITO', 'AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)}
          souVendedor={souVendedor} enviando={acao.enviando} executar={executar} />
        <NotasFiscais ordem={o} podeAdicionar={souVendedor && ['AGUARDANDO_PAGAMENTO', 'PAGO', 'ENVIADO', 'ENTREGUE'].includes(o.status)} enviando={acao.enviando} executar={executar} />
      </div>

      <Mensagens ordem={o} usuarioId={usuarioId} podeEnviar={souComprador || souVendedor} enviando={acao.enviando} executar={executar}
        novas={novas} onVisto={marcarLidas} />
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

/**
 * Aviso fixo quando o pedido seguiu (enviado/entregue) sem estar todo pago, ex.: venda a prazo.
 * Fica até o comprador anexar todos os comprovantes.
 */
function AvisoPagamentoPendente({ ordem, souComprador }: { ordem: OrdemDePedido; souComprador: boolean }) {
  if (!pagamentoAtrasadoNoFluxo(ordem.status, ordem.pagamentoPendente)) return null;
  const semComprovante = ordem.pagamentos.filter((p) => !p.comprovante);
  return (
    <div className="mb-4 flex flex-wrap items-center gap-x-4 gap-y-1 rounded-lg border border-orange-300 bg-orange-50 px-4 py-3 text-orange-900" role="status">
      <strong>⚠ Pagamento pendente:</strong>
      <span>
        o pedido foi {ordem.status === 'ENTREGUE' ? 'entregue' : 'enviado'}, mas ainda faltam{' '}
        <strong>{formatarMoeda(ordem.valorTotal - ordem.valorPago)}</strong> ({semComprovante.length} pagamento(s) sem comprovante).
        {souComprador ? ' Anexe os comprovantes em "Formas de pagamento" assim que pagar.' : ' Aguardando o comprador pagar e anexar os comprovantes.'}
      </span>
    </div>
  );
}

function LinhaDoTempo({ status, pagamentoPendente }: { status: StatusOrdem; pagamentoPendente: boolean }) {
  if (status === 'CANCELADO') {
    return <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 font-semibold text-vermelho">Esta ordem foi cancelada.</div>;
  }
  const atual = FLUXO.indexOf(status);
  return (
    <ol className="mb-4 grid gap-2 sm:grid-cols-3 lg:grid-cols-6">
      {FLUXO.map((s, i) => {
        // o pedido passou da etapa "Pago" sem estar pago (venda a prazo): a etapa fica em aberto, em laranja
        const pagoPendente = s === 'PAGO' && i < atual && pagamentoPendente;
        // "Entregue" é a etapa final: aparece concluída (verde), não como passo em andamento (azul)
        const final = status === 'ENTREGUE' && i === atual;
        const feito = (i < atual && !pagoPendente) || final;
        const ehAtual = i === atual && !final;
        return (
          <li key={s} aria-current={ehAtual || final ? 'step' : undefined}
            className={cx('flex items-center gap-2 rounded-lg border p-2.5 text-sm leading-tight',
              feito && 'border-green-200 bg-green-50 text-verde',
              final && 'border-green-500 font-bold',
              ehAtual && 'border-primaria bg-blue-50 font-bold text-primaria',
              pagoPendente && 'border-orange-300 bg-orange-50 font-semibold text-orange-800',
              !feito && !ehAtual && !pagoPendente && 'border-borda bg-white text-suave')}>
            <span className={cx('inline-flex size-6 shrink-0 items-center justify-center rounded-full text-xs',
              feito ? 'bg-verde text-white' : ehAtual ? 'bg-primaria text-white' : pagoPendente ? 'bg-orange-500 text-white' : 'bg-gray-200 text-suave')}>
              {feito ? '✔' : pagoPendente ? '!' : i + 1}
            </span>
            {pagoPendente ? 'Pagamento pendente' : STATUS_LABEL[s]}
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
  const [entrega, setEntrega] = useState({ descricao: ordem.entrega?.descricao ?? '', codigoRastreio: ordem.entrega?.codigoRastreio ?? '' });
  const st = ordem.status;
  const podeCancelar = (souComprador || souVendedor) && ['AGUARDANDO_CONFIRMACAO', 'ACEITO', 'AGUARDANDO_PAGAMENTO', 'PAGO'].includes(st);
  const podeAceitar = souComprador && st === 'AGUARDANDO_CONFIRMACAO';
  // também com pagamento pendente (ex.: venda a prazo): o comprador paga depois de receber
  const podeDespachar = souVendedor && (st === 'PAGO' || st === 'AGUARDANDO_PAGAMENTO');
  const faltaPagar = ordem.valorTotal - ordem.valorPago;
  const podeConfirmar = souComprador && st === 'ENVIADO';
  if (!podeCancelar && !podeDespachar && !podeConfirmar) return null;

  // o que cada um precisa fazer agora
  const orientacao = souComprador ? ({
    AGUARDANDO_CONFIRMACAO: 'Confira se os produtos, as quantidades e os valores estão corretos. Se estiver tudo certo, aceite a ordem; se não, converse com o vendedor ou cancele.',
    ACEITO: 'Aguardando o vendedor cadastrar as formas de pagamento combinadas.',
    AGUARDANDO_PAGAMENTO: 'Faça os pagamentos e anexe o comprovante de cada um em "Formas de pagamento".',
    PAGO: 'Pagamento concluído. Aguardando o vendedor despachar o pedido.',
  } as Partial<Record<StatusOrdem, string>>)[st] : ({
    AGUARDANDO_CONFIRMACAO: 'Aguardando o comprador conferir os produtos e os valores e aceitar a ordem.',
    ACEITO: 'O comprador aceitou. Cadastre as formas de pagamento combinadas (ex.: parte no PIX, parte no boleto) até cobrir o valor total.',
    AGUARDANDO_PAGAMENTO: 'Aguardando o comprador pagar e anexar os comprovantes. Se a venda for a prazo, você já pode despachar o pedido.',
    PAGO: 'Todos os comprovantes foram enviados. Confira os pagamentos e despache o pedido.',
  } as Partial<Record<StatusOrdem, string>>)[st];

  return (
    <Painel className="border-blue-200 bg-blue-50/40">
      <h3>Próximo passo</h3>
      {orientacao && <p className={cx(dica, 'mt-0')}>{orientacao}</p>}
      {podeDespachar && st === 'AGUARDANDO_PAGAMENTO' && (
        <p className="mt-0 mb-3 rounded-lg border border-orange-300 bg-orange-50 px-3 py-2 text-sm text-orange-900">
          ⚠ Ainda faltam <strong>{formatarMoeda(faltaPagar)}</strong> em pagamentos. Ao despachar agora, o pedido segue e a ordem
          continua marcada com <strong>pagamento pendente</strong> até o comprador anexar todos os comprovantes.
        </p>
      )}
      <div className={acoes}>
        {podeAceitar && (
          <Botao variante="primario" disabled={enviando}
            onClick={() => window.confirm('Confirma que os produtos, as quantidades e os valores estão corretos?') && executar(() => api.aceitarOrdem(ordem.id))}>
            ✔ Aceitar ordem
          </Botao>
        )}
        {podeDespachar && (
          <>
            <div className="w-full">
              <p className="mt-0 mb-2 text-sm font-semibold">Despacho <small className="font-normal text-suave">(os dados da entrega são opcionais e podem ser editados depois)</small></p>
              <div className={cx(linhaForm, 'items-start')}>
                <Campo label="Código de envio / rastreio" className="max-w-[320px]">
                  <input className={controle} value={entrega.codigoRastreio} maxLength={100} placeholder="Ex.: BR123456789BR"
                    onChange={(e) => setEntrega({ ...entrega, codigoRastreio: e.target.value })} />
                </Campo>
                <Campo label="Detalhes da entrega" className="grow-2 basis-80">
                  <textarea className={controle} rows={2} maxLength={2000} value={entrega.descricao}
                    placeholder="Transportadora, horário, quem recebe, instruções..."
                    onChange={(e) => setEntrega({ ...entrega, descricao: e.target.value })} />
                </Campo>
              </div>
            </div>
            <Campo label="Previsão de entrega" className="mb-0 max-w-[260px]">
              <input className={controle} type="date" value={previsao} min={new Date().toISOString().slice(0, 10)}
                onChange={(e) => setPrevisao(e.target.value)} />
            </Campo>
            <Botao variante="primario" disabled={enviando} onClick={() => {
              const ok = st === 'PAGO' || window.confirm(
                `Ainda faltam ${formatarMoeda(faltaPagar)} em pagamentos. Despachar mesmo assim (ex.: venda a prazo)? A ordem continuará indicando o pagamento pendente.`);
              if (ok) void executar(() => api.despachar(ordem.id, previsao || null, entrega));
            }}>
              {st === 'PAGO' ? 'Pedido despachado' : 'Despachar com pagamento pendente'}
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
    </Painel>
  );
}

/** "yyyy-MM-dd" de hoje no fuso do navegador (toISOString usaria UTC). */
const hojeLocal = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const TIPO_LABEL: Record<TipoPagamento, string> = { PIX: 'PIX', BOLETO: 'Boleto', TRANSFERENCIA: 'Transferência' };

/**
 * Dados da entrega (descrição e código de envio, ambos opcionais). O vendedor informa ao despachar ou aqui,
 * a qualquer momento depois do aceite; o comprador acompanha.
 */
function PainelEntrega({ ordem, souVendedor, enviando, executar }: {
  ordem: OrdemDePedido; souVendedor: boolean; enviando: boolean; executar: Exec;
}) {
  const e = ordem.entrega;
  const enviado = ['ENVIADO', 'ENTREGUE'].includes(ordem.status);
  const podeEditar = souVendedor && !['AGUARDANDO_CONFIRMACAO', 'CANCELADO'].includes(ordem.status);
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState({ descricao: '', codigoRastreio: '' });

  // só aparece quando faz sentido: já tem dados, o pedido saiu, ou o vendedor pode adiantar
  if (!e && !enviado && !(podeEditar && ['PAGO'].includes(ordem.status))) return null;

  const abrirEdicao = () => { setForm({ descricao: e?.descricao ?? '', codigoRastreio: e?.codigoRastreio ?? '' }); setEditando(true); };
  const salvar = async (ev: FormEvent) => {
    ev.preventDefault();
    if (await executar(() => api.registrarEntrega(ordem.id, { descricao: form.descricao || null, codigoRastreio: form.codigoRastreio || null }))) {
      setEditando(false);
    }
  };

  return (
    <Painel id="entrega" aria-labelledby="entrega-titulo">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 id="entrega-titulo" className="m-0">🚚 Entrega</h3>
        {podeEditar && !editando && (
          <Botao tamanho="pequeno" onClick={abrirEdicao}>{e ? 'Editar dados da entrega' : '+ Informar dados da entrega'}</Botao>
        )}
      </div>
      {editando ? (
        <form onSubmit={salvar} className="mt-3">
          <div className={cx(linhaForm, 'items-start')}>
            <Campo label="Código de envio / rastreio" className="max-w-[320px]">
              <input className={controle} value={form.codigoRastreio} maxLength={100} placeholder="Ex.: BR123456789BR"
                onChange={(ev) => setForm({ ...form, codigoRastreio: ev.target.value })} />
            </Campo>
            <Campo label="Detalhes da entrega" className="grow-2 basis-80">
              <textarea className={controle} rows={3} maxLength={2000} value={form.descricao}
                placeholder="Transportadora, horário, quem recebe, instruções..."
                onChange={(ev) => setForm({ ...form, descricao: ev.target.value })} />
            </Campo>
          </div>
          <p className={cx(dica, 'mt-0')}>Os dois campos são opcionais. Deixe os dois vazios para remover os dados da entrega.</p>
          <div className={acoes}>
            <Botao type="submit" variante="primario" disabled={enviando}>Salvar</Botao>
            <Botao onClick={() => setEditando(false)}>Cancelar</Botao>
          </div>
        </form>
      ) : (
        <div className="mt-3">
          <dl className="m-0 mb-3 grid grid-cols-[max-content_minmax(0,1fr)] gap-x-4 gap-y-1.5 text-sm">
            <dt className="text-suave">Previsão de entrega</dt><dd className="m-0">{formatarData(ordem.previsaoDeEntrega)}</dd>
          </dl>
          {e?.codigoRastreio && <div className="max-w-[420px]"><CampoCopiavel rotulo="Código de envio / rastreio" valor={e.codigoRastreio} mono /></div>}
          {e?.descricao && (
            <div className="mb-1">
              <span className="mb-1 block text-xs font-semibold text-gray-700">Detalhes da entrega</span>
              <p className="m-0 rounded-lg bg-gray-50 px-3 py-2 text-sm whitespace-pre-wrap break-words">{e.descricao}</p>
            </div>
          )}
          {!e && <p className={cx(dica, 'mb-0')}>{souVendedor ? 'Informe o código de envio ou detalhes da entrega para o comprador acompanhar.' : 'O vendedor ainda não informou código de envio nem detalhes da entrega.'}</p>}
          {e?.dataAlteracao && <p className={cx(dica, 'mb-0')}>Atualizado em {formatarData(e.dataAlteracao)}.</p>}
        </div>
      )}
    </Painel>
  );
}

/** Selo do pagamento: comprovante enviado ou pendente. */
function SituacaoPagamento({ p }: { p: Pagamento }) {
  return p.comprovante
    ? <span className="rounded-full bg-green-100 px-2 py-0.5 text-[0.7rem] font-bold text-green-800">✔ Comprovante enviado</span>
    : <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[0.7rem] font-bold text-amber-800">Aguardando comprovante</span>;
}

/**
 * Todos os dados de um pagamento. Chave PIX, boleto e dados bancários são copiados com um clique.
 * O comprador anexa (ou troca) o comprovante por aqui.
 */
function DetalhesPagamento({ ordem, pagamento: p, onFechar, podeExcluir, podeAnexar, enviando, onExcluir, executar }: {
  ordem: OrdemDePedido; pagamento: Pagamento | null; onFechar: () => void; podeExcluir: boolean; podeAnexar: boolean;
  enviando: boolean; onExcluir: (p: Pagamento) => void; executar: Exec;
}) {
  return (
    <Dialogo aberto={!!p} onFechar={onFechar} titulo={p ? `Pagamento via ${TIPO_LABEL[p.tipoPagamento]}` : ''}
      rodape={p && (
        <div className="flex flex-wrap items-center justify-between gap-2">
          {podeExcluir && !p.comprovante
            ? <Botao variante="perigo" tamanho="pequeno" disabled={enviando} onClick={() => onExcluir(p)}>Excluir forma de pagamento</Botao>
            : <span />}
          <Botao onClick={onFechar}>Fechar</Botao>
        </div>
      )}>
      {p && (
        <>
          <div className="mb-4 flex flex-wrap items-end justify-between gap-3 rounded-lg bg-green-50 px-4 py-3">
            <div>
              <span className="block text-xs text-suave">Valor</span>
              <strong className="text-2xl text-verde">{formatarMoeda(p.valor)}</strong>
            </div>
            <div className="text-right">
              <span className="block text-xs text-suave">Data para pagamento</span>
              <strong>{formatarData(p.dataReferencia ?? p.dataCadastro)}</strong>
            </div>
          </div>
          {p.tipoPagamento === 'PIX' && p.chavePix && <CampoCopiavel rotulo="Chave PIX" valor={p.chavePix} />}
          {p.tipoPagamento === 'BOLETO' && p.boleto && <CampoCopiavel rotulo="Código do boleto" valor={p.boleto} mono />}
          {p.tipoPagamento === 'TRANSFERENCIA' && (
            <div className="grid gap-x-3 sm:grid-cols-2">
              {p.codigoBanco && <CampoCopiavel rotulo="Banco" valor={p.codigoBanco} mono />}
              {p.agencia && <CampoCopiavel rotulo="Agência" valor={p.agencia} mono />}
              {p.conta && <div className="sm:col-span-2"><CampoCopiavel rotulo="Conta" valor={p.conta} mono /></div>}
            </div>
          )}

          {/* comprovante */}
          <div className={cx('mb-3 rounded-lg border px-3 py-2.5', p.comprovante ? 'border-green-200 bg-green-50/50' : 'border-amber-200 bg-amber-50/50')}>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <strong className="text-sm">Comprovante</strong>
              <SituacaoPagamento p={p} />
            </div>
            {p.comprovante && (
              <p className="m-0 mt-1.5 text-sm">
                📄 <a href={urlArquivo(p.comprovante)} target="_blank" rel="noreferrer">Ver comprovante</a>
                {p.dataComprovante && <small className="ml-2 text-suave">enviado em {formatarData(p.dataComprovante)}</small>}
              </p>
            )}
            {podeAnexar && (
              <UploadArquivo accept={ACEITA_COMPROVANTE} compacto
                rotulo={enviando ? 'Enviando...' : p.comprovante ? 'Trocar comprovante' : 'Anexar comprovante (imagem ou PDF)'}
                onEnviado={(a) => void executar(() => api.anexarComprovante(ordem.id, p.id, a.caminho))} />
            )}
            {!p.comprovante && !podeAnexar && <p className="m-0 mt-1 text-xs text-suave">O comprador anexa o comprovante depois de pagar.</p>}
          </div>

          <dl className="m-0 grid grid-cols-[max-content_minmax(0,1fr)] gap-x-4 gap-y-1.5 text-sm">
            {p.nomeReferencia && (<><dt className="text-suave">Referência</dt><dd className="m-0 break-words">{p.nomeReferencia}</dd></>)}
            <dt className="text-suave">Cadastrado em</dt><dd className="m-0">{formatarData(p.dataCadastro)}</dd>
          </dl>
          <p className={cx(dica, 'mb-0')}>Clique na chave, no código ou nos dados bancários para copiar.</p>
        </>
      )}
    </Dialogo>
  );
}

/** Comprovante: foto ou PDF. */
const ACEITA_COMPROVANTE = `${ACEITA_IMAGENS},application/pdf`;

type CampoPagamento = 'valor' | 'dataReferencia' | 'chavePix' | 'boleto' | 'codigoBanco' | 'agencia' | 'conta';

/** Campos obrigatórios de cada forma de pagamento (os mesmos que a API exige). */
const OBRIGATORIOS: Record<TipoPagamento, [CampoPagamento, string][]> = {
  PIX: [['chavePix', 'Informe a chave PIX']],
  BOLETO: [['boleto', 'Informe o código do boleto']],
  TRANSFERENCIA: [['codigoBanco', 'Informe o banco'], ['agencia', 'Informe a agência'], ['conta', 'Informe a conta']],
};

/**
 * Formas de pagamento da ordem: o VENDEDOR cadastra o que foi combinado (com os dados para o comprador pagar);
 * o COMPRADOR paga e anexa o comprovante de cada uma.
 */
function Pagamentos({ ordem, podeCadastrar, podeExcluir, podeAnexar, souVendedor, enviando, executar }: {
  ordem: OrdemDePedido; podeCadastrar: boolean; podeExcluir: boolean; podeAnexar: boolean; souVendedor: boolean;
  enviando: boolean; executar: Exec;
}) {
  const [tipo, setTipo] = useState<TipoPagamento>('PIX');
  const [detalheId, setDetalheId] = useState<string | null>(null);
  const detalhe = ordem.pagamentos.find((p) => p.id === detalheId) ?? null; // sempre a versão mais recente
  const fecharDetalhe = useCallback(() => setDetalheId(null), []);
  const [dataReferencia, setDataReferencia] = useState(hojeLocal());
  const [valor, setValor] = useState(ordem.valorRestante.toFixed(2));
  const vazio = { chavePix: '', boleto: '', codigoBanco: '', agencia: '', conta: '', nomeReferencia: '' };
  const [extra, setExtra] = useState(vazio);
  const [erros, setErros] = useState<Partial<Record<CampoPagamento, string>>>({});
  const set = (k: keyof typeof extra) => (e: { target: { value: string } }) => {
    setExtra({ ...extra, [k]: e.target.value });
    setErros((atual) => ({ ...atual, [k]: undefined })); // some o aviso ao começar a preencher
  };

  // o valor sugerido acompanha o que falta cadastrar
  useEffect(() => { setValor(ordem.valorRestante.toFixed(2)); }, [ordem.valorRestante]);

  const validar = () => {
    const e: Partial<Record<CampoPagamento, string>> = {};
    const v = Number(valor);
    if (!dataReferencia) e.dataReferencia = 'Informe a data para pagamento';
    if (!valor || v <= 0) e.valor = 'Informe o valor';
    else if (v > ordem.valorRestante) e.valor = `O valor não pode passar do que falta cadastrar (${formatarMoeda(ordem.valorRestante)})`;
    OBRIGATORIOS[tipo].forEach(([campo, msg]) => { if (!extra[campo as keyof typeof extra].trim()) e[campo] = msg; });
    setErros(e);
    return Object.keys(e).length === 0;
  };

  const cadastrar = async (e: FormEvent) => {
    e.preventDefault();
    if (!validar()) return;
    const ok = await executar(() => api.pagar(ordem.id, { tipoPagamento: tipo, valor: Number(valor), dataReferencia, ...extra }));
    if (ok) { setExtra(vazio); setErros({}); setDataReferencia(hojeLocal()); }
  };

  const excluir = (p: Pagamento) => {
    if (window.confirm(`Excluir a forma de pagamento de ${formatarMoeda(p.valor)} (${TIPO_LABEL[p.tipoPagamento]})?`)) {
      void executar(() => api.removerPagamento(ordem.id, p.id));
    }
  };

  const campo = (k: Exclude<CampoPagamento, 'valor' | 'dataReferencia'> | 'nomeReferencia', rotulo: string, placeholder?: string) => (
    <Campo label={rotulo + (k !== 'nomeReferencia' ? ' *' : '')} erro={k !== 'nomeReferencia' ? erros[k] : undefined}>
      <input className={controle} value={extra[k]} onChange={set(k)} placeholder={placeholder}
        aria-required={k !== 'nomeReferencia'} />
    </Campo>
  );

  const avisos = Object.values(erros).filter(Boolean);
  const pendentes = ordem.pagamentos.filter((p) => !p.comprovante).length;

  return (
    <Painel>
      <h3>Formas de pagamento</h3>
      {ordem.pagamentos.length === 0 ? (
        <p className={dica}>
          {['AGUARDANDO_CONFIRMACAO'].includes(ordem.status)
            ? 'As formas de pagamento são cadastradas pelo vendedor depois que o comprador aceitar a ordem.'
            : 'Nenhuma forma de pagamento cadastrada.'}
        </p>
      ) : (
        <ul className={lista}>
          {ordem.pagamentos.map((p) => (
            <li key={p.id} className="flex items-center gap-1 py-1">
              {/* a linha inteira abre os detalhes */}
              <button type="button" onClick={() => setDetalheId(p.id)} title="Clique para ver todos os dados do pagamento"
                aria-label={`Ver detalhes do pagamento de ${formatarMoeda(p.valor)}`}
                className="group -mx-2 flex min-w-0 flex-1 cursor-pointer flex-wrap items-center gap-2 rounded-lg px-2 py-1.5 text-left hover:bg-blue-50">
                <Badge>{TIPO_LABEL[p.tipoPagamento]}</Badge> <strong className="font-semibold">{formatarMoeda(p.valor)}</strong>
                {p.nomeReferencia && <small className="text-suave">· {p.nomeReferencia}</small>}
                <SituacaoPagamento p={p} />
                <small className="ml-auto text-suave">📅 {formatarData(p.dataReferencia ?? p.dataCadastro)}</small>
                <span className="text-xs font-semibold text-primaria opacity-0 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100 max-sm:opacity-100"
                  aria-hidden="true">{podeAnexar && !p.comprovante ? 'Pagar e anexar ›' : 'Ver detalhes ›'}</span>
              </button>
              {podeExcluir && !p.comprovante && (
                <Botao variante="perigo" tamanho="pequeno" disabled={enviando} onClick={() => excluir(p)}
                  aria-label={`Excluir pagamento de ${formatarMoeda(p.valor)}`}>Excluir</Botao>
              )}
            </li>
          ))}
        </ul>
      )}
      {podeAnexar && pendentes > 0 && ordem.valorRestante === 0 && (
        <p className={cx(dica, 'mb-0')}>Clique em cada pagamento para ver os dados (chave PIX, boleto ou conta) e anexar o comprovante.</p>
      )}
      {souVendedor && podeExcluir && ordem.pagamentos.length > 0 && (
        <p className={cx(dica, 'mb-0')}>Cadastrou algo errado? Exclua e cadastre de novo (enquanto o comprador não anexar o comprovante).</p>
      )}

      {podeCadastrar && (
        <form onSubmit={cadastrar} className={subForm} noValidate>
          <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
            <h4 className="m-0">Cadastrar forma de pagamento</h4>
            <span className="rounded-full border border-orange-200 bg-orange-50 px-3 py-1 text-sm text-laranja">
              Falta cadastrar <strong className="text-base">{formatarMoeda(ordem.valorRestante)}</strong>
            </span>
          </div>
          <p className={cx(dica, 'mt-0')}>Informe os dados que o comprador vai usar para pagar: sua chave PIX, o código do boleto ou a conta para transferência.</p>
          {avisos.length > 0 && (
            <div className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-vermelho" role="alert">
              Preencha os campos obrigatórios: {avisos.join(' · ')}.
            </div>
          )}
          <div className={linhaForm}>
            <Campo label="Forma de pagamento">
              <select className={controle} value={tipo} onChange={(e) => { setTipo(e.target.value as TipoPagamento); setErros({}); }}>
                <option value="PIX">PIX</option>
                <option value="BOLETO">Boleto</option>
                <option value="TRANSFERENCIA">Transferência</option>
              </select>
            </Campo>
            <Campo label="Valor *" erro={erros.valor}>
              <InputMoeda valor={valor} onValor={(v) => { setValor(v); setErros((a) => ({ ...a, valor: undefined })); }} />
            </Campo>
            <Campo label="Data para pagamento *" erro={erros.dataReferencia}>
              <input className={controle} type="date" value={dataReferencia} title="Quando o comprador deve pagar (vencimento)"
                onChange={(e) => { setDataReferencia(e.target.value); setErros((a) => ({ ...a, dataReferencia: undefined })); }} />
            </Campo>
          </div>
          <div className={linhaForm}>
            {tipo === 'PIX' && campo('chavePix', 'Chave PIX', 'CPF, CNPJ, e-mail, telefone ou chave aleatória')}
            {tipo === 'BOLETO' && campo('boleto', 'Código do boleto', 'Linha digitável')}
            {tipo === 'TRANSFERENCIA' && (
              <>
                {campo('codigoBanco', 'Banco', '001')}
                {campo('agencia', 'Agência', '0001')}
                {campo('conta', 'Conta')}
              </>
            )}
            {campo('nomeReferencia', 'Referência', 'Entrada, parcela 2...')}
          </div>
          <Botao type="submit" variante="primario" disabled={enviando}>Cadastrar forma de pagamento</Botao>
        </form>
      )}
      <DetalhesPagamento ordem={ordem} pagamento={detalhe} onFechar={fecharDetalhe} podeExcluir={podeExcluir} podeAnexar={podeAnexar}
        enviando={enviando} executar={executar} onExcluir={(p) => { setDetalheId(null); excluir(p); }} />
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

function Mensagens({ ordem, usuarioId, podeEnviar, enviando, executar, novas, onVisto }: {
  ordem: OrdemDePedido; usuarioId: string; podeEnviar: boolean; enviando: boolean; executar: Exec;
  /** ids das mensagens que chegaram sem o usuário ter visto */
  novas: Set<string>;
  /** chamado quando o usuário vê a seção com mensagens não lidas */
  onVisto: () => void;
}) {
  const secao = useRef<HTMLDivElement>(null);
  const temNaoLidas = ordem.mensagensNaoLidas > 0;

  // marca como lidas quando a seção aparece na tela
  useEffect(() => {
    const el = secao.current;
    if (!el || !temNaoLidas) return;
    const obs = new IntersectionObserver((e) => { if (e.some((x) => x.isIntersecting)) { obs.disconnect(); onVisto(); } }, { threshold: 0.3 });
    obs.observe(el);
    return () => obs.disconnect();
  }, [temNaoLidas, onVisto]);

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
  const qtdNovas = ordem.mensagensIncidente.filter((m) => novas.has(m.id)).length;
  return (
    <Painel id="mensagens" ref={secao}
      className={cx('scroll-mt-20 transition-shadow', qtdNovas > 0 && 'border-red-300 ring-2 ring-red-200')}>
      <h3 className="flex flex-wrap items-center gap-2">
        Incidentes e mensagens
        {qtdNovas > 0 && (
          <span className="rounded-full bg-vermelho px-2 py-0.5 text-xs font-bold text-white">
            {qtdNovas === 1 ? '1 nova' : `${qtdNovas} novas`}
          </span>
        )}
      </h3>
      <div className="flex max-h-[360px] flex-col gap-2.5 overflow-y-auto">
        {ordem.mensagensIncidente.length === 0 && <p className={dica}>Nenhuma mensagem. Use para relatar problemas com a carga, entrega ou NF.</p>}
        {ordem.mensagensIncidente.map((m) => {
          const minha = m.usuarioCriadorId === usuarioId;
          const nova = novas.has(m.id);
          return (
            <div key={m.id} className={cx('max-w-[75%] px-3.5 py-2.5 max-md:max-w-[90%]',
              minha ? 'self-end rounded-[12px_12px_2px_12px] bg-blue-100' : 'self-start rounded-[12px_12px_12px_2px] bg-slate-100',
              nova && 'bg-red-50 ring-2 ring-red-300')}>
              <small className="text-xs text-suave">
                {nova && <span className="mr-1.5 rounded bg-vermelho px-1.5 py-px text-[0.65rem] font-bold text-white uppercase">Nova</span>}
                {autor(m.usuarioCriadorId)} · {formatarData(m.dataCadastro)}
              </small>
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
