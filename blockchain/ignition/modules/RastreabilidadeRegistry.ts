import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

export default buildModule("RastreabilidadeRegistryModule", (m) => {
  const registry = m.contract("RastreabilidadeRegistry");
  return { registry };
});
