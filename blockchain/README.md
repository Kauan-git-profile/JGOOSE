# Blockchain local para o TCC da JGOOSE

Este projeto é um ponto de partida para testar o contrato de registro de hashes antes de integrar o Java/JGOOSE.

## Requisitos

- Node.js **22.13.0 ou superior** (requisito atual do Hardhat 3)
- npm
- Acesso à internet durante a instalação inicial das dependências

Confira:

```bash
node -v
npm -v
```

## Instalação

A pasta `blockchain/` deve ficar na raiz do repositório JGOOSE, ao lado de `jgoose/` e `e4j/`.

```bash
cd ~/JGOOSE/blockchain
npm install
```

Se você ainda não criou a pasta, extraia/copiei o conteúdo deste projeto para `~/JGOOSE/blockchain/`.

## Compilar o contrato

```bash
npm run build
```

## Iniciar a blockchain local

Abra o Terminal 1 e mantenha-o aberto:

```bash
cd ~/JGOOSE/blockchain
npm run node
```

O Hardhat inicia uma rede local, normalmente com Chain ID `31337`, RPC `http://127.0.0.1:8545` e contas de teste financiadas com ETH fictício.

**Nunca use as chaves privadas mostradas pelo nó local em uma rede pública ou carteira com fundos reais.**

## Implantar o contrato

Abra o Terminal 2:

```bash
cd ~/JGOOSE/blockchain
npm run deploy:local
```

Guarde o endereço de contrato exibido no final. Ele é necessário para a integração Java.

## O que o contrato registra

- `execucaoId`: identificador da execução no repositório off-chain.
- `hashArtefato`: hash SHA-256 do artefato, representado como `bytes32`.
- Timestamp do bloco e endereço que enviou a transação.
- Histórico append-only de versões para cada execução.

O contrato não armazena a matriz completa. O banco de dados continua sendo o repositório dos dados detalhados, conforme a arquitetura híbrida do TCC.

## Teste manual e próximos passos

Depois do deploy, o próximo passo é chamar `registrarHash(execucaoId, hashArtefato)` e consultar `obterHash(execucaoId)`. Isso pode ser feito inicialmente com um teste automatizado ou script de teste. Só depois de validar o contrato deve ser feita a integração com Java/Web3j.

## Limitação importante da rede local

A rede local mantém o estado em memória. Se o processo do nó for encerrado e iniciado novamente, os blocos e o estado do contrato deixam de existir. Para testes reprodutíveis, mantenha o nó aberto durante a sessão e faça novo deploy quando reiniciar a rede.
