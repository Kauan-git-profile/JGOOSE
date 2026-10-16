import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const RastreabilidadeRegistryModule = buildModule("RastreabilidadeRegistryModule", (m) => {
  const registry = m.contract("RastreabilidadeRegistry");
  return { registry };
});

export default RastreabilidadeRegistryModule;
