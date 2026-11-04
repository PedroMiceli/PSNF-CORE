import { useState, type ReactNode } from 'react';
import { urlArquivo, type ApiError } from '../api/client';
import type { StatusOrdem } from '../api/types';
import { STATUS_LABEL } from './format';

export function StatusBadge({ status }: { status: StatusOrdem }) {
  return <span className={`badge status-${status}`}>{STATUS_LABEL[status] ?? status}</span>;
}

export function Carregando({ texto = 'Carregando...' }: { texto?: string }) {
  return <div className="carregando">{texto}</div>;
}

export function ErroBox({ erro }: { erro: ApiError | null | undefined }) {
  if (!erro) return null;
  return (
    <div className="erro-box" role="alert">
      <strong>{erro.status ? `Erro ${erro.status}: ` : ''}</strong>
      {erro.message}
      {erro.campos && (
        <ul>
          {Object.entries(erro.campos).map(([campo, msg]) => (
            <li key={campo}>
              <code>{campo}</code>: {msg}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function Vazio({ children }: { children: ReactNode }) {
  return <div className="vazio">{children}</div>;
}

export function Campo({ label, erro, children, dica }: { label: string; erro?: string; dica?: string; children: ReactNode }) {
  return (
    <label className={`campo ${erro ? 'campo-erro' : ''}`}>
      <span className="campo-label">{label}</span>
      {children}
      {dica && !erro && <span className="campo-dica">{dica}</span>}
      {erro && <span className="campo-msg">{erro}</span>}
    </label>
  );
}

/** Mostra a imagem do anúncio; se o caminho não for uma URL válida, mostra um placeholder com o nome. */
export function ImagemAnuncio({ caminho, alt, className }: { caminho?: string | null; alt: string; className?: string }) {
  const [falhou, setFalhou] = useState(false);
  const ehUrl = !!caminho && /^(https?:|data:|\/)/.test(caminho);
  if (!caminho || !ehUrl || falhou) {
    return (
      <div className={`img-placeholder ${className ?? ''}`} title={caminho ?? ''}>
        <span>📦</span>
        <small>{caminho || 'sem imagem'}</small>
      </div>
    );
  }
  return <img className={className} src={urlArquivo(caminho)} alt={alt} onError={() => setFalhou(true)} />;
}
