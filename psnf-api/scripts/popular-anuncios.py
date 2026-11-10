#!/usr/bin/env python3
"""
Popula a PSNF API com anúncios de várias categorias, de vários fornecedores.

Uso (com a API rodando; para as imagens, também o MinIO):
    python3 psnf-api/scripts/popular-anuncios.py
    python3 psnf-api/scripts/popular-anuncios.py --imagens url     # sem MinIO: usa fotos do picsum.photos
    python3 psnf-api/scripts/popular-anuncios.py --limite 10       # só os 10 primeiros (teste rápido)
    API_URL=http://localhost:8081 python3 psnf-api/scripts/popular-anuncios.py

O que ele faz:
  1. busca as categorias da plataforma (fixas: são carregadas pela própria API, ninguém as cria);
  2. cria os fornecedores (usuários vendedores) que ainda não existirem;
  3. cadastra cada anúncio com faixas de preço por quantidade, variações e imagens.

Pode ser executado várias vezes: anúncios com o mesmo título para o mesmo fornecedor são pulados.
Cada fornecedor entra com o próprio login (POST /auth/token, senha teste123) para cadastrar os seus anúncios.
Só usa a biblioteca padrão do Python 3 (nada para instalar).
"""
import argparse
import json
import os
import re
import struct
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zlib

SENHA = "teste123"
# usuários criados antes da regra de 8 caracteres continuam com a senha antiga
SENHA_ANTIGA = "123456"

# Fornecedores fictícios. O primeiro é o Vendedor Mock da API (já existe).
FORNECEDORES = [
    {"chave": "mock", "email": "vendedor@mock.psnf"},
    {"chave": "aurora", "nome": "Distribuidora Aurora", "email": "contato@aurora.psnf.dev", "telefone": "11940000001"},
    {"chave": "central", "nome": "Atacado Central", "email": "vendas@central.psnf.dev", "telefone": "21940000002"},
    {"chave": "casaecia", "nome": "Casa & Cia Suprimentos", "email": "comercial@casaecia.psnf.dev", "telefone": "31940000003"},
    {"chave": "tecnomax", "nome": "TecnoMax Distribuição", "email": "b2b@tecnomax.psnf.dev", "telefone": "41940000004"},
    {"chave": "redesul", "nome": "Rede Sul Atacadista", "email": "pedidos@redesul.psnf.dev", "telefone": "51940000005"},
    {"chave": "nordeste", "nome": "Nordeste Comercial", "email": "atendimento@nordeste.psnf.dev", "telefone": "81940000006"},
]

# Catálogo: categoria -> [(título, preço unitário base, variações, detalhe para a descrição)]
CATALOGO = {
    "Alimentos e Bebidas": [
        ("Café torrado e moído 500g", 18.90, ["Tradicional", "Extraforte", "Gourmet"], "Pacote almofadado, torra média, ideal para revenda e cafeterias."),
        ("Arroz branco tipo 1 5kg", 27.50, [], "Grãos longos e soltinhos, fardo com 6 pacotes."),
        ("Feijão carioca 1kg", 8.40, [], "Safra nova, embalagem a vácuo, fardo com 10 unidades."),
        ("Azeite extravirgem 500ml", 32.00, ["Português", "Espanhol"], "Acidez máxima de 0,5%, garrafa de vidro escuro."),
        ("Refrigerante lata 350ml", 3.10, ["Cola", "Guaraná", "Laranja", "Limão"], "Fardo com 12 latas, ideal para revenda."),
        ("Água mineral sem gás 500ml", 1.20, [], "Fardo com 12 garrafas, fonte natural."),
    ],
    "Embalagens": [
        ("Caixa de papelão ondulado 40x30x20", 3.80, ["Parda", "Branca"], "Onda B, alta resistência para e-commerce e mudanças."),
        ("Saco plástico zip 15x20cm", 0.18, [], "Pacote com 100 unidades, fecho hermético."),
        ("Fita adesiva transparente 48mm x 100m", 6.50, ["Transparente", "Marrom"], "Alta adesão, ideal para fechamento de caixas."),
        ("Plástico bolha 1,2m x 100m", 89.00, [], "Bobina para proteção de produtos frágeis."),
        ("Sacola kraft com alça 32x26cm", 1.45, ["Parda", "Branca", "Preta"], "Papel 120g, alça de papel torcido."),
    ],
    "Limpeza e Higiene": [
        ("Detergente neutro 500ml", 2.30, ["Neutro", "Limão", "Coco"], "Caixa com 24 unidades, biodegradável."),
        ("Água sanitária 2L", 5.90, [], "Teor de cloro ativo de 2,0% a 2,5%."),
        ("Papel higiênico folha dupla 30m", 1.10, [], "Fardo com 64 rolos, macio e resistente."),
        ("Álcool em gel 70% 500g", 9.80, [], "Com hidratante, válvula pump."),
        ("Saco de lixo 100L reforçado", 0.65, ["Preto", "Azul"], "Pacote com 100 unidades, micragem reforçada."),
    ],
    "Material de Escritório": [
        ("Papel sulfite A4 75g", 24.90, [], "Resma com 500 folhas, alta alvura."),
        ("Grampeador de mesa 26/6", 22.00, ["Preto", "Azul"], "Capacidade de 25 folhas, base antiderrapante."),
        ("Pasta suspensa marmorizada", 2.10, [], "Caixa com 50 unidades, com visor e etiqueta."),
        ("Calculadora de mesa 12 dígitos", 34.90, [], "Visor grande, solar e bateria."),
        ("Organizador de mesa em acrílico", 45.00, ["Cristal", "Fumê"], "Com divisórias para canetas, clipes e post-its."),
    ],
    "Papelaria": [
        ("Caneta esferográfica 1.0mm", 0.95, ["Azul", "Preta", "Vermelha"], "Caixa com 50 unidades, escrita macia."),
        ("Caderno universitário 10 matérias", 19.90, ["Capa dura", "Espiral"], "200 folhas, pautado."),
        ("Lápis grafite HB", 0.70, [], "Caixa com 144 unidades."),
        ("Marca-texto fluorescente", 2.40, ["Amarelo", "Verde", "Rosa", "Laranja"], "Ponta chanfrada, secagem rápida."),
        ("Bloco de notas adesivas 76x76mm", 4.20, ["Amarelo", "Neon"], "100 folhas, cola reposicionável."),
    ],
    "Informática": [
        ("Notebook 15,6\" Core i5 16GB SSD 512GB", 3899.00, ["Cinza", "Preto"], "Tela Full HD, Windows 11, garantia de 1 ano."),
        ("Mouse sem fio 1600 DPI", 39.90, ["Preto", "Branco", "Azul"], "Receptor USB nano, pilha inclusa."),
        ("Teclado USB ABNT2", 49.90, [], "Teclas silenciosas, resistente a respingos."),
        ("Monitor LED 24\" Full HD", 789.00, [], "Painel IPS, 75Hz, HDMI e VGA."),
        ("SSD SATA 480GB", 219.00, [], "Leitura de até 550MB/s."),
        ("Cabo HDMI 2.0 2 metros", 18.50, ["1,5m", "2m", "3m"], "Suporta 4K a 60Hz."),
    ],
    "Eletrônicos": [
        ("Fone de ouvido Bluetooth", 119.00, ["Preto", "Branco"], "Até 20h de bateria, microfone embutido."),
        ("Caixa de som portátil 20W", 179.00, ["Preta", "Azul", "Vermelha"], "À prova d'água IPX5, Bluetooth 5.0."),
        ("Smart TV LED 43\" 4K", 1899.00, [], "HDR, Wi-Fi, 3 entradas HDMI."),
        ("Câmera de segurança Wi-Fi Full HD", 149.00, [], "Visão noturna, detecção de movimento, app no celular."),
        ("Power bank 10000mAh", 69.90, ["Preto", "Branco"], "Duas saídas USB, carga rápida."),
    ],
    "Eletrodomésticos": [
        ("Micro-ondas 30L", 649.00, ["Branco", "Inox"], "Função descongelar, 10 níveis de potência."),
        ("Liquidificador 1000W 3L", 159.00, ["Preto", "Vermelho"], "12 velocidades, copo de acrílico."),
        ("Air fryer 4L", 349.00, ["Preta", "Inox"], "Timer de 60 minutos, cesto antiaderente."),
        ("Ventilador de coluna 40cm", 189.00, ["Preto", "Branco"], "3 velocidades, oscilante, 6 pás."),
        ("Cafeteira elétrica 30 xícaras", 119.00, [], "Jarra de vidro, placa aquecedora."),
    ],
    "Celulares e Acessórios": [
        ("Capa de silicone para celular", 12.90, ["Preta", "Transparente", "Azul", "Rosa"], "Interior aveludado, proteção de câmera."),
        ("Película de vidro 3D", 6.50, [], "Dureza 9H, cobertura total da tela."),
        ("Carregador turbo USB-C 20W", 39.90, [], "Compatível com carregamento rápido PD."),
        ("Cabo USB-C 1 metro reforçado", 14.90, ["Branco", "Preto"], "Revestimento em nylon trançado."),
        ("Suporte veicular magnético", 24.90, [], "Fixação na saída de ar, giro de 360°."),
    ],
    "Ferramentas": [
        ("Furadeira de impacto 650W", 229.00, ["127V", "220V"], "Mandril de 13mm, velocidade variável."),
        ("Jogo de chaves combinadas 12 peças", 89.90, [], "Aço cromo-vanádio, de 8 a 22mm."),
        ("Parafusadeira a bateria 12V", 279.00, [], "Duas baterias, maleta e 13 acessórios."),
        ("Trena emborrachada 5 metros", 15.90, ["5m", "8m"], "Trava automática e gancho magnético."),
        ("Alicate universal 8\"", 27.50, [], "Cabo isolado 1000V."),
        ("Esmerilhadeira angular 4.1/2\" 850W", 199.00, ["127V", "220V"], "Empunhadura lateral, disco incluso."),
    ],
    "Construção": [
        ("Cimento CP II 50kg", 36.90, [], "Uso geral em concreto, argamassa e reboco."),
        ("Argamassa AC-II 20kg", 19.90, ["Cinza", "Branca"], "Para áreas internas e externas."),
        ("Tijolo cerâmico 8 furos", 0.85, [], "Milheiro com entrega em palete."),
        ("Tinta acrílica fosca 18L", 249.00, ["Branco neve", "Gelo", "Areia", "Cinza"], "Alto rendimento, lavável."),
        ("Massa corrida PVA 25kg", 59.90, [], "Para paredes internas, fácil de lixar."),
    ],
    "Elétrica": [
        ("Cabo flexível 2,5mm rolo 100m", 219.00, ["Preto", "Azul", "Vermelho", "Verde"], "750V, antichama."),
        ("Disjuntor DIN 20A", 14.50, ["10A", "16A", "20A", "32A"], "Curva C, monopolar."),
        ("Tomada 2P+T 10A", 6.90, ["10A", "20A"], "Com placa 4x2, padrão brasileiro."),
        ("Fita isolante 20m", 4.80, [], "Antichama, alta adesão."),
        ("Quadro de distribuição 12 disjuntores", 79.00, [], "Embutir, com barramento."),
    ],
    "Hidráulica": [
        ("Tubo PVC soldável 25mm 6m", 21.90, ["20mm", "25mm", "32mm"], "Para água fria, norma NBR 5648."),
        ("Registro de gaveta 3/4\"", 39.90, [], "Corpo em bronze, acabamento bruto."),
        ("Torneira de parede para cozinha", 69.90, ["Cromada", "Preta fosca"], "Bica móvel, arejador."),
        ("Caixa d'água polietileno 500L", 349.00, ["500L", "1000L"], "Com tampa, proteção UV."),
        ("Cola para PVC 175g", 12.90, [], "Secagem rápida, alta resistência."),
    ],
    "Iluminação": [
        ("Lâmpada LED bulbo 9W", 6.90, ["Branca fria", "Branca quente"], "Bivolt, base E27, 806 lúmens."),
        ("Refletor LED 50W", 54.90, [], "IP66, uso externo."),
        ("Fita LED 5 metros com fonte", 39.90, ["Branca", "Quente", "RGB"], "Autoadesiva, recortável."),
        ("Luminária plafon sobrepor 24W", 34.90, ["Redonda", "Quadrada"], "Luz branca, acabamento branco."),
        ("Arandela externa", 49.90, ["Preta", "Branca"], "Para muro e varanda, base E27."),
    ],
    "Móveis": [
        ("Cadeira de escritório ergonômica", 499.00, ["Preta", "Cinza"], "Encosto em tela, regulagem de altura e braços."),
        ("Mesa de escritório 120x60cm", 389.00, ["Carvalho", "Branca", "Preta"], "Tampo em MDP 25mm, pés de aço."),
        ("Estante de aço 5 prateleiras", 259.00, [], "Capacidade de 30kg por prateleira."),
        ("Armário multiuso 2 portas", 429.00, ["Branco", "Amadeirado"], "Com chave, 4 prateleiras."),
        ("Banqueta alta para balcão", 119.00, ["Preta", "Branca", "Madeira"], "Assento giratório, regulagem a gás."),
    ],
    "Casa e Decoração": [
        ("Quadro decorativo 40x60cm", 59.90, ["Abstrato", "Paisagem", "Botânico"], "Moldura de madeira e vidro."),
        ("Vaso de cerâmica médio", 44.90, ["Branco", "Terracota", "Verde"], "Acabamento fosco, com furo para drenagem."),
        ("Espelho redondo 60cm", 129.00, ["Moldura preta", "Moldura dourada"], "Para sala, quarto e banheiro."),
        ("Tapete 1,5x2m", 149.00, ["Cinza", "Bege", "Azul"], "Antiderrapante, pelo baixo."),
        ("Cortina blackout 2,8x1,8m", 89.90, ["Cinza", "Bege", "Preta"], "Bloqueia até 95% da luz."),
    ],
    "Cama, Mesa e Banho": [
        ("Jogo de lençol casal 150 fios", 79.90, ["Branco", "Cinza", "Azul"], "4 peças, 100% algodão."),
        ("Toalha de banho 70x140cm", 24.90, ["Branca", "Azul", "Bege"], "Fio penteado, alta absorção."),
        ("Travesseiro de fibra 50x70cm", 29.90, [], "Antialérgico, capa em algodão."),
        ("Toalha de mesa retangular 6 lugares", 49.90, ["Branca", "Xadrez", "Floral"], "Tecido impermeável."),
        ("Edredom casal dupla face", 149.00, ["Cinza/Branco", "Azul/Cinza"], "Enchimento siliconizado."),
    ],
    "Utilidades Domésticas": [
        ("Carrinho de metal para armazém", 120.00, ["Branco - P", "Branco - M", "Azul - M"], "Suporta até 150kg, rodas giratórias com trava."),
        ("Jogo de panelas antiaderente 5 peças", 199.00, ["Preto", "Vermelho"], "Tampas de vidro, cabos baquelite."),
        ("Pote hermético 1L", 9.90, ["500ml", "1L", "2L"], "Livre de BPA, vai ao micro-ondas."),
        ("Garrafa térmica 1L", 49.90, ["Inox", "Preta", "Branca"], "Mantém a temperatura por até 12h."),
        ("Escorredor de louça em aço", 69.90, [], "Com porta-talheres e bandeja."),
        ("Lixeira com pedal 30L", 89.90, ["Inox", "Branca"], "Balde interno removível."),
    ],
    "Roupas": [
        ("Camiseta básica algodão", 19.90, ["Branca - P", "Branca - M", "Branca - G", "Preta - M", "Preta - G"], "100% algodão fio 30.1, ideal para estampas."),
        ("Calça jeans masculina", 89.90, ["38", "40", "42", "44"], "Corte reto, lavagem média."),
        ("Moletom canguru", 79.90, ["Cinza", "Preto", "Azul-marinho"], "Flanelado por dentro, capuz com cordão."),
        ("Vestido midi estampado", 99.90, ["P", "M", "G"], "Tecido viscose, fresco e leve."),
        ("Camisa polo piquet", 49.90, ["Branca", "Azul", "Preta", "Verde"], "Bordado opcional para empresas."),
    ],
    "Calçados": [
        ("Tênis casual unissex", 129.00, ["37", "38", "39", "40", "41", "42"], "Solado de borracha, cabedal em lona."),
        ("Chinelo de borracha", 14.90, ["35/36", "37/38", "39/40", "41/42"], "Atacado em caixas com 24 pares."),
        ("Bota de segurança com bico de aço", 119.00, ["38", "40", "42", "44"], "CA aprovado, solado antiderrapante."),
        ("Sapatilha feminina", 59.90, ["34", "35", "36", "37", "38"], "Material sintético, palmilha macia."),
        ("Sandália papete", 69.90, ["Preta", "Caramelo"], "Tiras ajustáveis com velcro."),
    ],
    "Moda Íntima": [
        ("Cueca boxer algodão kit 3", 39.90, ["P", "M", "G", "GG"], "Elástico macio, sem costura lateral."),
        ("Calcinha de algodão kit 5", 34.90, ["P", "M", "G"], "Cores sortidas."),
        ("Meia soquete kit 6 pares", 24.90, ["Branca", "Preta", "Sortida"], "Algodão com elastano."),
        ("Pijama curto feminino", 59.90, ["P", "M", "G"], "Malha de algodão."),
    ],
    "Bolsas e Acessórios": [
        ("Mochila executiva para notebook", 149.00, ["Preta", "Cinza"], "Compartimento acolchoado até 15,6\"."),
        ("Carteira masculina em couro", 49.90, ["Preta", "Marrom"], "Porta-cartões e cédulas."),
        ("Bolsa tote feminina", 99.90, ["Caramelo", "Preta", "Off-white"], "Alça de ombro, bolso interno."),
        ("Óculos de sol UV400", 39.90, ["Preto", "Tartaruga"], "Proteção UV400, estojo incluso."),
        ("Relógio digital esportivo", 69.90, ["Preto", "Azul"], "À prova d'água, cronômetro e alarme."),
    ],
    "Uniformes e EPIs": [
        ("Luva nitrílica descartável", 32.90, ["P", "M", "G"], "Caixa com 100 unidades, sem pó."),
        ("Capacete de segurança com jugular", 24.90, ["Branco", "Amarelo", "Azul"], "Classe B, CA aprovado."),
        ("Óculos de proteção incolor", 7.90, [], "Antiembaçante, proteção UV."),
        ("Protetor auricular tipo plug", 1.90, [], "Silicone, com cordão."),
        ("Jaleco branco manga longa", 69.90, ["P", "M", "G", "GG"], "Gabardine, bolsos frontais."),
        ("Colete refletivo", 14.90, ["Laranja", "Amarelo"], "Faixas refletivas, ajuste com velcro."),
    ],
    "Beleza e Perfumaria": [
        ("Shampoo profissional 1L", 34.90, ["Hidratação", "Anticaspa", "Cachos"], "Uso em salões de beleza."),
        ("Creme hidratante corporal 400ml", 19.90, ["Lavanda", "Amêndoas", "Sem perfume"], "Pele seca a normal."),
        ("Kit de esmaltes 6 cores", 29.90, ["Cremosos", "Cintilantes"], "Secagem rápida."),
        ("Secador de cabelo 2000W", 159.00, ["127V", "220V"], "Dois níveis de temperatura, jato frio."),
        ("Perfume colônia 100ml", 79.90, ["Floral", "Amadeirado", "Cítrico"], "Fixação prolongada."),
    ],
    "Saúde": [
        ("Termômetro digital", 19.90, [], "Ponta flexível, alarme sonoro."),
        ("Máscara descartável tripla", 0.35, [], "Caixa com 50 unidades, clipe nasal."),
        ("Medidor de pressão de pulso", 129.00, [], "Memória para 60 leituras."),
        ("Kit de primeiros socorros", 89.90, ["Básico", "Empresarial"], "Maleta com itens essenciais."),
        ("Oxímetro de dedo", 59.90, [], "Mede saturação e batimentos."),
    ],
    "Esportes e Fitness": [
        ("Bola de pilates 65cm", 54.90, ["Azul", "Roxa", "Cinza"], "Anti-estouro, bomba inclusa."),
        ("Halter emborrachado par", 49.90, ["2kg", "4kg", "6kg", "8kg"], "Pegada antiderrapante."),
        ("Tapete de yoga 6mm", 59.90, ["Roxo", "Azul", "Preto"], "Material TPE, com alça."),
        ("Corda de pular com rolamento", 24.90, [], "Cabo de aço revestido, ajustável."),
        ("Bola de futebol de campo", 79.90, ["Branca", "Amarela"], "Costurada, tamanho oficial."),
        ("Garrafa squeeze 750ml", 19.90, ["Preta", "Azul", "Rosa"], "Livre de BPA, bico retrátil."),
    ],
    "Brinquedos": [
        ("Blocos de montar 500 peças", 89.90, [], "Compatível com as principais marcas, a partir de 4 anos."),
        ("Quebra-cabeça 1000 peças", 49.90, ["Paisagem", "Mapa-múndi", "Animais"], "Peças com encaixe preciso."),
        ("Boneca articulada", 69.90, ["Morena", "Loira", "Ruiva"], "Com acessórios."),
        ("Carrinho de controle remoto", 129.00, ["Vermelho", "Azul"], "Recarregável, alcance de 30 metros."),
        ("Jogo de tabuleiro clássico", 59.90, ["Damas e xadrez", "Ludo"], "Para toda a família."),
    ],
    "Bebês": [
        ("Fralda descartável", 54.90, ["P", "M", "G", "XG"], "Pacote econômico, gel superabsorvente."),
        ("Lenço umedecido 100 unidades", 9.90, [], "Sem álcool, hipoalergênico."),
        ("Mamadeira anticólica 260ml", 39.90, [], "Bico de silicone, livre de BPA."),
        ("Body bebê manga curta kit 3", 49.90, ["RN", "P", "M", "G"], "100% algodão."),
        ("Banheira infantil com suporte", 159.00, ["Branca", "Azul", "Rosa"], "Com termômetro e redutor."),
    ],
    "Pet Shop": [
        ("Ração para cães adultos 15kg", 159.00, ["Frango", "Carne"], "Premium, todas as raças."),
        ("Ração para gatos 10kg", 139.00, ["Salmão", "Frango"], "Castrados, controle de peso."),
        ("Areia higiênica para gatos 4kg", 19.90, [], "Granulada, controle de odor."),
        ("Cama pet acolchoada", 79.90, ["P", "M", "G"], "Lavável, base antiderrapante."),
        ("Coleira com guia", 34.90, ["P", "M", "G"], "Nylon resistente, fecho de segurança."),
    ],
    "Automotivo": [
        ("Óleo de motor 5W30 sintético 1L", 42.90, [], "API SP, para motores flex."),
        ("Palheta limpador de para-brisa", 29.90, ["16\"", "18\"", "20\"", "22\""], "Borracha de silicone."),
        ("Aspirador de pó automotivo 12V", 89.90, [], "Cabo de 4m, filtro lavável."),
        ("Kit de limpeza automotiva", 69.90, [], "Shampoo, cera e flanelas."),
        ("Capa para banco de carro", 119.00, ["Preta", "Cinza"], "Universal, couro sintético."),
    ],
    "Agro e Jardinagem": [
        ("Mangueira de jardim 30m", 89.90, ["15m", "30m", "50m"], "Com esguicho e engates."),
        ("Adubo NPK 10-10-10 5kg", 29.90, [], "Para hortas, jardins e frutíferas."),
        ("Sementes de hortaliças kit 10", 24.90, [], "Alface, tomate, cenoura e outras."),
        ("Pulverizador costal 20L", 189.00, [], "Bomba de pistão, alças acolchoadas."),
        ("Cortador de grama elétrico 1200W", 459.00, ["127V", "220V"], "Coletor de 30L, altura regulável."),
    ],
    "Industrial": [
        ("Pallet de madeira PBR", 79.00, [], "1,00 x 1,20m, padrão PBR."),
        ("Abraçadeira de nylon 200mm", 0.09, ["Branca", "Preta"], "Pacote com 100 unidades."),
        ("Estopa de algodão 1kg", 12.90, [], "Para limpeza industrial."),
        ("Lubrificante spray 300ml", 19.90, [], "Desengripante multiuso."),
        ("Parafuso sextavado M8 caixa 100", 34.90, ["M6", "M8", "M10"], "Aço zincado."),
    ],
    "Logística e Armazenagem": [
        ("Carrinho plataforma 300kg", 389.00, [], "Rodas de borracha, alça dobrável."),
        ("Paleteira manual 2500kg", 1890.00, [], "Garfos de 1150mm."),
        ("Filme stretch 50cm x 300m", 39.90, [], "25 micras, para paletização."),
        ("Caixa organizadora bin nº 5", 8.90, ["Azul", "Amarela", "Vermelha"], "Empilhável, para prateleiras."),
        ("Etiqueta térmica 100x150mm", 49.90, [], "Rolo com 500 etiquetas, para transportadoras."),
    ],
    "Festas e Eventos": [
        ("Balão de látex nº 9 pacote 50", 9.90, ["Sortido", "Branco", "Dourado", "Rosa"], "Ideal para decoração."),
        ("Copo descartável 200ml", 0.06, [], "Caixa com 2500 unidades."),
        ("Toalha de mesa TNT", 3.90, ["Branca", "Preta", "Vermelha"], "1,40 x 2,20m."),
        ("Vela de aniversário numeral", 4.90, ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"], "Dourada, 8cm."),
        ("Kit festa descartável 10 pessoas", 39.90, [], "Pratos, copos, talheres e guardanapos."),
    ],
}

# faixas de preço por quantidade: (quantidade mínima, fator sobre o preço base)
FAIXAS_VAREJO = [(1, 1.00), (10, 0.95), (50, 0.90), (200, 0.85)]
FAIXAS_ATACADO = [(10, 1.00), (100, 0.92), (500, 0.85), (1000, 0.80)]


# ------------------------------------------------------------------------------ HTTP
class ApiErro(Exception):
    def __init__(self, status, corpo):
        super().__init__(f"HTTP {status}: {corpo}")
        self.status = status
        self.corpo = corpo


def chamar(metodo, caminho, corpo=None, usuario=None, multipart=None, tentativa=0):
    args, kwargs = (metodo, caminho), {"corpo": corpo, "usuario": usuario, "multipart": multipart}
    headers = {}
    dados = None
    if multipart is not None:
        dados, tipo = multipart
        headers["Content-Type"] = tipo
    elif corpo is not None:
        dados = json.dumps(corpo).encode("utf-8")
        headers["Content-Type"] = "application/json"
    token = usuario or TOKEN_PADRAO
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(API + caminho, data=dados, method=metodo, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            texto = resp.read().decode("utf-8")
            return json.loads(texto) if texto else None
    except urllib.error.HTTPError as e:
        if e.code == 429 and tentativa < 5:
            # rate limit da API: espera o tempo pedido no Retry-After e tenta de novo
            espera = int(e.headers.get("Retry-After", "5"))
            print(f"    (limite de requisições da API: aguardando {espera} s)")
            time.sleep(espera)
            return chamar(*args, tentativa=tentativa + 1, **kwargs)
        raise ApiErro(e.code, e.read().decode("utf-8", "replace")) from None


def upload_png(nome_arquivo, conteudo, usuario):
    fronteira = "----psnf" + uuid.uuid4().hex
    corpo = (
        f"--{fronteira}\r\n"
        f'Content-Disposition: form-data; name="arquivo"; filename="{nome_arquivo}"\r\n'
        "Content-Type: image/png\r\n\r\n"
    ).encode() + conteudo + f"\r\n--{fronteira}--\r\n".encode()
    return chamar("POST", "/arquivos", usuario=usuario,
                  multipart=(corpo, f"multipart/form-data; boundary={fronteira}"))["caminho"]


# ------------------------------------------------------------------------------ imagens
def png(largura, altura, cor1, cor2, listras):
    """Gera um PNG com degradê diagonal e listras suaves (sem bibliotecas externas)."""
    linhas = []
    for y in range(altura):
        linha = bytearray(b"\x00")
        for x in range(largura):
            t = (x / largura + y / altura) / 2
            claro = 18 if ((x + y) // listras) % 2 == 0 else 0
            for c1, c2 in zip(cor1, cor2):
                linha.append(min(255, int(c1 + (c2 - c1) * t) + claro))
        linhas.append(bytes(linha))

    def bloco(tipo, dados):
        return struct.pack(">I", len(dados)) + tipo + dados + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF)

    return (b"\x89PNG\r\n\x1a\n"
            + bloco(b"IHDR", struct.pack(">IIBBBBB", largura, altura, 8, 2, 0, 0, 0))
            + bloco(b"IDAT", zlib.compress(b"".join(linhas), 9))
            + bloco(b"IEND", b""))


def cor_hsl(h, s, l):
    import colorsys
    r, g, b = colorsys.hls_to_rgb(h % 1.0, l, s)
    return int(r * 255), int(g * 255), int(b * 255)


def slug(texto):
    t = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode()
    return re.sub(r"[^a-z0-9]+", "-", t.lower()).strip("-")


# ------------------------------------------------------------------------------ etapas
def buscar_categorias():
    """As categorias são fixas (resources/db/categorias.sql na API). Produtos de uma categoria que não
    exista na plataforma são pulados, com aviso."""
    existentes = {c["nome"].lower(): c["id"] for c in chamar("GET", "/categorias")}
    ids = {}
    for nome in CATALOGO:
        if nome.lower() in existentes:
            ids[nome] = existentes[nome.lower()]
        else:
            print(f"  ! categoria '{nome}' não existe na plataforma: seus {len(CATALOGO[nome])} anúncios serão pulados")
    print(f"  {len(ids)} de {len(CATALOGO)} categorias do catálogo encontradas")
    return ids


def todos_anuncios_ativos():
    """Percorre todas as páginas da vitrine (GET /anuncios é paginado, no máximo 100 por página)."""
    pagina = 0
    while True:
        resultado = chamar("GET", f"/anuncios?pagina={pagina}&tamanho=100")
        yield from resultado["itens"]
        pagina += 1
        if pagina >= resultado["totalPaginas"]:
            return


def entrar(email):
    """Login de script: devolve o token de acesso e o id do usuário (POST /auth/token)."""
    r = entrar_confirmando(email, [SENHA, SENHA_ANTIGA])
    return {"id": r["usuario"]["id"], "token": r["accessToken"]}


def garantir_fornecedores():
    """Cria os fornecedores que faltam e faz login com cada um.

    O cadastro devolve 422 se o e-mail já existe; tudo bem: o login a seguir funciona do mesmo jeito
    (a senha de todos é teste123, ou 123456 nos criados antes da regra de 8 caracteres). Um fornecedor que exista com outra senha é pulado."""
    global TOKEN_PADRAO
    logados = []
    for f in FORNECEDORES:
        if "nome" in f:
            try:
                chamar("POST", "/usuarios", {"nome": f["nome"], "email": f["email"], "senha": SENHA,
                                             "telefone": f["telefone"]})
                print(f"  + fornecedor criado: {f['nome']}")
            except ApiErro as e:
                if e.status != 422:
                    raise
        try:
            logados.append(entrar(f["email"]))
        except ApiErro as e:
            print(f"  ! não foi possível entrar como {f['email']} ({e.status}): seus anúncios ficam com outro fornecedor")
    if not logados:
        sys.exit("Erro: nenhum fornecedor conseguiu entrar. Os usuários de teste estão ativos (PSNF_USUARIOS_TESTE)?")
    TOKEN_PADRAO = logados[0]["token"]
    return logados


def montar_anuncio(categoria_id, titulo, preco, variacoes, detalhe, imagens):
    atacado = preco < 5  # itens muito baratos são vendidos a partir de 10 unidades
    faixas = FAIXAS_ATACADO if atacado else FAIXAS_VAREJO
    return {
        "titulo": titulo,
        "descricao": f"{detalhe}\n\nVenda para empresas com preços progressivos por quantidade. "
                     f"Pedido mínimo de {faixas[0][0]} unidade(s). Negocie condições especiais pelo chat.",
        "categoriaId": categoria_id,
        "valores": [{"valorUnidade": round(preco * fator, 2), "quantidadeMinima": qtd} for qtd, fator in faixas],
        "imagens": [{"caminho": c, "principal": i == 0} for i, c in enumerate(imagens)],
        "variacoes": [{"variacao": v} for v in variacoes],
    }


def confirmar_email(email):
    """Conta nova precisa confirmar o e-mail. Sem SMTP (desenvolvimento), a API guarda os e-mails em
    GET /dev/emails: o script pega o link de lá e confirma, como o usuário faria clicando."""
    adm = chamar("POST", "/auth/token", {"email": "vendedor@mock.psnf", "senha": "123456"})["accessToken"]
    req = urllib.request.Request(API + "/dev/emails?para=" + urllib.parse.quote(email),
                                 headers={"Authorization": "Bearer " + adm})
    with urllib.request.urlopen(req, timeout=30) as resp:
        emails = json.loads(resp.read().decode("utf-8"))
    for e in emails:
        achado = re.search(r"confirmar-email\?token=([\w-]+)", e["texto"])
        if achado:
            chamar("POST", "/auth/confirmar-email", {"token": achado.group(1)})
            return True
    return False


def entrar_confirmando(email, senhas):
    """Login (POST /auth/token) tentando as senhas na ordem; e-mail não confirmado é confirmado antes."""
    for senha in senhas:
        try:
            return chamar("POST", "/auth/token", {"email": email, "senha": senha})
        except ApiErro as e:
            if e.status == 403 and confirmar_email(email):
                return chamar("POST", "/auth/token", {"email": email, "senha": senha})
            if e.status != 401:
                raise
    raise ApiErro(401, "senha não confere")


def main():
    global API
    parser = argparse.ArgumentParser(description="Popula a PSNF API com anúncios de exemplo.")
    parser.add_argument("--api", default=os.environ.get("API_URL", "http://localhost:8080"), help="URL da API")
    parser.add_argument("--imagens", choices=["upload", "url"], default="upload",
                        help="upload: gera PNGs e envia ao MinIO (padrão) | url: usa fotos de picsum.photos")
    parser.add_argument("--limite", type=int, default=0, help="cadastra no máximo N anúncios novos")
    args = parser.parse_args()
    API = args.api.rstrip("/")

    try:
        chamar("GET", "/auth/csrf")
    except (urllib.error.URLError, OSError):
        sys.exit(f"Erro: a API não respondeu em {API}. Ela está rodando?")

    print("Fornecedores:")
    fornecedores = garantir_fornecedores()
    print("Categorias:")
    categorias = buscar_categorias()

    # títulos que cada fornecedor já anunciou (para poder rodar o script de novo)
    ja_anunciados = {f["id"]: {a["titulo"] for a in chamar("GET", f"/usuarios/{f['id']}/anuncios")} for f in fornecedores}

    total = sum(len(p) for p in CATALOGO.values())
    print(f"\nCadastrando até {total} anúncios em {API} (imagens: {args.imagens}) ...\n")
    criados = pulados = falhas = 0
    imagem_upload_ok = args.imagens == "upload"
    n = 0

    for ci, (categoria, produtos) in enumerate(CATALOGO.items()):
        matiz = ci / len(CATALOGO)
        for pi, (titulo, preco, variacoes, detalhe) in enumerate(produtos):
            n += 1
            if categoria not in categorias:
                pulados += 1
                continue
            vendedor = fornecedores[(ci + pi) % len(fornecedores)]
            if titulo in ja_anunciados.get(vendedor["id"], set()):
                pulados += 1
                continue
            if args.limite and criados >= args.limite:
                continue

            try:
                imagens = []
                for k in range(2):  # duas imagens por anúncio
                    if imagem_upload_ok:
                        try:
                            conteudo = png(480, 360, cor_hsl(matiz + pi * 0.03, 0.55, 0.45),
                                           cor_hsl(matiz + 0.08 + k * 0.05, 0.6, 0.72), 24 + 6 * pi)
                            imagens.append(upload_png(f"{slug(titulo)}-{k + 1}.png", conteudo, vendedor["token"]))
                            continue
                        except ApiErro as e:
                            if e.status != 503:
                                raise
                            print("  ! storage (MinIO) indisponível: usando imagens do picsum.photos daqui em diante")
                            imagem_upload_ok = False
                    imagens.append(f"https://picsum.photos/seed/{slug(titulo)}-{k + 1}/600/400")

                corpo = montar_anuncio(categorias[categoria], titulo, preco, variacoes, detalhe, imagens)
                criado = chamar("POST", "/anuncios", corpo, usuario=vendedor["token"])
                criados += 1
                print(f"  ✔ [{n:3}/{total}] {categoria:<24} {titulo}  (a partir de R$ {criado['precoMenor']:.2f})")
            except ApiErro as e:
                falhas += 1
                print(f"  ✘ [{n:3}/{total}] {titulo}: {e}")

    print(f"\nResumo: {criados} anúncio(s) criado(s), {pulados} já existia(m), {falhas} falha(s).")
    print(f"Fornecedores usados: {len(fornecedores)} | categorias: {len(categorias)}")
    sys.exit(1 if falhas else 0)


API = ""
TOKEN_PADRAO = None
if __name__ == "__main__":
    main()
