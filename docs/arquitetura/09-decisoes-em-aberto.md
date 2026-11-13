# 9. Decisões em aberto

[← 8. Operação](08-operacao.md) · [Índice](README.md)

Esta página reúne o que a arquitetura deixou em aberto. Cada item traz as opções e o que pesa na
escolha. A coluna "Decisão" está vazia para ser preenchida.

## Infraestrutura

| Tema | Opções | O que pesa | Decisão |
| --- | --- | --- | --- |
| Onde os 3 nós rodam | Um servidor com 3 máquinas virtuais, ou 3 servidores | Custo contra proteção a falha de hardware. Ver [infraestrutura](02-infraestrutura.md). | |
| Hardware de cada nó | Recomendado (8 núcleos, 32 GB) ou mínimo (4 núcleos, 16 GB) | Folga para picos e para a falha de um nó. | |
| Distribuição do Kubernetes | Há distribuições leves, pensadas para poucos nós, e a instalação padrão | Facilidade de instalar e atualizar contra controle fino. | |
| Energia | Só nobreak, ou nobreak e gerador | Quanto tempo fora do ar o negócio tolera. | |

## Rede

| Tema | Opções | O que pesa | Decisão |
| --- | --- | --- | --- |
| Link de internet | Plano atual, ou link empresarial simétrico | Velocidade de envio e estabilidade. Ver [rede e acesso](03-rede-e-acesso.md). | |
| Endereço IP | IP fixo, ou DNS dinâmico | Com IP que muda, o site sai do ar a cada troca. | |
| Segundo link | Não ter, ou contratar de outro provedor | É o ponto único de falha mais comum em servidor próprio. | |
| Domínio | Qual endereço a plataforma vai usar | Necessário para o DNS e o certificado. | |
| Acesso de administração | Só na rede local, ou VPN | Necessidade de operar de fora do local. | |

## Dados

| Tema | Opções | O que pesa | Decisão |
| --- | --- | --- | --- |
| Serviço de S3 | AWS S3 ou outro provedor compatível | Preço por volume guardado e por volume baixado; localização dos dados. | |
| Retenção dos backups | Por quantos dias guardar | Custo de armazenamento contra até quando se quer poder voltar. | |
| Perda de dados aceitável | Minutos, ou até um dia | Define se o envio contínuo das alterações é obrigatório. | |
| Tempo de reconstrução aceitável | Horas, ou dias | Define se é preciso ter hardware reserva. | |

## Serviços externos

| Tema | Opções | O que pesa | Decisão |
| --- | --- | --- | --- |
| Envio de e-mail | Serviço externo de envio, ou servidor de e-mail próprio | E-mails enviados de um IP próprio costumam cair em spam ou ser recusados. Um serviço externo é uma segunda dependência fora do local, além do S3. | |
| BrasilAPI | Manter como está | Se ficar indisponível, o preenchimento automático de CEP e a verificação de CNPJ param; o resto funciona. | |

## Operação

| Tema | Opções | O que pesa | Decisão |
| --- | --- | --- | --- |
| Registro de imagens | Dentro do cluster, ou em outra máquina do local | Dentro do cluster é mais simples; fora, sobrevive a problemas do cluster. | |
| Monitoramento externo | Qual serviço vai testar o site de fora | É a única forma de saber de uma queda de link ou energia. | |
| Quem recebe os alertas | Uma pessoa, ou um revezamento | Alertas que ninguém vê não servem. | |
| Ambiente de homologação | No mesmo cluster, ou não ter | Consome recursos dos mesmos nós. | |

## Itens que dependem de mudança na aplicação

Não bloqueiam a implantação, mas melhoram a operação:

| Item | Ganho |
| --- | --- |
| Endereço próprio de verificação de saúde na API | O cluster detecta com mais precisão quando uma cópia está com problema. |
| Métricas da aplicação | Painéis e alertas com dados de dentro da API. |
| Cache local das fotos mais vistas | Menos tráfego no link e menor custo de S3. |
| Leitura nas réplicas do banco | Mais capacidade sem trocar hardware. |
