import { useRef, useState, type FormEvent, type Ref } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { EnderecoInput } from '../api/types';
import { UFS } from './format';
import { Botao, Campo, controle, cx, linhaForm } from './ui';

const vazio: EnderecoInput = { cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '' };

/** "01310100" -> "01310-100" (aceita digitação parcial). */
const mascaraCep = (valor: string) => {
  const d = valor.replace(/\D/g, '').slice(0, 8);
  return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
};

type StatusCep =
  | { tipo: 'vazio' }
  | { tipo: 'buscando' }
  | { tipo: 'encontrado'; completo: boolean }
  | { tipo: 'erro'; mensagem: string };

interface Props {
  enviando: boolean;
  /** erros de validação por campo vindos da API */
  erros?: Record<string, string>;
  /** devolve true se salvou (o formulário é limpo) */
  onSalvar: (endereco: EnderecoInput) => Promise<boolean>;
}

/**
 * Formulário de endereço com busca pelo CEP: ao completar os 8 dígitos, consulta a API (BrasilAPI)
 * e preenche logradouro, bairro, cidade e UF. Todos os campos continuam editáveis.
 */
export function EnderecoForm({ enviando, erros, onSalvar }: Props) {
  const [end, setEnd] = useState<EnderecoInput>(vazio);
  const [status, setStatus] = useState<StatusCep>({ tipo: 'vazio' });
  const ultimoCep = useRef('');
  const refNumero = useRef<HTMLInputElement>(null);
  const refLogradouro = useRef<HTMLInputElement>(null);

  const set = (campo: keyof EnderecoInput, valor: string) => setEnd((e) => ({ ...e, [campo]: valor }));

  const alterarCep = (valor: string) => {
    const cep = mascaraCep(valor);
    set('cep', cep);
    const digitos = cep.replace(/\D/g, '');
    if (digitos.length === 8 && digitos !== ultimoCep.current) {
      void buscar(digitos);
    } else if (digitos.length < 8) {
      ultimoCep.current = '';
      setStatus({ tipo: 'vazio' });
    }
  };

  const buscar = async (digitos: string) => {
    ultimoCep.current = digitos;
    setStatus({ tipo: 'buscando' });
    try {
      const r = await api.consultarCep(digitos);
      if (ultimoCep.current !== digitos) return; // o usuário já mudou o CEP: ignora a resposta antiga
      setEnd((e) => ({
        ...e,
        cep: r.cep,
        logradouro: r.logradouro ?? '',
        bairro: r.bairro ?? '',
        cidade: r.cidade ?? '',
        uf: r.uf ?? '',
      }));
      const completo = !!r.logradouro;
      setStatus({ tipo: 'encontrado', completo });
      // próximo campo a preencher: o número (ou a rua, quando o CEP é geral da cidade)
      setTimeout(() => (completo ? refNumero : refLogradouro).current?.focus(), 0);
    } catch (e) {
      if (ultimoCep.current !== digitos) return;
      const mensagem = e instanceof ApiError && e.status === 404
        ? 'CEP não encontrado. Confira o número ou preencha o endereço manualmente.'
        : e instanceof ApiError && e.status === 503
          ? e.message
          : 'Não foi possível buscar o CEP agora. Preencha o endereço manualmente.';
      setStatus({ tipo: 'erro', mensagem });
    }
  };

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await onSalvar(end)) {
      setEnd(vazio);
      setStatus({ tipo: 'vazio' });
      ultimoCep.current = '';
    }
  };

  const campo = (k: keyof EnderecoInput, label: string, extra: { placeholder?: string; className?: string; ref?: Ref<HTMLInputElement> } = {}) => (
    <Campo label={label} erro={erros?.[k]} className={extra.className}>
      <input ref={extra.ref} className={controle} value={end[k] ?? ''} placeholder={extra.placeholder} onChange={(e) => set(k, e.target.value)} />
    </Campo>
  );

  return (
    <form onSubmit={enviar} className="mt-4 border-t border-borda pt-2">
      <h4>Adicionar endereço</h4>

      <div className={cx(linhaForm, 'items-center')}>
        <Campo label="CEP" erro={erros?.cep} className="max-w-[170px] min-w-[150px]">
          <input className={controle} value={end.cep} onChange={(e) => alterarCep(e.target.value)} onBlur={() => {
            const d = end.cep.replace(/\D/g, '');
            if (d.length === 8 && status.tipo === 'erro') void buscar(d); // tenta de novo ao sair do campo
          }} placeholder="00000-000" inputMode="numeric" autoComplete="postal-code" maxLength={9} aria-describedby="status-cep" />
        </Campo>
        <p id="status-cep" role="status" aria-live="polite" className={cx('mt-2 min-w-[200px] flex-1 text-sm',
          status.tipo === 'vazio' && 'text-suave',
          status.tipo === 'buscando' && 'text-primaria',
          status.tipo === 'encontrado' && 'font-semibold text-verde',
          status.tipo === 'erro' && 'font-semibold text-laranja')}>
          {status.tipo === 'vazio' && 'Digite o CEP para preencher o endereço automaticamente.'}
          {status.tipo === 'buscando' && '⏳ Buscando endereço...'}
          {status.tipo === 'encontrado' && (status.completo
            ? '✔ Endereço encontrado. Confira e informe o número.'
            : '✔ CEP geral da cidade: informe a rua e o bairro.')}
          {status.tipo === 'erro' && `⚠ ${status.mensagem}`}
        </p>
      </div>

      <div className={linhaForm}>
        {campo('logradouro', 'Logradouro', { placeholder: 'Rua, avenida...', className: 'grow-2 basis-64', ref: refLogradouro })}
        {campo('numero', 'Número', { className: 'max-w-[120px] min-w-[100px]', ref: refNumero })}
        {campo('complemento', 'Complemento', { placeholder: 'Sala, bloco...' })}
      </div>
      <div className={linhaForm}>
        {campo('bairro', 'Bairro')}
        {campo('cidade', 'Cidade')}
        <Campo label="UF" erro={erros?.uf} className="max-w-[130px] min-w-[110px]">
          <select className={controle} value={end.uf} onChange={(e) => set('uf', e.target.value)}>
            <option value="">Selecione...</option>
            {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
          </select>
        </Campo>
      </div>
      <Botao type="submit" variante="primario" disabled={enviando || status.tipo === 'buscando'}>Adicionar</Botao>
    </form>
  );
}
