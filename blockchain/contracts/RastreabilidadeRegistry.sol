// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

/// @title RastreabilidadeRegistry
/// @notice Registra evidências SHA-256 de artefatos de rastreabilidade da JGOOSE.
/// @dev O contrato guarda somente hashes; os artefatos completos ficam off-chain.
contract RastreabilidadeRegistry {
    struct Registro {
        bytes32 hashArtefato;
        uint256 timestamp;
        address responsavel;
    }

    mapping(uint256 => Registro[]) private registrosPorExecucao;

    event HashRegistrado(
        uint256 indexed execucaoId,
        bytes32 indexed hashArtefato,
        uint256 timestamp,
        address indexed responsavel,
        uint256 versao
    );

    /// @notice Registra uma nova versão do hash para uma execução.
    /// @param execucaoId Identificador da execução no repositório off-chain.
    /// @param hashArtefato SHA-256 do artefato, convertido para bytes32.
    function registrarHash(uint256 execucaoId, bytes32 hashArtefato) external {
        require(execucaoId > 0, "execucaoId invalido");
        require(hashArtefato != bytes32(0), "hash vazio");

        registrosPorExecucao[execucaoId].push(
            Registro({
                hashArtefato: hashArtefato,
                timestamp: block.timestamp,
                responsavel: msg.sender
            })
        );

        uint256 versao = registrosPorExecucao[execucaoId].length;
        emit HashRegistrado(
            execucaoId,
            hashArtefato,
            block.timestamp,
            msg.sender,
            versao
        );
    }

    /// @notice Retorna o hash mais recente registrado para uma execução.
    function obterHash(uint256 execucaoId) external view returns (bytes32) {
        uint256 total = registrosPorExecucao[execucaoId].length;
        require(total > 0, "execucao sem registros");
        return registrosPorExecucao[execucaoId][total - 1].hashArtefato;
    }

    /// @notice Retorna a quantidade de versões registradas.
    function quantidadeRegistros(uint256 execucaoId) external view returns (uint256) {
        return registrosPorExecucao[execucaoId].length;
    }

    /// @notice Retorna um registro histórico por índice (versão - 1).
    function obterRegistro(
        uint256 execucaoId,
        uint256 indice
    ) external view returns (bytes32 hashArtefato, uint256 timestamp, address responsavel) {
        require(indice < registrosPorExecucao[execucaoId].length, "indice invalido");
        Registro storage r = registrosPorExecucao[execucaoId][indice];
        return (r.hashArtefato, r.timestamp, r.responsavel);
    }
}
