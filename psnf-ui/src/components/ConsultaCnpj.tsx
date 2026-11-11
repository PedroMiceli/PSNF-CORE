import { useEffect, useState } from 'react';
import { ApiError } from '../api/client';
import { api } from '../api/endpoints';
import type { EmpresaConsultada } from '../api/types';
import { cnpjValido } from './format';

export type ResultadoCnpj =
  | { tipo: 'vazio' }
  | { tipo: 'consultando' }
  | { tipo: 'ativa'; empresa: EmpresaConsultada }
  | { tipo: 'inativa'; empresa: EmpresaConsultada }
  | { tipo: 'fora-da-base' }
  | { tipo: 'indisponivel' };

/**
 * Consulta o CNPJ na Receita (pela API, que usa a BrasilAPI) assim que ele está completo e válido.
 * Mesmo CNPJ já consultado não consulta de novo (o navegador também guarda a resposta por 1 hora).
 */
export function useConsultaCnpj(cnpj: string, onEncontrada?: (e: EmpresaConsultada) => void): ResultadoCnpj {
  const [resultado, setResultado] = useState<ResultadoCnpj>({ tipo: 'vazio' });
  const limpo = cnpj.toUpperCase().replace(/[^0-9A-Z]/g, '');
  const completo = limpo.length === 14 && cnpjValido(limpo);

  useEffect(() => {
    if (!completo) { setResultado({ tipo: 'vazio' }); return; }
    let cancelado = false;
    setResultado({ tipo: 'consultando' });
    const t = setTimeout(() => {
      api.consultarCnpj(limpo)
        .then((empresa) => {
          if (cancelado) return;
          const ativa = empresa.situacao?.toUpperCase() === 'ATIVA';
          setResultado({ tipo: ativa ? 'ativa' : 'inativa', empresa });
          if (ativa) onEncontrada?.(empresa);
        })
        .catch((e) => {
          if (!cancelado) setResultado({ tipo: e instanceof ApiError && e.status === 404 ? 'fora-da-base' : 'indisponivel' });
        });
    }, 300);
    return () => { cancelado = true; clearTimeout(t); };
    // onEncontrada fica de fora de propósito: só o CNPJ dispara a consulta
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limpo, completo]);

  return resultado;
}

const cidade = (e: EmpresaConsultada) => [e.cidade && titulo(e.cidade), e.uf].filter(Boolean).join('/');
const titulo = (s: string) => s.toLowerCase().replace(/(^|\s)\S/g, (l) => l.toUpperCase());

/** Caixa abaixo do campo CNPJ com o que a Receita informa. */
export function ResultadoConsultaCnpj({ resultado }: { resultado: ResultadoCnpj }) {
  switch (resultado.tipo) {
    case 'vazio':
      return null;
    case 'consultando':
      return <p className="-mt-2 mb-3 text-xs text-suave" role="status">Consultando o CNPJ na Receita Federal...</p>;
    case 'ativa': {
      const e = resultado.empresa;
      return (
        <p className="-mt-2 mb-3 rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm" role="status">
          <strong className="text-verde">✓ {e.razaoSocial}</strong>
          {e.nomeFantasia && e.nomeFantasia !== e.razaoSocial && <span className="text-suave"> ({e.nomeFantasia})</span>}
          <span className="block text-xs text-suave">Ativa na Receita Federal{cidade(e) && ` · ${cidade(e)}`}</span>
        </p>
      );
    }
    case 'inativa':
      return (
        <p className="-mt-2 mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-vermelho" role="alert">
          <strong>{resultado.empresa.razaoSocial}</strong>: consta como <strong>{resultado.empresa.situacao}</strong> na Receita Federal.
          Só CNPJs ativos podem ser cadastrados.
        </p>
      );
    case 'fora-da-base':
      return (
        <p className="-mt-2 mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm" role="status">
          Não encontramos este CNPJ na base pública da Receita (CNPJs muito recentes demoram a aparecer).
          Você pode salvar mesmo assim; a conta fica sem o selo de empresa verificada.
        </p>
      );
    case 'indisponivel':
      return <p className="-mt-2 mb-3 text-xs text-suave" role="status">Não foi possível consultar a Receita agora. Você pode salvar mesmo assim.</p>;
  }
}
