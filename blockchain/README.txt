# Restaurar a pasta Ignition

Esta pasta contém o módulo de implantação do contrato `RastreabilidadeRegistry`.

Estrutura:
ignition/
└── modules/
    └── RastreabilidadeRegistry.ts

Como restaurar:
1. Extraia o ZIP.
2. Copie a pasta `ignition/` para a raiz do diretório `blockchain/` do JGOOSE.
3. Confirme que o contrato existe em `contracts/RastreabilidadeRegistry.sol` e se chama `RastreabilidadeRegistry`.

Observação:
Se o nó local atual continua ligado e você já implantou o contrato, restaurar o arquivo não exige
nova implantação. Se reiniciar o nó local, a rede normalmente será recriada do zero e será
necessário implantar o contrato novamente.
