import { useRef, useState, type DragEvent } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { ArquivoEnviado } from '../api/types';

export const ACEITA_IMAGENS = 'image/jpeg,image/png,image/webp,image/gif';
export const ACEITA_DOCUMENTOS = 'application/pdf,application/xml,text/xml,.xml';

interface Props {
  accept: string;
  multiple?: boolean;
  rotulo?: string;
  compacto?: boolean;
  onEnviado: (arquivo: ArquivoEnviado) => void;
}

/**
 * Seleção/arrastar-e-soltar de arquivos. Cada arquivo é enviado para POST /arquivos (MinIO/S3)
 * e o componente devolve o "caminho" gerado pela API.
 */
export function UploadArquivo({ accept, multiple, rotulo = 'Escolher arquivo', compacto, onEnviado }: Props) {
  const input = useRef<HTMLInputElement>(null);
  const [enviando, setEnviando] = useState(0);
  const [erros, setErros] = useState<string[]>([]);
  const [arrastando, setArrastando] = useState(false);

  const enviar = async (arquivos: FileList | null) => {
    if (!arquivos?.length) return;
    const lista = Array.from(multiple ? arquivos : [arquivos[0]]);
    setErros([]);
    setEnviando(lista.length);
    for (const arquivo of lista) {
      try {
        onEnviado(await api.enviarArquivo(arquivo));
      } catch (e) {
        const msg = e instanceof ApiError ? e.message : String(e);
        setErros((atual) => [...atual, `${arquivo.name}: ${msg}`]);
      } finally {
        setEnviando((n) => n - 1);
      }
    }
    if (input.current) input.current.value = '';
  };

  const soltar = (e: DragEvent) => {
    e.preventDefault();
    setArrastando(false);
    void enviar(e.dataTransfer.files);
  };

  return (
    <div className="upload">
      <div
        className={`upload-area ${compacto ? 'compacto' : ''} ${arrastando ? 'arrastando' : ''}`}
        onClick={() => input.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setArrastando(true); }}
        onDragLeave={() => setArrastando(false)}
        onDrop={soltar}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && input.current?.click()}
      >
        {enviando > 0 ? (
          <span>⏳ Enviando {enviando} arquivo(s)...</span>
        ) : (
          <span>📎 {rotulo}{!compacto && <small> ou arraste aqui</small>}</span>
        )}
        <input ref={input} type="file" accept={accept} multiple={multiple} hidden onChange={(e) => void enviar(e.target.files)} />
      </div>
      {erros.map((e) => <p key={e} className="campo-msg">{e}</p>)}
    </div>
  );
}
