import { Link } from 'react-router-dom';
import type { Anuncio } from '../api/types';
import { Estrelas, formatarNota } from './Estrelas';
import { formatarMoeda } from './format';
import { Badge, BADGE_CNPJ, Botao, ImagemAnuncio } from './ui';

const cartao = 'overflow-hidden rounded-card border border-borda bg-white text-inherit';
const elevar = 'transition hover:shadow-[0_8px_24px_rgba(15,23,42,0.08)]';

/** Imagem, categoria, título, nota e preço: o miolo do cartão da vitrine e da página do vendedor. */
function Conteudo({ anuncio: a, categoria, mostrarSeloCnpj }: { anuncio: Anuncio; categoria: string; mostrarSeloCnpj: boolean }) {
  const principal = a.imagens.find((i) => i.principal) ?? a.imagens[0];
  return (
    <>
      <ImagemAnuncio caminho={principal?.caminho} alt={a.titulo} className="h-[170px] w-full" />
      <div className="p-3.5">
        <span className="flex items-center justify-between gap-2">
          <span className="text-[0.72rem] tracking-wider text-suave uppercase">{categoria}</span>
          {a.exigeCnpj && mostrarSeloCnpj && <Badge className={BADGE_CNPJ + ' shrink-0 whitespace-nowrap'}>Somente CNPJ</Badge>}
        </span>
        <h3 className="mt-1 mb-1 text-base">{a.titulo}</h3>
        {a.quantidadeAvaliacoes > 0 && (
          <p className="mb-1 flex items-center gap-1 text-xs">
            <Estrelas nota={a.notaMedia ?? 0} tamanho="text-sm" />
            <strong>{formatarNota(a.notaMedia)}</strong> <span className="text-suave">({a.quantidadeAvaliacoes})</span>
          </p>
        )}
        <p className="text-lg font-extrabold text-verde">
          <small className="text-[0.72rem] font-medium text-suave">a partir de</small> {formatarMoeda(a.precoMenor)}
        </p>
        <p className="mt-1 text-xs text-suave">
          {a.valores.length} faixa(s) de preço · {a.variacoes.length} variação(ões)
        </p>
      </div>
    </>
  );
}

interface Props {
  anuncio: Anuncio;
  /** nome da categoria (vem da lista de categorias carregada pela página) */
  categoria: string;
  /** o selo "Somente CNPJ" só interessa a quem não tem CNPJ */
  mostrarSeloCnpj: boolean;
  /** sem login o cartão não abre o anúncio: chama esta função (a vitrine mostra o aviso para entrar) */
  aoClicarSemLogin?: () => void;
}

/** Cartão de anúncio: logado abre o anúncio; sem login (aoClicarSemLogin) só avisa. */
export function CartaoAnuncio({ anuncio, categoria, mostrarSeloCnpj, aoClicarSemLogin }: Props) {
  const conteudo = <Conteudo anuncio={anuncio} categoria={categoria} mostrarSeloCnpj={mostrarSeloCnpj} />;
  if (aoClicarSemLogin) {
    return (
      <div role="button" tabIndex={0} aria-label={`${anuncio.titulo}: entre para ver os detalhes`}
        className={`${cartao} ${elevar} cursor-pointer`}
        onClick={aoClicarSemLogin}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); aoClicarSemLogin(); } }}>
        {conteudo}
      </div>
    );
  }
  return (
    <Link to={`/anuncios/${anuncio.id}`} className={`${cartao} ${elevar} hover:-translate-y-0.5 hover:no-underline`}>
      {conteudo}
    </Link>
  );
}

/** Anterior / Página x de y / Próxima, voltando ao topo a cada troca. */
export function Paginacao({ pagina, totalPaginas, carregando, aoMudar }: {
  pagina: number; totalPaginas: number; carregando: boolean; aoMudar: (pagina: number) => void;
}) {
  if (totalPaginas <= 1) return null;
  const ir = (p: number) => { aoMudar(p); window.scrollTo({ top: 0 }); };
  return (
    <nav className="mt-6 flex items-center justify-center gap-4 text-suave" aria-label="Páginas">
      <Botao disabled={pagina === 0 || carregando} onClick={() => ir(pagina - 1)}>← Anterior</Botao>
      <span>Página {pagina + 1} de {totalPaginas}</span>
      <Botao disabled={pagina + 1 >= totalPaginas || carregando} onClick={() => ir(pagina + 1)}>Próxima →</Botao>
    </nav>
  );
}
