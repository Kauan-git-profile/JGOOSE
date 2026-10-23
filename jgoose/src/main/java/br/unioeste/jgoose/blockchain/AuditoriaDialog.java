package br.unioeste.jgoose.blockchain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import br.unioeste.jgoose.persistence.DatabaseConnection;

/**
 * Janela simples do módulo de auditoria: para cada execução salva no banco, recalcula o hash,
 * consulta a blockchain e mostra se está ÍNTEGRO, DESATUALIZADO, com ALERTA DE ADULTERAÇÃO
 * ou NÃO REGISTRADO. A auditoria roda em segundo plano (a interface não trava).
 */
public class AuditoriaDialog {

    private static final Color VERDE = new Color(0xD5F5DC);
    private static final Color AMARELO = new Color(0xFFF1C2);
    private static final Color VERMELHO = new Color(0xF8CFCF);

    /** Instala o menu "Integridade > Auditar com a blockchain..." na janela (sem mexer no layout dela). */
    public static void instalarMenu(final JFrame janela) {
        JMenuBar barra = janela.getJMenuBar();
        if (barra == null) {
            barra = new JMenuBar();
            janela.setJMenuBar(barra);
        }
        JMenu menu = new JMenu("Integridade");
        JMenuItem item = new JMenuItem("Auditar com a blockchain...");
        item.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                abrir(janela);
            }
        });
        menu.add(item);
        barra.add(menu);
    }

    /** Abre a janela e inicia a auditoria. */
    public static JDialog abrir(Component pai) {
        final JDialog dialogo = new JDialog(pai == null ? null : javax.swing.SwingUtilities.getWindowAncestor(pai),
                "Auditoria de integridade (banco x blockchain)");
        dialogo.setModalityType(java.awt.Dialog.ModalityType.MODELESS);

        final JLabel resumo = new JLabel("Auditando...");
        resumo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        resumo.setFont(resumo.getFont().deriveFont(Font.BOLD, 14f));

        final DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Execução", "Projeto", "Origem", "Resultado", "Versões on-chain"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        final JTable tabela = new JTable(modelo);
        tabela.setRowHeight(24);
        tabela.getColumnModel().getColumn(0).setMaxWidth(90);
        tabela.getColumnModel().getColumn(4).setMinWidth(130);
        tabela.getColumnModel().getColumn(4).setMaxWidth(150);
        final List<AuditoriaService.Resultado> resultados = new ArrayList<AuditoriaService.Resultado>();

        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int linha, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, col);
                if (!sel && linha < resultados.size()) {
                    c.setBackground(corDoStatus(resultados.get(linha).status));
                } else if (!sel) {
                    c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        });

        final JTextArea detalhes = new JTextArea();
        detalhes.setEditable(false);
        detalhes.setLineWrap(true);
        detalhes.setWrapStyleWord(true);
        detalhes.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detalhes.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        detalhes.setText("Selecione uma execução para ver os hashes e as versões registradas.");

        tabela.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                int l = tabela.getSelectedRow();
                if (!e.getValueIsAdjusting() && l >= 0 && l < resultados.size()) {
                    detalhes.setText(resultados.get(l).descricao());
                    detalhes.setCaretPosition(0);
                }
            }
        });

        JSplitPane divisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabela), new JScrollPane(detalhes));
        divisor.setResizeWeight(0.5);

        final JButton auditar = new JButton("Auditar novamente");
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialogo.dispose();
            }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(auditar);
        rodape.add(fechar);

        dialogo.getContentPane().setLayout(new BorderLayout());
        dialogo.getContentPane().add(resumo, BorderLayout.NORTH);
        dialogo.getContentPane().add(divisor, BorderLayout.CENTER);
        dialogo.getContentPane().add(rodape, BorderLayout.SOUTH);
        dialogo.setPreferredSize(new Dimension(900, 520));
        dialogo.pack();
        dialogo.setLocationRelativeTo(pai);

        final Runnable executar = new Runnable() {
            @Override
            public void run() {
                iniciar(resumo, modelo, tabela, detalhes, resultados, auditar);
            }
        };
        auditar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executar.run();
            }
        });

        dialogo.setVisible(true);
        executar.run();
        return dialogo;
    }

    private static Color corDoStatus(AuditoriaService.Status s) {
        switch (s) {
            case INTEGRO:
                return VERDE;
            case DIVERGENTE:
                return VERMELHO;
            default:
                return AMARELO;
        }
    }

    private static String textoDoStatus(AuditoriaService.Status s) {
        switch (s) {
            case INTEGRO:
                return "ÍNTEGRO";
            case DIVERGENTE:
                return "ALERTA DE ADULTERAÇÃO";
            case VERSAO_ANTERIOR:
                return "DESATUALIZADO";
            case ERRO:
                return "ERRO NA AUDITORIA";
            default:
                return "NÃO REGISTRADO";
        }
    }

    /** Resultado do trabalho em segundo plano. */
    private static class Saida {
        final List<AuditoriaService.Resultado> resultados = new ArrayList<AuditoriaService.Resultado>();
        final List<String> projetos = new ArrayList<String>();
        final List<String> origens = new ArrayList<String>();
        String erro;
    }

    private static void iniciar(final JLabel resumo, final DefaultTableModel modelo, final JTable tabela,
            final JTextArea detalhes, final List<AuditoriaService.Resultado> resultados, final JButton botao) {
        botao.setEnabled(false);
        resumo.setText("Auditando...");
        resumo.setForeground(Color.DARK_GRAY);
        modelo.setRowCount(0);
        resultados.clear();

        new SwingWorker<Saida, Void>() {
            @Override
            protected Saida doInBackground() {
                Saida saida = new Saida();
                File db = new File(DatabaseConnection.caminhoDoBanco());
                if (!db.isFile()) {
                    saida.erro = "Banco não encontrado: " + db.getAbsolutePath()
                            + "\nGere uma matriz (Horizontal Traceability) antes de auditar.";
                    return saida;
                }
                if (!BlockchainService.configurado()) {
                    saida.erro = "Blockchain não configurada.\nDefina JGOOSE_PRIVATE_KEY e JGOOSE_CONTRACT_ADDRESS "
                            + "(variáveis de ambiente ou arquivo jgoose-blockchain.properties).";
                    return saida;
                }
                BlockchainService chain = null;
                try {
                    DatabaseConnection.criarSchema();
                    chain = new BlockchainService();
                    AuditoriaService svc = new AuditoriaService();
                    for (Long id : svc.listarExecucoes()) {
                        saida.resultados.add(svc.auditarSeguro(chain, id.longValue()));
                        saida.projetos.add(svc.nomeDoProjeto(id.longValue()));
                        saida.origens.add(svc.origemDaExecucao(id.longValue()));
                    }
                } catch (Exception e) {
                    saida.erro = "Erro na auditoria: " + e.getMessage();
                } finally {
                    if (chain != null) {
                        chain.encerrar();
                    }
                }
                return saida;
            }

            @Override
            protected void done() {
                botao.setEnabled(true);
                Saida saida;
                try {
                    saida = get();
                } catch (InterruptedException e) {
                    return;
                } catch (ExecutionException e) {
                    resumo.setText("Erro inesperado na auditoria");
                    detalhes.setText(String.valueOf(e.getCause()));
                    return;
                }
                if (saida.erro != null) {
                    resumo.setText("Não foi possível auditar");
                    resumo.setForeground(new Color(0xB00020));
                    detalhes.setText(saida.erro);
                    return;
                }
                int integras = 0, alertas = 0, outras = 0;
                for (int i = 0; i < saida.resultados.size(); i++) {
                    AuditoriaService.Resultado r = saida.resultados.get(i);
                    resultados.add(r);
                    modelo.addRow(new Object[]{r.execucaoId, saida.projetos.get(i), saida.origens.get(i), textoDoStatus(r.status), r.versoes.size()});
                    if (r.status == AuditoriaService.Status.INTEGRO) {
                        integras++;
                    } else if (r.status == AuditoriaService.Status.DIVERGENTE) {
                        alertas++;
                    } else {
                        outras++;
                    }
                }
                resumo.setHorizontalAlignment(SwingConstants.LEFT);
                if (saida.resultados.isEmpty()) {
                    resumo.setText("Nenhuma execução salva no banco.");
                } else if (alertas > 0) {
                    resumo.setText("ALERTA: " + alertas + " execução(ões) com indício de adulteração  |  "
                            + integras + " íntegra(s)  |  " + outras + " outra(s)");
                    resumo.setForeground(new Color(0xB00020));
                } else {
                    resumo.setText(integras + " de " + saida.resultados.size() + " execução(ões) íntegra(s)"
                            + (outras > 0 ? "  |  " + outras + " sem registro atual / desatualizada(s)" : ""));
                    resumo.setForeground(outras > 0 ? new Color(0x8A6D00) : new Color(0x1B6E2E));
                }
                if (!resultados.isEmpty()) {
                    tabela.setRowSelectionInterval(0, 0);
                }
            }
        }.execute();
    }
}
