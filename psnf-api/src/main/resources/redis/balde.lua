-- Balde de fichas (token bucket) atômico. Uma chamada lê, reabastece, aplica a operação e grava.
--
-- KEYS[1]  chave do balde
-- ARGV[1]  capacidade (fichas)
-- ARGV[2]  fichas por minuto (pode ser fracionário)
-- ARGV[3]  operação: "consumir" (gasta 1 se houver), "gastar" (gasta 1 mesmo sem ter, fica em 0),
--          "espiar" (só consulta, não grava)
-- Devolve  { permitido (1/0), fichas inteiras restantes, segundos até haver 1 ficha (0 = há agora) }
--
-- O relógio é o do próprio Redis (TIME): as instâncias da API não precisam ter os relógios acertados entre si.
-- A chave expira sozinha quando o balde estaria cheio de novo (cheio = inexistente), então o Redis não acumula
-- baldes de clientes que pararam de chamar.

local capacidade = tonumber(ARGV[1])
local porMicro = tonumber(ARGV[2]) / 60000000
local operacao = ARGV[3]

local t = redis.call('TIME')
local agora = tonumber(t[1]) * 1000000 + tonumber(t[2])

local dados = redis.call('HMGET', KEYS[1], 'f', 't')
local fichas = tonumber(dados[1])
local em = tonumber(dados[2])
if fichas == nil or em == nil then
  fichas = capacidade
  em = agora
end

-- reabastece pelo tempo decorrido (capacidade pode ter diminuído na configuração: respeita o novo teto)
fichas = math.min(capacidade, fichas + math.max(0, agora - em) * porMicro)

local permitido = 0
if fichas >= 1 then permitido = 1 end

if operacao == 'consumir' then
  if permitido == 1 then fichas = fichas - 1 end
elseif operacao == 'gastar' then
  fichas = math.max(0, fichas - 1)
end

if operacao ~= 'espiar' then
  redis.call('HSET', KEYS[1], 'f', string.format('%.17g', fichas), 't', string.format('%.0f', agora))
  -- tempo até encher de novo, com 1 s de folga
  redis.call('PEXPIRE', KEYS[1], string.format('%.0f', math.ceil((capacidade - fichas) / porMicro / 1000) + 1000))
end

local espera = 0
if fichas < 1 then espera = math.max(1, math.ceil((1 - fichas) / porMicro / 1000000)) end

return { permitido, math.floor(fichas), espera }
