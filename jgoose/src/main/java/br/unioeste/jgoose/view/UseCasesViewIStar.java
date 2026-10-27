package br.unioeste.jgoose.view;

import br.unioeste.jgoose.view.MainView;
import br.unioeste.jgoose.UseCases.Actor;
import br.unioeste.jgoose.UseCases.ActorISA;
import br.unioeste.jgoose.UseCases.Extend;
import br.unioeste.jgoose.UseCases.Step;
import br.unioeste.jgoose.UseCases.UseCase;
import br.unioeste.jgoose.controller.BPMNController;
import br.unioeste.jgoose.controller.Controller;
import br.unioeste.jgoose.controller.EditorWindowListener;
import br.unioeste.jgoose.controller.HorizontalBPMNTraceController;
import br.unioeste.jgoose.controller.HorizontalControler;
import br.unioeste.jgoose.controller.HorizontalIStarTraceController;
import br.unioeste.jgoose.controller.HorizontalUseCaseTraceController;
import br.unioeste.jgoose.controller.ImportBPMNGraph;
import br.unioeste.jgoose.controller.ImportIStarGraph;
import br.unioeste.jgoose.controller.UCController;
import br.unioeste.jgoose.controller.VerticalTraceController;
import br.unioeste.jgoose.e4j.filters.ShapeFilenameFilter;
import br.unioeste.jgoose.e4j.swing.BasicBPMNEditor;
import br.unioeste.jgoose.e4j.swing.BasicIStarEditor;
import br.unioeste.jgoose.e4j.swing.BasicUseCasesEditor;
import br.unioeste.jgoose.e4j.swing.EditorJFrame;
import br.unioeste.jgoose.e4j.swing.menubar.EditorMenuBar;
import br.unioeste.jgoose.model.FiltroDOC;
import br.unioeste.jgoose.model.UCActor;
import br.unioeste.jgoose.model.UCUseCase;
import br.unioeste.jgoose.util.IStarUtils;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.shape.mxStencilShape;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.w3c.dom.Element;

/**
 *
 * @author Alysson Girotto
 * @author Victor Augusto Pozzan
 */
public final class UseCasesViewIStar extends javax.swing.JFrame {

    private Actor selectedActor = null;
    private String selectedCase = "";
    private EditorJFrame e4jInstance = null;
    private EditorJFrame E4JiStar = null;
    private EditorJFrame E4JUseCases = null;
    private EditorJFrame E4JBPMN = null;
    private UseCasesViewBPMN useCasesViewBPMN = null;
    private DefaultTableModel tabCasosDeUso = new DefaultTableModel();
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger("console");
    private Image iconJGOOSE = Toolkit.getDefaultToolkit().getImage("./src/main/resources/icons/jgoose.gif");
    private BasicBPMNEditor bpmnEditor;
    Font roboto;

    /**
     * Creates new form UseCasesView
     *
     * @param E4JiStar
     * @param E4JBPMN
     * @param E4JUseCases
     * @param useCasesViewBPMN
     */
    public UseCasesViewIStar(EditorJFrame E4JiStar, EditorJFrame E4JBPMN, EditorJFrame E4JUseCases, UseCasesViewBPMN useCasesViewBPMN) {
        this.E4JBPMN = E4JBPMN;
        this.E4JiStar = E4JiStar;
        this.E4JUseCases = E4JUseCases;
        this.useCasesViewBPMN = useCasesViewBPMN;

        setLocationRelativeTo(null);
        initComponents();
        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        updateTable();

        try {
            InputStream myStream = new BufferedInputStream(new FileInputStream("./src/main/resources/fonts/Roboto-Black.ttf"));
            roboto = Font.createFont(Font.TRUETYPE_FONT, myStream);
            roboto = roboto.deriveFont(30f);

        } catch (IOException | FontFormatException e) {
            System.out.println(e);
        }

    }

    /*
     * Método que atualiza a Tabela de Casos de Uso
     */
    public void updateTable() {
        tabCasosDeUso = new DefaultTableModel();
        tabelUseCases.setAutoResizeMode(MAXIMIZED_HORIZ);
        tabCasosDeUso.addColumn(" ID"); // 0
        tabCasosDeUso.addColumn(" Use Case");
        tabCasosDeUso.addColumn(" Info"); // 2
        tabCasosDeUso.addColumn(" Delete"); // 3
        //tabCasosDeUso.addColumn("Included"); //4
        //tabCasosDeUso.addColumn("Guideline"); // 5
        String vetCasosDeUso[] = new String[4];
        int cont = 1;
        // adiciona os dados dos casos de uso no modelo da tabela
        for (UseCase useCase : Controller.getallUseCases()) {
            vetCasosDeUso[0] = " " + String.valueOf(useCase.getId());
            vetCasosDeUso[1] = " " + useCase.getName();
            vetCasosDeUso[2] = "";
            vetCasosDeUso[3] = "";
            tabCasosDeUso.addRow(vetCasosDeUso);
        }
        /*for (Actor actor : Controller.getUseCases()) {
            for (Object useCase : actor.getUseCases()) {
                UseCase caso = (UseCase) useCase;
                vetCasosDeUso[0] = "" + cont++;
                vetCasosDeUso[2] = caso.getName();
                vetCasosDeUso[3] = caso.getType();
                tabCasosDeUso.addRow(vetCasosDeUso);
            }
        }*/
        tabelUseCases.setModel(tabCasosDeUso);
        // seta a largura das colunas da tabela
        tabelUseCases.getColumnModel().getColumn(0).setMinWidth(40);
        tabelUseCases.getColumnModel().getColumn(0).setMaxWidth(40);
        tabelUseCases.getColumnModel().getColumn(1).setMinWidth(MAXIMIZED_HORIZ);
        tabelUseCases.getColumnModel().getColumn(2).setMinWidth(70);
        tabelUseCases.getColumnModel().getColumn(2).setMaxWidth(80);
        tabelUseCases.getColumnModel().getColumn(3).setMinWidth(70);
        tabelUseCases.getColumnModel().getColumn(3).setMaxWidth(80);

        tabelUseCases.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        tabelUseCases.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JTextField()));

        tabelUseCases.getColumnModel().getColumn(3).setCellRenderer(new ButtonRendererDelete1());
        tabelUseCases.getColumnModel().getColumn(3).setCellEditor(new ButtonDelete1(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tabelUseCases.getModel());
        tabelUseCases.setRowSorter(rowSorter);
        jtfFilter.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        });

    }

    /*
     * Método que insere um texto na área de texto de Casos de Uso com formatação
     */
    private void insertStyle(String text, SimpleAttributeSet style) {
        Document doc = textUseCases.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertStyleSave(String text, SimpleAttributeSet style) {
        Document doc = textUseCasesSave.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("empty-statement")
    private void tabelUseCasesValueChanged(ListSelectionEvent evt) {

        int colunm = tabelUseCases.getSelectedColumn();
        int linha = tabelUseCases.getSelectedRow();
        if (linha != -1) {
            labelCasosDeUso.setText("Use Case Specification");
            textUseCases.setText(""); // limpa o painel de texto
            textUseCases.setFont(new java.awt.Font("Roboto", 0, 14));
            // define o objeto com formatação negrito negrito
            SimpleAttributeSet negrito = new SimpleAttributeSet();
            StyleConstants.setBold(negrito, true);

            // insere os dados do caso de uso selecionado
            UseCase useCase = Controller.getallUseCases().get(linha);

            insertStyle("Use Case", negrito);
            insertStyle(": " + useCase.getName() + "\n\n", null);
            insertStyle("CHARACTERISTIC INFORMATIONN\n", negrito);
            insertStyle("	Goal in Context: ", negrito);
            insertStyle(": \n", null);
            insertStyle("	Scope", negrito);
            insertStyle(": \n", null);
            insertStyle("	Preconditions", negrito);
            insertStyle(": \n", null);
            insertStyle("	Success End Condition", negrito);
            insertStyle(": \n", null);
            insertStyle("	Failed End Condition", negrito);
            insertStyle(": \n", null);

            insertStyle("	Primary actor", negrito);
            insertStyle(": " + useCase.getPrimaryActor().getName() + "\n", null);

            insertStyle("\nMAIN SUCCESS SCENARIO\n", negrito);

            if (useCase.getSteps().isEmpty()) {
                insertStyle("\nEXTENSIONS\n", negrito);
            } else {
                int cont = 1;
                int n = useCase.getSteps().size();
                Step step;
                for (int i = n - 1; i >= 0; i--) {
                    step = useCase.getSteps().get(i);
                    String name = step.getName().replaceAll("\"", "");
                    if (step.getExtends().isEmpty()) {
                        if (step.isInclude()) {
                            name += " << include >> ";
                        }
                        insertStyle("	" + cont++ + ". " + name + "\n", null);
                    } else {
                        int m = step.getExtends().size() - 1;
                        name += " " + step.getExtend(m).getName().replaceAll("\"", "");
                        if (step.isInclude()) {
                            name += " << include >> ";
                        }
                        insertStyle("	" + cont++ + ". " + name + "\n", null);
                    }
                }
                insertStyle("\nEXTENSIONS\n", negrito);
                int aux = useCase.getSteps().size();
                cont = 1;
                for (int i = n - 1; i >= 0; i--) {
                    step = useCase.getSteps().get(i);
                    Extend extend;
                    n = step.getExtends().size();
                    if (n < 2) {
                        aux--;
                    } else {
                        for (int j = n - 2; j >= 0; j--) {
                            extend = step.getExtend(j);
                            insertStyle("	" + cont + "." + (n - j - 1) + ". " + extend.getName().replaceAll("\"", "") + " << extend >>\n", null);
                        }
                    }
                    cont++;
                }
            }

            insertStyle("RELATED INFORMATION\n", negrito);
            insertStyle("	Priority", negrito);
            insertStyle(": \n", null);
            insertStyle("	Target performance", negrito);
            insertStyle(": \n", null);
            insertStyle("	Frequency", negrito);
            insertStyle(": \n", null);
            insertStyle("	Use Case father", negrito);
            insertStyle(": \n", null);
            insertStyle("	Included Use Cases", negrito);
            insertStyle(": \n", null);
            insertStyle("	Secondary actors", negrito);
            insertStyle(": \n", null);
            insertStyle("	Additional information", negrito);
            insertStyle(": \n", null);
        } else {
            textUseCases.setText("");
        }

        if (colunm == 3) {
            int dialogButton = JOptionPane.YES_NO_OPTION;
            UseCase useCase = Controller.getallUseCases().get(linha);
            int dialogResult = JOptionPane.showConfirmDialog(null, "Would you like to delete a " + useCase.getName() + "?", "Warning", dialogButton);
            if (dialogResult == JOptionPane.YES_OPTION) {
                Controller.deleteUC(useCase);
                tabCasosDeUso.removeRow(linha);
            };
        }
    }

    /**
     * Abre uma janela GuidelinesDialogView
     */
    private void showGuidelinesDialog() throws URISyntaxException {
        GuidelinesDialogView diretrizes;
        try {
            diretrizes = new GuidelinesDialogView(this);
            diretrizes.setModal(true);
            int x = this.getX() + (this.getWidth() - diretrizes.getWidth()) / 2;
            int y = this.getY() + (this.getHeight() - diretrizes.getHeight()) / 2;
            diretrizes.setLocation(x, y);

            diretrizes.setVisible(true);
        } catch (BadLocationException ex) {
            LOG.error(ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelBackground = new javax.swing.JPanel();
        jPanelHeader = new javax.swing.JPanel();
        buttonDiagram = new javax.swing.JButton();
        buttonGuidelines = new javax.swing.JButton();
        JLabelUseCasesFromBPMN = new javax.swing.JLabel();
        buttonSaveUseCases = new javax.swing.JButton();
        jButtonAddUseCase = new javax.swing.JButton();
        jButtonAddUseCase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainView mainView = new MainView();
                AddUseCasefromBPMN addUseCase = new AddUseCasefromBPMN(mainView, true, "i*");
                int option = addUseCase.createDialogAdd();
                if(option == AddUseCasefromBPMN.YES) {
                    updateTable();
                }
            }
        });
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        labelSearchIcon = new javax.swing.JLabel();
        jtfFilter = new javax.swing.JTextField();
        buttonNFRs = new javax.swing.JButton();
        buttonShowIsas = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textUseCases = new javax.swing.JTextPane();
        labelCasosDeUso = new javax.swing.JLabel();
        buttonExportThisSpecification = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelUseCases = new javax.swing.JTable(){
            @Override
            public boolean isCellEditable(int row, int column){
                switch (column) {
                    case 2:
                    case 3:
                    return true;
                    default:
                    return false;
                }
            }
        };
        jPanelMenuButtons = new javax.swing.JPanel();
        btnMenuHome = new javax.swing.JButton();
        btnMenuiStar = new javax.swing.JButton();
        btnMenuBPMN = new javax.swing.JButton();
        btnMenuUC = new javax.swing.JButton();
        btnUCIStarViewBlock = new javax.swing.JButton();
        bntMenuTraceVertical = new javax.swing.JButton();
        btnMenuTraceHorizontal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Use Cases");
        setLocationByPlatform(true);
        setMinimumSize(null);

        jPanelBackground.setBackground(new java.awt.Color(244, 244, 244));
        jPanelBackground.setMaximumSize(new java.awt.Dimension(3276799, 32767999));

        jPanelHeader.setBackground(new java.awt.Color(255, 255, 255));
        jPanelHeader.setMaximumSize(new java.awt.Dimension(32767999, 32767999));

        buttonDiagram.setBackground(new java.awt.Color(15, 157, 229));
        buttonDiagram.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        buttonDiagram.setForeground(new java.awt.Color(250, 250, 250));
        buttonDiagram.setText("Diagram");
        buttonDiagram.setBorder(null);
        buttonDiagram.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonDiagramMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonDiagramMouseExited(evt);
            }
        });
        buttonDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDiagramActionPerformed(evt);
            }
        });

        buttonGuidelines.setBackground(new java.awt.Color(255, 255, 255));
        buttonGuidelines.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        buttonGuidelines.setText("Guidelines");
        buttonGuidelines.setBorder(null);
        buttonGuidelines.setPreferredSize(new java.awt.Dimension(69, 28));
        buttonGuidelines.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonGuidelinesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonGuidelinesMouseExited(evt);
            }
        });
        buttonGuidelines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGuidelinesActionPerformed(evt);
            }
        });

        JLabelUseCasesFromBPMN.setFont(new java.awt.Font("Roboto", 1, 24)); // NOI18N
        JLabelUseCasesFromBPMN.setForeground(new java.awt.Color(37, 172, 241));
        JLabelUseCasesFromBPMN.setText("Use Cases Mapped from i*");

        buttonSaveUseCases.setBackground(new java.awt.Color(255, 255, 255));
        buttonSaveUseCases.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        buttonSaveUseCases.setText("Export all Use Cases descriptions");
        buttonSaveUseCases.setBorder(null);
        buttonSaveUseCases.setPreferredSize(new java.awt.Dimension(153, 28));
        buttonSaveUseCases.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonSaveUseCasesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonSaveUseCasesMouseExited(evt);
            }
        });
        buttonSaveUseCases.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveUseCasesActionPerformed(evt);
            }
        });

        jButtonAddUseCase.setBackground(new java.awt.Color(15, 157, 229));
        jButtonAddUseCase.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jButtonAddUseCase.setForeground(new java.awt.Color(250, 250, 250));
        jButtonAddUseCase.setText("Add Use Case");
        jButtonAddUseCase.setBorder(null);
        jButtonAddUseCase.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonAddUseCaseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonAddUseCaseMouseExited(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undraw_file_analysis.png"))); // NOI18N

        jPanel2.setBackground(new java.awt.Color(244, 251, 255));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(213, 239, 255), 2, true));

        labelSearchIcon.setBackground(new java.awt.Color(244, 251, 255));
        labelSearchIcon.setForeground(new java.awt.Color(244, 251, 255));
        labelSearchIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSearchIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchIcon (3).png"))); // NOI18N
        labelSearchIcon.setToolTipText("");
        labelSearchIcon.setOpaque(true);

        jtfFilter.setBackground(new java.awt.Color(244, 251, 255));
        jtfFilter.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        jtfFilter.setToolTipText("");
        jtfFilter.setBorder(null);
        jtfFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jtfFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSearchIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jtfFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(labelSearchIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        buttonNFRs.setBackground(new java.awt.Color(255, 255, 255));
        buttonNFRs.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        buttonNFRs.setText("Show NFRs");
        buttonNFRs.setBorder(null);
        buttonNFRs.setPreferredSize(new java.awt.Dimension(77, 28));
        buttonNFRs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonNFRsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonNFRsMouseExited(evt);
            }
        });
        buttonNFRs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNFRsActionPerformed(evt);
            }
        });

        buttonShowIsas.setBackground(new java.awt.Color(255, 255, 255));
        buttonShowIsas.setFont(new java.awt.Font("Roboto", 0, 14)); // NOI18N
        buttonShowIsas.setText("Show Isa's");
        buttonShowIsas.setBorder(null);
        buttonShowIsas.setPreferredSize(new java.awt.Dimension(69, 28));
        buttonShowIsas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonShowIsasMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonShowIsasMouseExited(evt);
            }
        });
        buttonShowIsas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShowIsasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelHeaderLayout = new javax.swing.GroupLayout(jPanelHeader);
        jPanelHeader.setLayout(jPanelHeaderLayout);
        jPanelHeaderLayout.setHorizontalGroup(
            jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JLabelUseCasesFromBPMN))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(buttonNFRs, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addComponent(jButtonAddUseCase, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23)
                        .addComponent(buttonDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addComponent(buttonShowIsas, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonSaveUseCases, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(buttonGuidelines, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelHeaderLayout.setVerticalGroup(
            jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHeaderLayout.createSequentialGroup()
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelHeaderLayout.createSequentialGroup()
                                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonAddUseCase, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(21, 21, 21))))
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(JLabelUseCasesFromBPMN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonGuidelines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonSaveUseCases, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonNFRs, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonShowIsas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        textUseCases.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(53, 178, 242)));
        textUseCases.setFont(new java.awt.Font("Roboto", 1, 14));
        textUseCases.setEnabled(false);
        jScrollPane3.setViewportView(textUseCases);

        labelCasosDeUso.setBackground(new java.awt.Color(53, 178, 242));
        labelCasosDeUso.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        labelCasosDeUso.setForeground(new java.awt.Color(53, 178, 242));
        labelCasosDeUso.setText("Use Case Specification");

        buttonExportThisSpecification.setBackground(new java.awt.Color(53, 178, 242));
        buttonExportThisSpecification.setFont(new java.awt.Font("Roboto", 0, 16)); // NOI18N
        buttonExportThisSpecification.setForeground(new java.awt.Color(250, 250, 250));
        buttonExportThisSpecification.setText("Export this Specification");
        buttonExportThisSpecification.setToolTipText("");
        buttonExportThisSpecification.setBorder(null);
        buttonExportThisSpecification.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonExportThisSpecificationMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonExportThisSpecificationMouseExited(evt);
            }
        });
        buttonExportThisSpecification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExportThisSpecificationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelCasosDeUso)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(buttonExportThisSpecification, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(labelCasosDeUso)
                .addGap(8, 8, 8)
                .addComponent(buttonExportThisSpecification, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        tabelUseCases.setAutoCreateRowSorter(true);
        tabelUseCases.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        tabelUseCases.setForeground(new java.awt.Color(37, 172, 241));
        tabelUseCases.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Use Case", "", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelUseCases.setGridColor(new java.awt.Color(252, 252, 252));
        tabelUseCases.setPreferredSize(null);
        tabelUseCases.setRowHeight(30);
        tabelUseCases.setSelectionBackground(new java.awt.Color(199, 235, 254));
        tabelUseCases.setSelectionForeground(new java.awt.Color(37, 172, 241));
        tabelUseCases.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelUseCases.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelUseCases.setShowHorizontalLines(false);
        tabelUseCases.setShowVerticalLines(false);
        tabelUseCases.setUpdateSelectionOnSort(false);
        tabelUseCases.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelUseCases.getTableHeader().setReorderingAllowed(false);

        tabelUseCases.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent evt) {
                tabelUseCases.repaint();
                tabelUseCasesValueChanged(evt);
            }
        });

        tabelUseCases.getTableHeader().setFont(new java.awt.Font("Roboto", 0, 14));
        tabelUseCases.getColumnModel().getColumn(1).setMinWidth(MAXIMIZED_HORIZ);
        jScrollPane1.setViewportView(tabelUseCases);
        if (tabelUseCases.getColumnModel().getColumnCount() > 0) {
            tabelUseCases.getColumnModel().getColumn(0).setResizable(false);
            tabelUseCases.getColumnModel().getColumn(1).setResizable(false);
            tabelUseCases.getColumnModel().getColumn(2).setResizable(false);
            tabelUseCases.getColumnModel().getColumn(3).setResizable(false);
        }
        tabelUseCases.getAccessibleContext().setAccessibleName("");
        tabelUseCases.getAccessibleContext().setAccessibleDescription("");
        tabelUseCases.getTableHeader().setForeground(new java.awt.Color(71, 92, 84));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        javax.swing.GroupLayout jPanelBackgroundLayout = new javax.swing.GroupLayout(jPanelBackground);
        jPanelBackground.setLayout(jPanelBackgroundLayout);
        jPanelBackgroundLayout.setHorizontalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addGroup(jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelBackgroundLayout.setVerticalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanelMenuButtons.setBackground(new java.awt.Color(11, 113, 165));

        btnMenuHome.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuHome.setForeground(new java.awt.Color(11, 113, 165));
        btnMenuHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-Home.png"))); // NOI18N
        btnMenuHome.setBorderPainted(false);
        btnMenuHome.setFocusPainted(false);
        btnMenuHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMenuHomeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMenuHomeMouseExited(evt);
            }
        });
        btnMenuHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuHomeActionPerformed(evt);
            }
        });

        btnMenuiStar.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuiStar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-istar.png"))); // NOI18N
        btnMenuiStar.setBorder(null);
        btnMenuiStar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMenuiStarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMenuiStarMouseExited(evt);
            }
        });
        btnMenuiStar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuiStarActionPerformed(evt);
            }
        });

        btnMenuBPMN.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuBPMN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-BPMN.png"))); // NOI18N
        btnMenuBPMN.setBorder(null);
        btnMenuBPMN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMenuBPMNMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMenuBPMNMouseExited(evt);
            }
        });
        btnMenuBPMN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuBPMNActionPerformed(evt);
            }
        });

        btnMenuUC.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuUC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-UseCase.png"))); // NOI18N
        btnMenuUC.setBorder(null);
        btnMenuUC.setFocusable(false);
        btnMenuUC.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMenuUCMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMenuUCMouseExited(evt);
            }
        });
        btnMenuUC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuUCActionPerformed(evt);
            }
        });

        btnUCIStarViewBlock.setBackground(new java.awt.Color(11, 113, 165));
        btnUCIStarViewBlock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-UC-iStar.png"))); // NOI18N
        btnUCIStarViewBlock.setBorder(null);
        btnUCIStarViewBlock.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnUCIStarViewBlock.setEnabled(false);

        bntMenuTraceVertical.setBackground(new java.awt.Color(11, 113, 165));
        bntMenuTraceVertical.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-VerticalTraceability.png"))); // NOI18N
        bntMenuTraceVertical.setBorder(null);
        bntMenuTraceVertical.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bntMenuTraceVerticalMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bntMenuTraceVerticalMouseExited(evt);
            }
        });
        bntMenuTraceVertical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntMenuTraceVerticalActionPerformed(evt);
            }
        });

        btnMenuTraceHorizontal.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuTraceHorizontal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-HorizontalTraceability.png"))); // NOI18N
        btnMenuTraceHorizontal.setToolTipText("");
        btnMenuTraceHorizontal.setBorder(null);
        btnMenuTraceHorizontal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMenuTraceHorizontalMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMenuTraceHorizontalMouseExited(evt);
            }
        });
        btnMenuTraceHorizontal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenuTraceHorizontalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMenuButtonsLayout = new javax.swing.GroupLayout(jPanelMenuButtons);
        jPanelMenuButtons.setLayout(jPanelMenuButtonsLayout);
        jPanelMenuButtonsLayout.setHorizontalGroup(
            jPanelMenuButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMenuButtonsLayout.createSequentialGroup()
                .addGroup(jPanelMenuButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMenuBPMN, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMenuUC, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUCIStarViewBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bntMenuTraceVertical, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMenuiStar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMenuHome, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMenuTraceHorizontal, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        jPanelMenuButtonsLayout.setVerticalGroup(
            jPanelMenuButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMenuButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnMenuHome, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(btnMenuiStar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnMenuBPMN, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnMenuUC, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnUCIStarViewBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(bntMenuTraceVertical, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnMenuTraceHorizontal, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanelMenuButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelBackground, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelBackground, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelMenuButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonGuidelinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGuidelinesActionPerformed
        try {
            this.showGuidelinesDialog();
        } catch (URISyntaxException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonGuidelinesActionPerformed

    private void buttonDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDiagramActionPerformed
        try {
            this.showUseCasesDiagram();
        } catch (HeadlessException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_buttonDiagramActionPerformed

    private void buttonSaveUseCasesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesActionPerformed
        this.showSaveUseCases();
    }//GEN-LAST:event_buttonSaveUseCasesActionPerformed

    private void buttonExportThisSpecificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportThisSpecificationActionPerformed
        saveDescription();
    }//GEN-LAST:event_buttonExportThisSpecificationActionPerformed

    private void jtfFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtfFilterActionPerformed

    private void btnMenuHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuHomeActionPerformed
        dispose();
    }//GEN-LAST:event_btnMenuHomeActionPerformed

    private void btnMenuTraceHorizontalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalActionPerformed
            HorizontalControler.openViewTraceabilityHorizontal();
    }//GEN-LAST:event_btnMenuTraceHorizontalActionPerformed

    private void btnMenuiStarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuiStarMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnMenuiStar.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnMenuiStarMouseEntered

    private void btnMenuiStarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuiStarMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnMenuiStar.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnMenuiStarMouseExited

    private void btnMenuHomeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuHomeMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnMenuHome.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnMenuHomeMouseEntered

    private void btnMenuHomeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuHomeMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnMenuHome.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnMenuHomeMouseExited

    private void btnMenuBPMNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuBPMNMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnMenuBPMN.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnMenuBPMNMouseEntered

    private void btnMenuBPMNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuBPMNMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnMenuBPMN.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnMenuBPMNMouseExited

    private void btnMenuUCMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuUCMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnMenuUC.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnMenuUCMouseEntered

    private void btnMenuUCMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuUCMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnMenuUC.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnMenuUCMouseExited

    private void bntMenuTraceVerticalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        bntMenuTraceVertical.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_bntMenuTraceVerticalMouseEntered

    private void bntMenuTraceVerticalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        bntMenuTraceVertical.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_bntMenuTraceVerticalMouseExited

    private void btnMenuTraceHorizontalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnMenuTraceHorizontal.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnMenuTraceHorizontalMouseEntered

    private void btnMenuTraceHorizontalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnMenuTraceHorizontal.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnMenuTraceHorizontalMouseExited

    private void jButtonAddUseCaseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAddUseCaseMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        jButtonAddUseCase.setBackground(new java.awt.Color(69, 185, 243));
    }//GEN-LAST:event_jButtonAddUseCaseMouseEntered

    private void jButtonAddUseCaseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAddUseCaseMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        jButtonAddUseCase.setBackground(new java.awt.Color(15, 157, 229));
    }//GEN-LAST:event_jButtonAddUseCaseMouseExited

    private void buttonDiagramMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonDiagramMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonDiagram.setBackground(new java.awt.Color(69, 185, 243));
    }//GEN-LAST:event_buttonDiagramMouseEntered

    private void buttonDiagramMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonDiagramMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        buttonDiagram.setBackground(new java.awt.Color(15, 157, 229));
    }//GEN-LAST:event_buttonDiagramMouseExited

    private void buttonSaveUseCasesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonSaveUseCases.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonSaveUseCasesMouseEntered

    private void buttonSaveUseCasesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesMouseExited
        buttonSaveUseCases.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_buttonSaveUseCasesMouseExited

    private void buttonGuidelinesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonGuidelinesMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonGuidelines.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonGuidelinesMouseEntered

    private void buttonGuidelinesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonGuidelinesMouseExited
        buttonGuidelines.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_buttonGuidelinesMouseExited

    private void buttonExportThisSpecificationMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonExportThisSpecificationMouseEntered
        setCursor(Cursor.HAND_CURSOR);
    }//GEN-LAST:event_buttonExportThisSpecificationMouseEntered

    private void buttonExportThisSpecificationMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonExportThisSpecificationMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_buttonExportThisSpecificationMouseExited

    private void btnMenuiStarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuiStarActionPerformed
        try {
            this.showE4JiStar();
        } catch (HeadlessException ex) {
            java.util.logging.Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnMenuiStarActionPerformed

    private void btnMenuBPMNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuBPMNActionPerformed
        try {
            this.showE4JBPMN();
        } catch (HeadlessException ex) {
            java.util.logging.Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnMenuBPMNActionPerformed

    private void btnMenuUCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuUCActionPerformed
        try {
            this.showE4JUseCases();
        } catch (HeadlessException ex) {
            java.util.logging.Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnMenuUCActionPerformed

    private void bntMenuTraceVerticalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalActionPerformed
        VerticalTraceController.openVerticalTraceabilityView();
    }//GEN-LAST:event_bntMenuTraceVerticalActionPerformed

    private void buttonNFRsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonNFRsMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonNFRs.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonNFRsMouseEntered

    private void buttonNFRsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonNFRsMouseExited
        buttonNFRs.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);        // TODO add your handling code here:
    }//GEN-LAST:event_buttonNFRsMouseExited

    private void buttonNFRsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNFRsActionPerformed
        MainView mainView = new MainView();
        ShowNRFs info = new ShowNRFs(mainView, true);
        info.setVisible(true);
    }//GEN-LAST:event_buttonNFRsActionPerformed

    private void buttonShowIsasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonShowIsasMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonShowIsas.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonShowIsasMouseEntered

    private void buttonShowIsasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonShowIsasMouseExited
        buttonShowIsas.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);        // TODO add your handling code here:
    }//GEN-LAST:event_buttonShowIsasMouseExited

    private void buttonShowIsasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowIsasActionPerformed
        MainView mainView = new MainView();
        ShowISAs info = new ShowISAs(mainView, true);
        info.setVisible(true);
    }//GEN-LAST:event_buttonShowIsasActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel JLabelUseCasesFromBPMN;
    private javax.swing.JButton bntMenuTraceVertical;
    private javax.swing.JButton btnMenuBPMN;
    private javax.swing.JButton btnMenuHome;
    private javax.swing.JButton btnMenuTraceHorizontal;
    private javax.swing.JButton btnMenuUC;
    private javax.swing.JButton btnMenuiStar;
    private javax.swing.JButton btnUCIStarViewBlock;
    private javax.swing.JButton buttonDiagram;
    private javax.swing.JButton buttonExportThisSpecification;
    private javax.swing.JButton buttonGuidelines;
    private javax.swing.JButton buttonNFRs;
    private javax.swing.JButton buttonSaveUseCases;
    private javax.swing.JButton buttonShowIsas;
    private javax.swing.JButton jButtonAddUseCase;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelBackground;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelMenuButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jtfFilter;
    private javax.swing.JLabel labelCasosDeUso;
    private javax.swing.JLabel labelSearchIcon;
    public javax.swing.JTable tabelUseCases;
    private javax.swing.JTextPane textUseCases;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTextPane textUseCasesSave;

    // Clicou em Mostrar Diagrama de Casos de Uso
    private void showUseCasesDiagram() throws HeadlessException, IOException {
        if (e4jInstance == null) {
            e4jInstance = new EditorJFrame(1);
            e4jInstance.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            EditorWindowListener windowListener = new EditorWindowListener(this, e4jInstance);
            this.addWindowListener(windowListener);
            e4jInstance.addWindowListener(windowListener);
            this.addWindowListener(windowListener);
            BasicUseCasesEditor editor = (BasicUseCasesEditor) e4jInstance.getEditor();
            JMenuBar menubar = e4jInstance.getJMenuBar();
            JMenu fileMenu = ((EditorMenuBar) menubar).getFileMenu();
            String label1 = mxResources.get("traceabilityMaker", null, "Horizontal Traceability");
            JMenuItem menuItem1 = new JMenuItem(editor.bind(label1, (Action) new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 3)));
            fileMenu.add(menuItem1, 3);
            String label2 = mxResources.get("traceabilityMaker", null, "Vertical Traceability");
            JMenuItem menuItem2 = new JMenuItem(editor.bind(label2, new VerticalTraceController(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 2)));
            fileMenu.add(menuItem2, 3);
        }
        mxGraph graph = generateDiagram();

        e4jInstance.setVisible(true);
        this.setVisible(false);
    }

    // Gera e exibe o diagrama de Casos de Uso UML com base nas informações obtidas
    private mxGraph generateDiagram() throws IOException {
        ArrayList<Actor> useCases = Controller.getUseCases();
        ArrayList<ActorISA> isas = Controller.getIsas();
        // comeca a atualizar o grafo
        mxGraph graph = ((BasicUseCasesEditor)e4jInstance.getEditor()).getGraphComponent().getGraph();
        graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
        graph.removeCells(graph.getChildEdges(graph.getDefaultParent()));
        graph.getModel().beginUpdate();
        Element value;
        File shapesFolder = new File("resources/shapes/use cases diagram/");
        File[] files = shapesFolder.listFiles(ShapeFilenameFilter.instance);
        String nodeXml = mxUtils.readFile(files[0].getAbsolutePath());
        mxStencilShape newShape = new mxStencilShape(nodeXml);
        String styleActor = "shape=" + newShape.getName() + ";";
        nodeXml = mxUtils.readFile(files[1].getAbsolutePath());
        newShape = new mxStencilShape(nodeXml);
        String styleCase = "shape=" + newShape.getName() + ";";
        mxGeometry geo;
        // HashMaps para quardar as celular dos atores e casos de uso
        HashMap<String, mxCell> actors = new HashMap<>();
        HashMap<String, mxCell> cases = new HashMap<>();
        mxCell aresta;
        mxCell ext;
        // variaveis para controlar as posicoes X e Y dos elementos no diagrama
        int yActor = 110;
        int yCase = 30;
        int xCase = 400;
        String cod;
        /*
         * Adicionar ao diagrama todos os atores com seus respectivos
         * casos de uso e arestas.
         */
        for (Actor actor : useCases) {
            // cria ator e sua geometria e estilo
            value = IStarUtils.createActorUseCase();
            value.setAttribute("label", actor.getName());
            geo = new mxGeometry(50, yActor, 80, 80);
            geo.setX(50);
            geo.setY(yActor);
            mxCell cell = new mxCell(value, geo, styleActor);
            cell.setVertex(true);
            System.out.println("" + cell.getStyle());
            actors.put(actor.getCod(), cell);
            graph.addCell(cell);
            // cria os casos de uso e arestas
            for (UseCase caso : actor.getUseCases()) {
                // cria caso de uso, sua geometria e estilo
                value = IStarUtils.createUseCase();
                value.setAttribute("label", caso.getName().replace("\"", ""));
                geo = new mxGeometry(xCase, yCase, 120, 60);
                xCase = xCase == 400 ? 700 : 400;
                geo.setX(xCase);
                geo.setY(yCase);
                yCase += 100;
                cod = caso.getCod();
                // se o caso de uso possui elemento decomposto guarda o codigo dele
                // pois é o elemento decomposto que é incluso por outros casos de uso
                if (caso.getCodDecomposedElement() != null) {
                    cod = caso.getCodDecomposedElement();
                }
                // verifica se o caso de uso ainda não foi adicionado no grafo
                if (cases.get(cod) == null) {
                    cases.put(cod, new mxCell(value, geo, styleCase));
                    cases.get(cod).setVertex(true);
                    graph.addCell(cases.get(cod));
                }
                // cria uma aresta entre o ator e seu caso de uso
                value = IStarUtils.createAssociation();
                aresta = (mxCell) graph.createEdge(graph.getDefaultParent(), null, value, actors.get(actor.getCod()), cases.get(cod), "straight;endArrow=none;noLabel=1;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                aresta.setEdge(true);
                aresta.setStyle("straight;endArrow=none;noLabel=1;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                aresta.setSource(actors.get(actor.getCod()));
                aresta.setTarget(cases.get(cod));
                graph.addCell(aresta);
            }
            yActor = yCase + 100;
        }
        // Adiciona as ligacoes do tipo << include >> e << extend >>
        for (Actor actor : useCases) {
            for (UseCase caso : actor.getUseCases()) {
                // cria as ligações do tipo << include >>
                for (Step step : caso.getSteps()) {
                    if (step.isInclude()) {
                        cod = caso.getCod();
                        // se o ator possui elemento decomposto, pega o código dele
                        if (caso.getCodDecomposedElement() != null) {
                            cod = caso.getCodDecomposedElement();
                        }
                        // cria a ligação include entre os casos de uso
                        value = IStarUtils.createInclude();
                        aresta = (mxCell) graph.createEdge(graph.getDefaultParent(), null, value, cases.get(cod), cases.get(step.getCod()), "straight;dashed=1;endArrow=open;endSize=14;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                        aresta.setSource(cases.get(cod));
                        aresta.setTarget(cases.get(step.getCod()));
                        aresta.setEdge(true);
                        aresta.setValue(value);
                        aresta.setStyle("straight;dashed=1;endArrow=open;endSize=14;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                        graph.addCell(aresta);
                        // cria as ligações do tipo << extend >>
                        for (Extend extend : step.getExtends()) {
                            // verifica se existe um caso de uso que é a extensão
                            if (cases.get(extend.getCod()) != null) {
                                value = IStarUtils.createExtend();
                                ext = (mxCell) graph.createEdge(graph.getDefaultParent(), null, value, cases.get(extend.getCod()), cases.get(cod), "straight;dashed=1;endArrow=open;endSize=14;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                                ext.setEdge(true);
                                ext.setSource(cases.get(extend.getCod()));
                                ext.setTarget(cases.get(cod));
                                ext.setValue(value);
                                ext.setStyle("straight;dashed=1;endArrow=open;endSize=14;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                                graph.addCell(ext);
                            }
                        }
                    }
                }
            }
        }
        // adiciona as ligações do tipo ISA (generalização de atores)
        for (ActorISA isa : isas) {
            for (int i = 0; i < isa.getCodFathers().size(); i++) {
                String codFather = isa.getCodFathers().get(i);
                String nameFather = isa.getNameFathers().get(i);
                // cria a ligação generalization entre os atores
                value = IStarUtils.createGeneralization();
                aresta = (mxCell) graph.createEdge(graph.getDefaultParent(), null, value, actors.get(codFather), actors.get(isa.getCod()), "straight;noLabel=1;endArrow=diamond;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                aresta.setEdge(true);
                // verifica se o ator está no diagrama
                if (actors.get(codFather) == null) {
                    // cria ator e sua geometria e estilo
                    value = IStarUtils.createActorUseCase();
                    value.setAttribute("label", nameFather);
                    geo = new mxGeometry(50, yActor, 80, 80);
                    geo.setX(50);
                    geo.setY(yActor);
                    actors.put(codFather, new mxCell(value, geo, styleActor));
                    actors.get(codFather).setVertex(true);
                    graph.addCell(actors.get(codFather));
                    yActor += 100;
                }
                aresta.setSource(actors.get(codFather));
                // verifica se o ator está no diagrama
                if (actors.get(isa.getCod()) == null) {
                    // cria ator e sua geometria e estilo
                    value = IStarUtils.createActorUseCase();
                    value.setAttribute("label", isa.getName());
                    geo = new mxGeometry(50, yActor, 80, 80);
                    geo.setX(50);
                    geo.setY(yActor);
                    actors.put(isa.getCod(), new mxCell(value, geo, styleActor));
                    actors.get(isa.getCod()).setVertex(true);
                    graph.addCell(actors.get(isa.getCod()));
                    yActor += 100;
                }
                aresta.setTarget(actors.get(isa.getCod()));
                aresta.setValue(value);
                aresta.setStyle("straight;noLabel=1;endArrow=block;endFill=0;endSize=14;shape=curvedEdge;edgeStyle=curvedEdgeStyle");
                graph.addCell(aresta);
            }
        }
        graph.getModel().endUpdate();
        return graph;

    }

    private void setSelectedActor(Actor actor) {
        selectedActor = actor;
    }

    private Actor getSelectedActor() {
        return this.selectedActor;
    }

    public String getSelectedCase() {
        return selectedCase;
    }

    public void setSelectedCase(String selectedCase) {
        this.selectedCase = selectedCase;
    }

    private void showSaveUseCases() {
        String path = Controller.loadProperties();
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileFilter(new FiltroDOC());
        int resultado = fileChooser.showSaveDialog(null);
        Controller.saveProperties(fileChooser.getSelectedFile().getParent());
        if (resultado == JFileChooser.CANCEL_OPTION) {
            fileChooser.setVisible(false);
        } else {
            try {
                File file = fileChooser.getSelectedFile();
                try ( FileWriter writer = new FileWriter(file.getPath() + ".doc", true)) {
                    for (Actor actor : Controller.getUseCases()) {
                        for (UseCase caso : actor.getUseCases()) {
                            writer.write("Use Case: " + caso.getName() + "\n\n"
                                    + "CHARACTERISTIC INFORMATION\n"
                                    + "	Goal in Context:\n"
                                    + "	Scope:\n"
                                    + "	Preconditions:\n"
                                    + "	Success End Condition:\n"
                                    + "	Failed End Condition:\n"
                                    + "	Primary Actor: " + actor.getName() + "\n"
                                    + "\nMAIN SUCESS SCENARIO\n");
                            if (caso.getSteps().isEmpty()) {
                                writer.write("	1. Description Step 1 [<< include >> optional]\n"
                                        + "	2. Description Step 2 [<< include >> optional]\n"
                                        + "	3. Description Step 3 [<< include >> optional]\n"
                                        + "\nEXTENSIONS\n"
                                        + "	2.1. Extension Step 2 << extend >>\n"
                                        + "	2.2. Extension Step 2 << extend >>\n"
                                        + "	3.1. Extension Step 3 << extend >>\n");
                            } else {
                                int cont = 1;
                                int n = caso.getSteps().size();
                                Step step;
                                for (int i = n - 1; i >= 0; i--) {
                                    step = caso.getSteps().get(i);
                                    String name = step.getName().replaceAll("\"", "");
                                    if (step.isInclude()) {
                                        name += " << include >> ";
                                    }
                                    if (step.getExtends().isEmpty()) {
                                        if (step.isInclude()) {
                                            name += " << include >> ";
                                        }
                                        writer.write("	" + cont++ + ". " + name + "\n");
                                    } else {
                                        int m = step.getExtends().size() - 1;
                                        name += " " + step.getExtend(m).getName().replaceAll("\"", "");
                                        if (step.isInclude()) {
                                            name += " << include >> ";
                                        }
                                        writer.write("	" + cont++ + ". " + name + "\n");
                                    }
                                }
                                writer.write("\nEXTENSIONS\n");
                                int aux = caso.getSteps().size();
                                cont = 1;
                                for (int i = n - 1; i >= 0; i--) {
                                    step = caso.getSteps().get(i);
                                    Extend extend;
                                    n = step.getExtends().size();
                                    if (n < 2) {
                                        aux--;
                                    } else {
                                        for (int j = n - 2; j >= 0; j--) {
                                            extend = step.getExtend(j);
                                            writer.write("	" + cont + "." + (n - j - 1) + "." + extend.getName().replaceAll("\"", "") + " << extend >>\n");
                                        }
                                    }
                                    cont++;
                                    if (aux == 0) {
                                        writer.write("	2.1. Extension Step 2 << extend >>\n"
                                                + "	2.2. Extension Step 2 << extend >>\n"
                                                + "	3.1. Extension Step 3 << extend >>\n");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void showE4JiStar() throws HeadlessException, IOException {
        if (E4JiStar == null) {
            E4JiStar = new EditorJFrame(0);
            E4JiStar.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            E4JiStar.setIconImage(iconJGOOSE);
            E4JiStar.setExtendedState(MAXIMIZED_BOTH);
            EditorWindowListener windowListener = new EditorWindowListener(this, E4JiStar);
            this.addWindowListener(windowListener);
            E4JiStar.addWindowListener(windowListener);
            this.addWindowListener(windowListener);
            BasicIStarEditor editor = (BasicIStarEditor) E4JiStar.getEditor();
            JMenuBar menubar = E4JiStar.getJMenuBar();
            JMenu fileMenu = ((EditorMenuBar) menubar).getFileMenu();
            String label = mxResources.get("useCaseMaker", null, "Generate Use Cases");
            JMenuItem menuItem = new JMenuItem(editor.bind(label, new ImportIStarGraph(E4JiStar)));
            fileMenu.add(menuItem, 3);
            fileMenu.add(new JPopupMenu.Separator(), 4);
            String label1 = mxResources.get("traceabilityMaker", null, "Horizontal Traceability");
            JMenuItem menuItem1 = new JMenuItem(editor.bind(label1, new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 1)));
            fileMenu.add(menuItem1, 3);
            fileMenu.add(new JPopupMenu.Separator(), 4);
            String label2 = mxResources.get("traceabilityMaker", null, "Vertical Traceability");
            JMenuItem menuItem2 = new JMenuItem(editor.bind(label2, new VerticalTraceController(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 2)));
            fileMenu.add(menuItem2, 3);
            fileMenu.add(new JPopupMenu.Separator(), 4);
        }
        E4JiStar.setVisible(true);
        this.setVisible(false);
    }

    private void showE4JBPMN() {
        try {
            if (E4JBPMN == null) {
                E4JBPMN = new EditorJFrame(2);
                E4JBPMN.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                EditorWindowListener windowListener = new EditorWindowListener(this, E4JBPMN);
                E4JBPMN.setIconImage(iconJGOOSE);
                E4JBPMN.setExtendedState(MAXIMIZED_BOTH);
                this.addWindowListener(windowListener);
                E4JBPMN.addWindowListener(windowListener);
                this.addWindowListener(windowListener);
                bpmnEditor = (BasicBPMNEditor) E4JBPMN.getEditor();

                // get diagram menu bar and shows option to derive Use Cases
                JMenuBar menubar = E4JBPMN.getJMenuBar();
                JMenu fileMenu = ((EditorMenuBar) menubar).getFileMenu();
                String label1 = mxResources.get("traceabilityMaker", null, "Horizontal Traceability");
                JMenuItem menuItem1 = new JMenuItem(bpmnEditor.bind(label1,  new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 2)));
                fileMenu.add(menuItem1, 3);
                fileMenu.add(new JPopupMenu.Separator(), 4);
                String label2 = mxResources.get("traceabilityMaker", null, "Vertical Traceability");
                JMenuItem menuItem2 = new JMenuItem(bpmnEditor.bind(label2, new VerticalTraceController(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 1)));
                fileMenu.add(menuItem2, 3);
                fileMenu.add(new JPopupMenu.Separator(), 4);
                String label = mxResources.get("useCaseMaker", null, "Generate Use Cases");
                JMenuItem menuItem = new JMenuItem(bpmnEditor.bind(label, new ImportBPMNGraph(E4JBPMN)));
                fileMenu.add(menuItem, 3);
                fileMenu.add(new JPopupMenu.Separator(), 4);

            }
            E4JBPMN.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(e.toString());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("\n\tat ");
                sb.append(ste);
            }
            String trace = sb.toString();
            JOptionPane.showMessageDialog(null, trace);
        }
    }

    private void showE4JUseCases() throws HeadlessException, IOException {
        if (E4JUseCases == null) {
            E4JUseCases = new EditorJFrame(1);
            E4JUseCases.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            E4JUseCases.setIconImage(iconJGOOSE);
            E4JUseCases.setExtendedState(MAXIMIZED_BOTH);
            EditorWindowListener windowListener = new EditorWindowListener(this, E4JUseCases);
            this.addWindowListener(windowListener);
            E4JUseCases.addWindowListener(windowListener);
            this.addWindowListener(windowListener);
            BasicUseCasesEditor editor = (BasicUseCasesEditor) E4JUseCases.getEditor();
            JMenuBar menubar = E4JUseCases.getJMenuBar();
            JMenu fileMenu = ((EditorMenuBar) menubar).getFileMenu();
            String label1 = mxResources.get("traceabilityMaker", null, "Horizontal Traceability");
            JMenuItem menuItem1 = new JMenuItem(editor.bind(label1, (Action) new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, this, useCasesViewBPMN, 3)));
            fileMenu.add(menuItem1, 3);
        }
        E4JUseCases.setVisible(true);
        this.setVisible(false);
    }

    private void saveDescription() {
        String path = Controller.loadProperties();
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileFilter(new FiltroDOC());
        int resultado = fileChooser.showSaveDialog(null);
        Controller.saveProperties(fileChooser.getSelectedFile().getParent());
        if (resultado == JFileChooser.CANCEL_OPTION) {
            fileChooser.setVisible(false);
        } else {
            try {
                File file = fileChooser.getSelectedFile();
                try ( FileWriter writer = new FileWriter(file.getPath() + ".doc", true)) {
                    writer.write(textUseCases.getText());
                }
            } catch (IOException ex) {
                Logger.getLogger(UseCasesViewIStar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void bpmnUCView() {
        try {
            if (useCasesViewBPMN == null) {
                useCasesViewBPMN = new UseCasesViewBPMN(E4JiStar, E4JBPMN, E4JUseCases, this);
                useCasesViewBPMN.setIconImage(iconJGOOSE);
                useCasesViewBPMN.setExtendedState(MAXIMIZED_BOTH);
                useCasesViewBPMN.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                EditorWindowListener windowListener = new EditorWindowListener(this, useCasesViewBPMN);
                this.addWindowListener(windowListener);
                useCasesViewBPMN.addWindowListener(windowListener);
                this.addWindowListener(windowListener);
            }
            useCasesViewBPMN.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(e.toString());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("\n\tat ");
                sb.append(ste);
            }
            String trace = sb.toString();
            JOptionPane.showMessageDialog(null, trace);
        }
    }

//BUTTON RENDERER CLASS
    class ButtonRenderer extends JButton implements TableCellRenderer {

        //CONSTRUCTOR
        public ButtonRenderer() {
            //SET BUTTON PROPERTIES
            setOpaque(true);
            setBackground(new java.awt.Color(255, 255, 255));
            setForeground(new java.awt.Color(15, 157, 229));
            setBorder(null);
            Icon icon = new ImageIcon("./src/main/resources/icons/info-icon.png");
            setIcon(icon);
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object obj,
                boolean selected, boolean focused, int row, int col) {

            //SET PASSED OBJECT AS BUTTON TEXT
            setText((obj == null) ? "" : obj.toString());

            return this;
        }

    }

//BUTTON EDITOR CLASS
    class ButtonEditor extends DefaultCellEditor {

        private List informations;
        protected JButton btn;
        private String lbl;
        private Boolean clicked;
        private int row;

        public ButtonEditor(JTextField txt) {
            super(txt);

            btn = new JButton();
            btn.setOpaque(true);

            //WHEN BUTTON IS CLICKED
            btn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    fireEditingStopped();
                }
            });
        }

        public String[] findElementAndInfos(int row) {
            int cont = 0;
            for (UseCase useCase : Controller.getallUseCases()) {
                if (row == cont) {
                    String vetCasosDeUso[] = new String[6];
                    vetCasosDeUso[0] = String.valueOf(useCase.getId());
                    vetCasosDeUso[1] = useCase.getName();
                    vetCasosDeUso[2] = useCase.getPrimaryActor() == null ?  "" : useCase.getPrimaryActor().getName();
                    vetCasosDeUso[3] = useCase.getType();
                    return vetCasosDeUso;
                }
                cont++;
            }
            return null;
        }

        //OVERRIDE A COUPLE OF METHODS
        @Override
        public Component getTableCellEditorComponent(JTable table, Object obj,
                boolean selected, int row, int col) {

            //SET TEXT TO BUTTON,SET CLICKED TO TRUE,THEN RETURN THE BTN OBJECT
            lbl = (obj == null) ? "" : obj.toString();
            btn.setText(lbl);
            btn.setOpaque(true);
            btn.setBackground(new java.awt.Color(255, 255, 255));
            btn.setForeground(new java.awt.Color(15, 157, 229));
            btn.setBorder(null);
            btn.setBorderPainted(false);
            btn.setText(lbl);
            clicked = true;
            this.row = row;
            return btn;
        }

        //IF BUTTON CELL VALUE CHNAGES,IF CLICKED THAT IS
        @Override
        public Object getCellEditorValue() {

            if (clicked) {
                //SHOW US SOME MESSAGE
                String vetCasosDeUso[] = findElementAndInfos(row);
                MainView mainView = new MainView();
                MoreInfoUCFromIStar info = new MoreInfoUCFromIStar(mainView, true, vetCasosDeUso);
                info.setVisible(true);
            }
            //SET IT TO FALSE NOW THAT ITS CLICKED
            clicked = false;
            return new String(lbl);
        }

        @Override
        public boolean stopCellEditing() {

            //SET CLICKED TO FALSE FIRST
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            // TODO Auto-generated method stub
            super.fireEditingStopped();
        }
    }

}

//BUTTON RENDERER CLASS
class ButtonRendererDelete1 extends JButton implements TableCellRenderer {

    //CONSTRUCTOR
    public ButtonRendererDelete1() {
        //SET BUTTON PROPERTIES
        setOpaque(true);
        setOpaque(true);
        setBackground(new java.awt.Color(255, 255, 255));
        setForeground(new java.awt.Color(15, 157, 229));
        setBorder(null);
        Icon icon = new ImageIcon("./src/main/resources/icons/trash-icon.png");
        setIcon(icon);
        setBorderPainted(false);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object obj,
            boolean selected, boolean focused, int row, int col) {

        //SET PASSED OBJECT AS BUTTON TEXT
        setText((obj == null) ? "" : obj.toString());

        return this;
    }

}

//BUTTON EDITOR CLASS
class ButtonDelete1 extends DefaultCellEditor {

    protected JButton btn;
    private String lbl;
    private Boolean clicked;
    private int row;

    public ButtonDelete1(JTextField txt, int row) {
        super(txt);

        btn = new JButton();

        //WHEN BUTTON IS CLICKED
        btn.addMouseListener((new MouseAdapter() {
            private Color background;

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("");
            }
        }));

        btn.addActionListener((ActionEvent e) -> {
            fireEditingStopped();
        });
    }

    //OVERRIDE A COUPLE OF METHODS
    @Override
    public Component getTableCellEditorComponent(JTable table, Object obj,
            boolean selected, int row, int col) {

        //SET TEXT TO BUTTON,SET CLICKED TO TRUE,THEN RETURN THE BTN OBJECT
        lbl = (obj == null) ? "" : obj.toString();
        btn.setText(lbl);
        btn.setOpaque(true);
        btn.setBackground(new java.awt.Color(255, 255, 255));
        btn.setForeground(new java.awt.Color(15, 157, 229));
        btn.setBorder(null);
        btn.setBorderPainted(false);
        clicked = true;
        this.row = row;
        return btn;
    }

    //IF BUTTON CELL VALUE CHNAGES,IF CLICKED THAT IS
    @Override
    public Object getCellEditorValue() {

        if (clicked) {
            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(null, "would you like to delete?", "Warning", dialogButton);
            if (dialogResult == JOptionPane.YES_OPTION) {
                System.out.println("YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
            }

        }
        //SET IT TO FALSE NOW THAT ITS CLICKED
        clicked = false;
        return new String(lbl);
    }

    @Override
    public boolean stopCellEditing() {

        //SET CLICKED TO FALSE FIRST
        clicked = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        // TODO Auto-generated method stub
        super.fireEditingStopped();
    }
}

