package br.unioeste.jgoose.view;

import br.unioeste.jgoose.TraceabilityHorizontal.TraceBPMNHorizontal;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceBPMNVertical;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceIStarHorizontal;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceIStarVertical;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceUCHorizontal;
import br.unioeste.jgoose.blockchain.AuditoriaDialog;
import br.unioeste.jgoose.UseCases.Actor;
import br.unioeste.jgoose.controller.BPMNController;
import br.unioeste.jgoose.controller.Controller;
import br.unioeste.jgoose.controller.EditorWindowListener;
import br.unioeste.jgoose.controller.HorizontalBPMNTraceController;
import br.unioeste.jgoose.controller.HorizontalControler;
import br.unioeste.jgoose.controller.HorizontalIStarTraceController;
import br.unioeste.jgoose.controller.HorizontalUseCaseTraceController;
import br.unioeste.jgoose.controller.ImportBPMNGraph;
import br.unioeste.jgoose.controller.ImportIStarGraph;
import br.unioeste.jgoose.controller.VerticalTraceController;
import br.unioeste.jgoose.e4j.swing.BasicBPMNEditor;
import br.unioeste.jgoose.e4j.swing.BasicIStarEditor;
import br.unioeste.jgoose.e4j.swing.BasicUseCasesEditor;
import br.unioeste.jgoose.e4j.swing.EditorJFrame;
import br.unioeste.jgoose.e4j.swing.menubar.EditorMenuBar;
import br.unioeste.jgoose.model.TokensTraceability;
import br.unioeste.jgoose.model.TracedElement;
import br.unioeste.jgoose.model.UCActor;
import br.unioeste.jgoose.model.UCUseCase;
import com.mxgraph.util.mxResources;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import static com.itextpdf.text.Element.ALIGN_MIDDLE;
import static javax.swing.text.StyleConstants.ALIGN_CENTER;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 *
 * @author Victor Augusto Pozzan
 */
public final class TraceabilityView1 extends javax.swing.JFrame {

    private static Matriz matriz;
    private Integer type;
    private DefaultTableModel traceabilityInfo = new DefaultTableModel();
    private TokensTraceability lista;
    private ArrayList<String[]> elementTracedReport;
    private Actor selectedActor = null;
    private String selectedCase = "";

    private EditorJFrame e4jInstance = null;
    private EditorJFrame E4JiStar = null;
    private EditorJFrame E4JUseCases = null;
    private EditorJFrame E4JBPMN = null;
    private UseCasesViewBPMN useCasesViewBPMN = null;
    private UseCasesViewIStar useCasesViewIStar = null;

    private final Image iconJGOOSE = Toolkit.getDefaultToolkit().getImage("./src/main/resources/icons/jgoose.gif");
    private BasicBPMNEditor bpmnEditor;
    Font roboto;

    /**
     * Creates new form UseCasesView
     *
     * @param type
     * @param E4JiStar
     * @param E4JBPMN
     * @param E4JUseCases
     * @param useCasesViewIStar
     * @param useCasesViewBPMN
     */
    public TraceabilityView1(int type, EditorJFrame E4JiStar, EditorJFrame E4JBPMN, EditorJFrame E4JUseCases,
            UseCasesViewIStar useCasesViewIStar, UseCasesViewBPMN useCasesViewBPMN) {

        this.E4JBPMN = E4JBPMN;
        this.E4JiStar = E4JiStar;
        this.E4JUseCases = E4JUseCases;
        this.useCasesViewIStar = useCasesViewIStar;
        this.useCasesViewBPMN = useCasesViewBPMN;

        this.type = type;
        this.elementTracedReport = new ArrayList<>();
        initComponents();
        AuditoriaDialog.instalarMenu(this); // menu "Integridade > Auditar com a blockchain..."
        Image Icone;
        Icone = Toolkit.getDefaultToolkit().getImage("./src/main/resources/icons/jgoose.gif");
        setIconImage(Icone);
        this.setDefaultCloseOperation(TraceabilityView.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setExtendedState(MAXIMIZED_BOTH);
        this.setVisible(true);
        configTraceabilityView();
        configSideButtons();
    }

    private void configTraceabilityView() {
        switch (type) {
            case 1: //Horizontal BPMN
                lista = TraceBPMNHorizontal.getLista();
                labelTypeTraceability.setText("Traceability Horizontal");
                break;
            case 2: //Horizontal UC
                lista = TraceUCHorizontal.getLista();
                labelTypeTraceability.setText("Traceability Horizontal");
                break;
            case 3://Horizonal i*
                labelTypeTraceability.setText("Traceability Horizontal");
                lista = TraceIStarHorizontal.getLista();
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                labelTypeTraceability.setText("Vertical Traceability");
                lista = TraceBPMNVertical.getLista();
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                labelTypeTraceability.setText("Vertical Traceability");
                lista = TraceIStarVertical.getLista();
                break;
        }
    }

    private void configSideButtons() {
        if (type <= 3) {
            btnMenuTraceHorizontal.setEnabled(false);
        } else {
            bntMenuTraceVertical.setEnabled(false);
        }
    }

    /*
     * Método que atualiza as Tabelas
     */
    public void updateTable() {
        switch (type) {
            case 1: //Horizontal BPMN
                updateTableBPMNHorizontalTraceability();
                break;
            case 2: //Horizontal UC
                updateTableUCHorizontalTraceability();
                break;
            case 3://Horizonal i*
                updateTableIStarHorizontalTraceability();
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                updateTableVerticalBPMNtoUCTraceability();
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                updateTableVerticalIStartoUCTraceability();
                break;
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
        jLabel1 = new javax.swing.JLabel();
        labelTypeTraceability = new javax.swing.JLabel();
        UIManager.put("ComboBox.background", new ColorUIResource(new java.awt.Color(253, 253, 253)));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(new java.awt.Color(244, 251, 255)));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(new java.awt.Color(15,157,229)));
        choiceMatrixTrace = new javax.swing.JComboBox<>();
        buttonSaveUseCases = new javax.swing.JButton();
        buttonHelp = new javax.swing.JButton();
        btnTraceability = new javax.swing.JButton();
        JLabelTracedElements1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableTraceability = new javax.swing.JTable(){
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
        jPanel2 = new javax.swing.JPanel();
        labelSearchIcon = new javax.swing.JLabel();
        jtfFilter = new javax.swing.JTextField();
        JLabelTracedElements = new javax.swing.JLabel();
        jButtonAddTracedElement = new javax.swing.JButton();
        jButtonAddTracedElement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainView mainView = new MainView();
                AddTraceElement addTraceElement = new AddTraceElement(mainView, true, type, lista);
                addTraceElement.setVisible(true);
                int option = addTraceElement.newTraceElement();
                if(option == AddUseCasefromBPMN.YES) {
                    System.out.println("result OK:"+ option);
                    switch (type) {
                        case 1: //Horizontal BPMN
                        updateTableBPMNHorizontalTraceability();
                        break;
                        case 2: //Horizontal UC
                        updateTableUCHorizontalTraceability();
                        break;
                        case 3://Horizonal i*
                        updateTableIStarHorizontalTraceability();
                        break;
                        case 4://Vertical rastreabilidade vertical BPMN to UC
                        updateTableVerticalBPMNtoUCTraceability();
                        break;
                        case 5://Vertical rastreabilidade vertical i* to UC
                        updateTableVerticalIStartoUCTraceability();
                        break;
                    }
                }
            }
        });
        jPanelMenuButtons = new javax.swing.JPanel();
        btnMenuHome = new javax.swing.JButton();
        btnMenuiStar = new javax.swing.JButton();
        btnMenuBPMN = new javax.swing.JButton();
        btnMenuUC = new javax.swing.JButton();
        btnUCIStarView = new javax.swing.JButton();
        bntMenuTraceVertical = new javax.swing.JButton();
        btnMenuTraceHorizontal = new javax.swing.JButton();
        btnUCViewBpmn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Use Cases");
        setLocationByPlatform(true);
        setMinimumSize(null);

        jPanelBackground.setBackground(new java.awt.Color(244, 244, 244));
        jPanelBackground.setMaximumSize(new java.awt.Dimension(3276799, 32767999));

        jPanelHeader.setBackground(new java.awt.Color(255, 255, 255));
        jPanelHeader.setMaximumSize(new java.awt.Dimension(32767999, 32767999));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undraw_file_analysis.png"))); // NOI18N

        labelTypeTraceability.setFont(new java.awt.Font("Roboto", 1, 24)); // NOI18N
        labelTypeTraceability.setForeground(new java.awt.Color(37, 172, 241));
        labelTypeTraceability.setText("Traceability");

        choiceMatrixTrace.setBackground(new java.awt.Color(244, 251, 255));
        choiceMatrixTrace.setEditable(true);
        choiceMatrixTrace.setFont(new java.awt.Font("Roboto", 1, 14));
        choiceMatrixTrace.setForeground(new java.awt.Color(101, 196, 245));
        choiceMatrixTrace.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Informação Externa x Informação Externa ", "Informação Externa x Informação Organizacional", "Informação Organizacional x Informação Organizacional", "Informação Organizacional x Requisitos", "Objetivo do Sistema x Objetivo do Sistema", "Objetivo do Sistema x Requisitos", "Stakeholder x Stakeholder", "Stakeholder x Requisitos", "Requisitos x Requisitos", "Requisitos x Informação Externa", "Objetivo do Sistema x Informação Organizacional", "Objetivo do Sistema x Ator Sistema", "Ator Sistema x Stakeholder" }));
        choiceMatrixTrace.setSelectedIndex(1);
        choiceMatrixTrace.setToolTipText("Choose a Matrix");
        choiceMatrixTrace.setBorder(null);
        choiceMatrixTrace.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        choiceMatrixTrace.setFocusable(false);
        choiceMatrixTrace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                choiceMatrixTraceActionPerformed(evt);
            }
        });

        buttonSaveUseCases.setBackground(new java.awt.Color(255, 255, 255));
        buttonSaveUseCases.setFont(new java.awt.Font("Roboto", 1, 14));
        buttonSaveUseCases.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pdf.png"))); // NOI18N
        buttonSaveUseCases.setText("Save PDF ");
        buttonSaveUseCases.setToolTipText("Save matrix in PDF ");
        buttonSaveUseCases.setBorder(null);
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

        buttonHelp.setBackground(new java.awt.Color(255, 255, 255));
        buttonHelp.setFont(new java.awt.Font("Roboto", 1, 14));
        buttonHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/helpicon.png"))); // NOI18N
        buttonHelp.setText("Help");
        buttonHelp.setToolTipText("Help");
        buttonHelp.setBorder(null);
        buttonHelp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonHelpMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonHelpMouseExited(evt);
            }
        });
        buttonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHelpActionPerformed(evt);
            }
        });

        btnTraceability.setBackground(new java.awt.Color(15, 157, 229));
        btnTraceability.setFont(new java.awt.Font("Roboto", 1, 14));
        btnTraceability.setForeground(new java.awt.Color(255, 255, 255));
        btnTraceability.setText("Traceability");
        btnTraceability.setToolTipText("Traceability");
        btnTraceability.setBorder(null);
        btnTraceability.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTraceabilityMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTraceabilityMouseExited(evt);
            }
        });
        btnTraceability.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraceabilityActionPerformed(evt);
            }
        });

        JLabelTracedElements1.setFont(new java.awt.Font("Roboto", 0, 20));
        JLabelTracedElements1.setForeground(new java.awt.Color(85, 190, 244));
        JLabelTracedElements1.setText("Choose a Matrix:");

        javax.swing.GroupLayout jPanelHeaderLayout = new javax.swing.GroupLayout(jPanelHeader);
        jPanelHeader.setLayout(jPanelHeaderLayout);
        jPanelHeaderLayout.setHorizontalGroup(
            jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(choiceMatrixTrace, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelTypeTraceability)
                            .addComponent(JLabelTracedElements1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnTraceability, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 188, Short.MAX_VALUE)
                .addComponent(buttonSaveUseCases, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelHeaderLayout.setVerticalGroup(
            jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelHeaderLayout.createSequentialGroup()
                        .addComponent(labelTypeTraceability, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JLabelTracedElements1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(choiceMatrixTrace, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSaveUseCases, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTraceability, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        choiceMatrixTrace.getEditor().getEditorComponent().setBackground(Color.YELLOW);
        ((JTextField) choiceMatrixTrace.getEditor().getEditorComponent()).setBackground(Color.YELLOW);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        tableTraceability.setAutoCreateRowSorter(true);
        tableTraceability.setFont(new java.awt.Font("Roboto", 1, 14)); // NOI18N
        tableTraceability.setForeground(new java.awt.Color(37, 172, 241));
        tableTraceability.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableTraceability.setGridColor(new java.awt.Color(252, 252, 252));
        tableTraceability.setRowHeight(30);
        tableTraceability.setSelectionBackground(new java.awt.Color(199, 235, 254));
        tableTraceability.setSelectionForeground(new java.awt.Color(37, 172, 241));
        tableTraceability.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTraceability.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTraceability.setShowHorizontalLines(false);
        tableTraceability.setShowVerticalLines(false);
        tableTraceability.setUpdateSelectionOnSort(false);
        tableTraceability.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableTraceability.getTableHeader().setReorderingAllowed(false);

        tableTraceability.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent evt) {
                tableTraceability.repaint();
                tableTraceabilityValueChanged(evt);
            }
        });

        tableTraceability.getTableHeader().setFont(new java.awt.Font("Roboto", 0, 14));
        jScrollPane1.setViewportView(tableTraceability);
        if (tableTraceability.getColumnModel().getColumnCount() > 0) {
            tableTraceability.getColumnModel().getColumn(0).setResizable(false);
            tableTraceability.getColumnModel().getColumn(0).setHeaderValue("ID");
            tableTraceability.getColumnModel().getColumn(1).setResizable(false);
            tableTraceability.getColumnModel().getColumn(1).setHeaderValue("");
            tableTraceability.getColumnModel().getColumn(2).setResizable(false);
            tableTraceability.getColumnModel().getColumn(2).setHeaderValue("");
        }
        tableTraceability.getAccessibleContext().setAccessibleName("");
        tableTraceability.getAccessibleContext().setAccessibleDescription("");
        tableTraceability.getTableHeader().setForeground(new java.awt.Color(71, 92, 84));

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
        jtfFilter.setToolTipText("Seach");
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
                .addComponent(jtfFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
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

        JLabelTracedElements.setFont(new java.awt.Font("Roboto", 0, 20));
        JLabelTracedElements.setForeground(new java.awt.Color(85, 190, 244));
        JLabelTracedElements.setText("Traced Elements");

        jButtonAddTracedElement.setBackground(new java.awt.Color(15, 157, 229));
        jButtonAddTracedElement.setFont(new java.awt.Font("Roboto", 0, 18)); // NOI18N
        jButtonAddTracedElement.setForeground(new java.awt.Color(250, 250, 250));
        jButtonAddTracedElement.setText("Add a new Element");
        jButtonAddTracedElement.setToolTipText("Add a new Element");
        jButtonAddTracedElement.setBorder(null);
        jButtonAddTracedElement.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonAddTracedElementMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonAddTracedElementMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(JLabelTracedElements)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonAddTracedElement, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JLabelTracedElements, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddTracedElement, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanelBackgroundLayout = new javax.swing.GroupLayout(jPanelBackground);
        jPanelBackground.setLayout(jPanelBackgroundLayout);
        jPanelBackgroundLayout.setHorizontalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addGroup(jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelBackgroundLayout.setVerticalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelMenuButtons.setBackground(new java.awt.Color(11, 113, 165));

        btnMenuHome.setBackground(new java.awt.Color(11, 113, 165));
        btnMenuHome.setForeground(new java.awt.Color(11, 113, 165));
        btnMenuHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-Home.png"))); // NOI18N
        btnMenuHome.setToolTipText("Home");
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
        btnMenuiStar.setToolTipText("Editor i* ");
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
        btnMenuBPMN.setToolTipText("Editor BPMN");
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
        btnMenuUC.setToolTipText("Editor Use Case");
        btnMenuUC.setBorder(null);
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

        btnUCIStarView.setBackground(new java.awt.Color(11, 113, 165));
        btnUCIStarView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-UC-iStar.png"))); // NOI18N
        btnUCIStarView.setBorder(null);
        btnUCIStarView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUCIStarViewMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUCIStarViewMouseExited(evt);
            }
        });
        btnUCIStarView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUCIStarViewActionPerformed(evt);
            }
        });

        bntMenuTraceVertical.setBackground(new java.awt.Color(11, 113, 165));
        bntMenuTraceVertical.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-VerticalTraceability.png"))); // NOI18N
        bntMenuTraceVertical.setToolTipText("Vertical Traceability");
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
        btnMenuTraceHorizontal.setToolTipText("Horizontal Traceability");
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

        btnUCViewBpmn.setBackground(new java.awt.Color(11, 113, 165));
        btnUCViewBpmn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ICON-UC-BPMN.png"))); // NOI18N
        btnUCViewBpmn.setBorder(null);
        btnUCViewBpmn.setFocusable(false);
        btnUCViewBpmn.setOpaque(false);
        btnUCViewBpmn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUCViewBpmnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUCViewBpmnMouseExited(evt);
            }
        });
        btnUCViewBpmn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUCViewBpmnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMenuButtonsLayout = new javax.swing.GroupLayout(jPanelMenuButtons);
        jPanelMenuButtons.setLayout(jPanelMenuButtonsLayout);
        jPanelMenuButtonsLayout.setHorizontalGroup(
            jPanelMenuButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnMenuBPMN, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnMenuUC, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnUCIStarView, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(bntMenuTraceVertical, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnMenuiStar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnMenuHome, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnMenuTraceHorizontal, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnUCViewBpmn, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(btnUCIStarView, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(btnUCViewBpmn, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

    private void jtfFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtfFilterActionPerformed

    private void btnMenuHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuHomeActionPerformed
        MainView mainView = new MainView(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, E4JiStar);
        this.setVisible(false);
        mainView.setVisible(true);
    }//GEN-LAST:event_btnMenuHomeActionPerformed

    private void btnMenuTraceHorizontalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalActionPerformed
        this.setVisible(false);
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

    private void btnUCIStarViewMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUCIStarViewMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnUCIStarView.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnUCIStarViewMouseEntered

    private void btnUCIStarViewMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUCIStarViewMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnUCIStarView.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnUCIStarViewMouseExited

    private void bntMenuTraceVerticalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalMouseEntered
        if (type <= 3) {
            setCursor(Cursor.HAND_CURSOR);
            bntMenuTraceVertical.setBackground(new java.awt.Color(59, 141, 183));
        }
    }//GEN-LAST:event_bntMenuTraceVerticalMouseEntered

    private void bntMenuTraceVerticalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalMouseExited
        if (type <= 3) {
            setCursor(Cursor.DEFAULT_CURSOR);
            bntMenuTraceVertical.setBackground(new java.awt.Color(11, 113, 165));
        }
    }//GEN-LAST:event_bntMenuTraceVerticalMouseExited

    private void btnMenuTraceHorizontalMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalMouseEntered
        if (type > 3) {
            setCursor(Cursor.HAND_CURSOR);
            btnMenuTraceHorizontal.setBackground(new java.awt.Color(59, 141, 183));
        }
    }//GEN-LAST:event_btnMenuTraceHorizontalMouseEntered

    private void btnMenuTraceHorizontalMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMenuTraceHorizontalMouseExited
        if (type > 3) {
            setCursor(Cursor.DEFAULT_CURSOR);
            btnMenuTraceHorizontal.setBackground(new java.awt.Color(11, 113, 165));
        }
    }//GEN-LAST:event_btnMenuTraceHorizontalMouseExited

    private void jButtonAddTracedElementMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAddTracedElementMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        jButtonAddTracedElement.setBackground(new java.awt.Color(69, 185, 243));
    }//GEN-LAST:event_jButtonAddTracedElementMouseEntered

    private void jButtonAddTracedElementMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonAddTracedElementMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        jButtonAddTracedElement.setBackground(new java.awt.Color(15, 157, 229));
    }//GEN-LAST:event_jButtonAddTracedElementMouseExited

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
            Logger.getLogger(TraceabilityView1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnMenuUCActionPerformed

    private void btnUCIStarViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUCIStarViewActionPerformed
        /* try {
            if (useCasesViewIStar == null) {
                useCasesViewIStar = new UseCasesViewIStar(E4JiStar, E4JBPMN, E4JUseCases, this);
                useCasesViewIStar.setIconImage(iconJGOOSE);
                useCasesViewIStar.setExtendedState(MAXIMIZED_BOTH);
                useCasesViewIStar.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                EditorWindowListener windowListener = new EditorWindowListener(this, useCasesViewIStar);
                this.addWindowListener(windowListener);
                useCasesViewIStar.addWindowListener(windowListener);
                this.addWindowListener(windowListener);
            }
            useCasesViewIStar.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(e.toString());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("\n\tat ");
                sb.append(ste);
            }
            String trace = sb.toString();
            JOptionPane.showMessageDialog(null, trace);
        }*/
    }//GEN-LAST:event_btnUCIStarViewActionPerformed

    private void bntMenuTraceVerticalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntMenuTraceVerticalActionPerformed
        this.setVisible(false);
        VerticalTraceController.openVerticalTraceabilityView();
    }//GEN-LAST:event_bntMenuTraceVerticalActionPerformed

    private void buttonSaveUseCasesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesActionPerformed
        Document document = new Document(PageSize.A4.rotate());

        String path = Controller.loadProperties();
        JFileChooser fileChooser = new JFileChooser(path);
        int resultado = fileChooser.showSaveDialog(null);
        Controller.saveProperties(fileChooser.getSelectedFile().getParent());

        try {
            File file = fileChooser.getSelectedFile();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file.getPath() + ".pdf"));
            document.open();
            document.add(new Paragraph("A Traceability PDF document."));

            PdfPTable table = new PdfPTable(5); // 5 columns.
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table
            float[] columnWidths = {1f, 2f, 1f, 2f, 1f};
            table.setWidths(columnWidths);
            String[] cabeçalho = {"Abreviation", "Name", "Model", "Class", "Segment"};

            for (int i = 0; i < 5; i++) {//Cabeçalho da tabela
                PdfPCell cell1 = new PdfPCell(new Paragraph(cabeçalho[i]));
                cell1.setBorderColor(BaseColor.BLUE);
                cell1.setPaddingLeft(10);
                cell1.setHorizontalAlignment(ALIGN_CENTER);
                cell1.setVerticalAlignment(ALIGN_MIDDLE);
                table.addCell(cell1);
            }

            for (int i = 0; i < lista.getAtorSistema().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getAtorSistema().get(i).getAbreviacao();
                vetor[1] = lista.getAtorSistema().get(i).getLabel();
                vetor[2] = lista.getAtorSistema().get(i).getModel();
                vetor[3] = lista.getAtorSistema().get(i).getClasse();
                vetor[4] = lista.getAtorSistema().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }

            for (int i = 0; i < lista.getStakeholders().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getStakeholders().get(i).getAbreviacao();
                vetor[1] = lista.getStakeholders().get(i).getLabel();
                vetor[2] = lista.getStakeholders().get(i).getModel();
                vetor[3] = lista.getStakeholders().get(i).getClasse();
                vetor[4] = lista.getStakeholders().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }

            for (int i = 0; i < lista.getInformcaoExterna().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getInformcaoExterna().get(i).getAbreviacao();
                vetor[1] = lista.getInformcaoExterna().get(i).getLabel();
                vetor[2] = lista.getInformcaoExterna().get(i).getModel();
                vetor[3] = lista.getInformcaoExterna().get(i).getClasse();
                vetor[4] = lista.getInformcaoExterna().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }

            for (int i = 0; i < lista.getInformacaoOrg().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getInformacaoOrg().get(i).getAbreviacao();
                vetor[1] = lista.getInformacaoOrg().get(i).getLabel();
                vetor[2] = lista.getInformacaoOrg().get(i).getModel();
                vetor[3] = lista.getInformacaoOrg().get(i).getClasse();
                vetor[4] = lista.getInformacaoOrg().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }

            for (int i = 0; i < lista.getObjetivoSistema().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getObjetivoSistema().get(i).getAbreviacao();
                vetor[1] = lista.getObjetivoSistema().get(i).getLabel();
                vetor[2] = lista.getObjetivoSistema().get(i).getModel();
                vetor[3] = lista.getObjetivoSistema().get(i).getClasse();
                vetor[4] = lista.getObjetivoSistema().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }

            for (int i = 0; i < lista.getRequisitos().size(); i++) {
                String[] vetor = new String[5];
                vetor[0] = lista.getRequisitos().get(i).getAbreviacao();
                vetor[1] = lista.getRequisitos().get(i).getLabel();
                vetor[2] = lista.getRequisitos().get(i).getModel();
                vetor[3] = lista.getRequisitos().get(i).getClasse();
                vetor[4] = lista.getRequisitos().get(i).getFase();

                for (int j = 0; j < 5; j++) {
                    PdfPCell cell1 = new PdfPCell(new Paragraph(vetor[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }
            /*for (int i = 0; i < elementTracedReport.size(); i++) {//Conteúdo
                for (int j = 0; j < 5; j++) {
                    System.out.println("elementTracedReort:" + elementTracedReport.get(i)[j]);
                    PdfPCell cell1 = new PdfPCell(new Paragraph(elementTracedReport.get(i)[j]));
                    cell1.setBorderColor(BaseColor.BLUE);
                    cell1.setPaddingLeft(10);
                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                    table.addCell(cell1);
                }
            }*/
            document.add(table);

            //incluir matrizes
            for (int i = 0; i < 12; i++) {
                switch (type) {
                    case 1:
                    try {
                        matriz = HorizontalBPMNTraceController.propertiesMatriz(i);
                        document.add(new Paragraph(matriz.getTitle()));
                        PdfPTable tableMatriz = new PdfPTable(matriz.getSizeCol() + 1);
                        tableMatriz.setWidthPercentage(100); //Width 50%
                        tableMatriz.setSpacingBefore(1f); //Space before table
                        tableMatriz.setSpacingAfter(1f); //Space after table

                        PdfPCell cel1 = new PdfPCell(new Paragraph("<-"));
                        cel1.setBorderColor(BaseColor.BLUE);
                        cel1.setPaddingLeft(10);
                        cel1.setHorizontalAlignment(ALIGN_CENTER);
                        cel1.setVerticalAlignment(ALIGN_MIDDLE);
                        tableMatriz.addCell(cel1);

                        for (int l = 0; l < matriz.getSizeCol(); l++) {
                            cel1 = new PdfPCell(new Paragraph(matriz.getElementColumn().get(l).getAbreviacao()));
                            cel1.setBorderColor(BaseColor.BLUE);
                            cel1.setPaddingLeft(10);
                            cel1.setHorizontalAlignment(ALIGN_CENTER);
                            cel1.setVerticalAlignment(ALIGN_MIDDLE);
                            tableMatriz.addCell(cel1);
                        }
                        boolean marcado = false;
                        for (int l = 0; l < matriz.getSizeRow(); l++) {
                            cel1 = new PdfPCell(new Paragraph(matriz.getElementRow().get(l).getAbreviacao()));
                            cel1.setBorderColor(BaseColor.BLUE);
                            cel1.setPaddingLeft(10);
                            cel1.setHorizontalAlignment(ALIGN_CENTER);
                            cel1.setVerticalAlignment(ALIGN_MIDDLE);
                            tableMatriz.addCell(cel1);
                            for (int k = 0; k < matriz.getElementColumn().size(); k++) {
                                for (int j = 0; j < matriz.getElementRow().get(l).getListConcflicts().size(); j++) {
                                    if (matriz.getElementRow().get(l).getListConcflicts().get(j)[0] == matriz.getElementColumn().get(k).getCode()) {
                                        if (matriz.isMatrizQuadrada() && l != k) {
                                            PdfPCell cell1 = new PdfPCell(new Paragraph(matriz.getElementRow().get(l).getListConcflicts().get(j)[1]));
                                            cell1.setBorderColor(BaseColor.BLUE);
                                            cell1.setPaddingLeft(10);
                                            cell1.setHorizontalAlignment(ALIGN_CENTER);
                                            cell1.setVerticalAlignment(ALIGN_MIDDLE);
                                            tableMatriz.addCell(cell1);
                                            marcado = true;
                                            j = matriz.getElementRow().get(l).getListConcflicts().size();
                                            break;
                                            //table.setValueAt(elementCol.get(l).getListConcflicts().get(j)[1], k, l);
                                        } else if (!matriz.isMatrizQuadrada()) {
                                            PdfPCell cell1 = new PdfPCell(new Paragraph(matriz.getElementRow().get(l).getListConcflicts().get(j)[1]));
                                            cell1.setBorderColor(BaseColor.BLUE);
                                            cell1.setPaddingLeft(10);
                                            cell1.setHorizontalAlignment(ALIGN_CENTER);
                                            cell1.setVerticalAlignment(ALIGN_MIDDLE);
                                            tableMatriz.addCell(cell1);
                                            marcado = true;
                                            break;
                                            // table.setValueAt(elementCol.get(l).getListConcflicts().get(j)[1], k, l);
                                        }
                                    }
                                }
                                if (!marcado) {
                                    PdfPCell cell1 = new PdfPCell(new Paragraph(" "));
                                    cell1.setBorderColor(BaseColor.BLUE);
                                    cell1.setPaddingLeft(10);
                                    cell1.setHorizontalAlignment(ALIGN_CENTER);
                                    cell1.setVerticalAlignment(ALIGN_MIDDLE);
                                    tableMatriz.addCell(cell1);
                                }
                                marcado = false;
                            }

                        }

                        document.add(tableMatriz);
                    } catch (Error e) {
                        System.out.println("Error: " + e);
                    }
                    break;
                }
            }

            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //this.saveDocTraceability(tableTraceability);
    }//GEN-LAST:event_buttonSaveUseCasesActionPerformed

    private void buttonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHelpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonHelpActionPerformed

    private void choiceMatrixTraceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choiceMatrixTraceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_choiceMatrixTraceActionPerformed

    private void btnTraceabilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraceabilityActionPerformed
        int indice = choiceMatrixTrace.getSelectedIndex();
        switch (type) {
            case 1: //Horizontal BPMN
                //try {
                HorizontalBPMNTraceController.selectMatriz(indice);
                //} catch (Exception error) {

                //JOptionPane.showMessageDialog(this, "MATRIZ ERROR" + error.getMessage());
                // }
                break;
            case 2: //Horizontal UC
            try {
                HorizontalUseCaseTraceController.selectMatriz(indice);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "MATRIZ ERROR" + error.getMessage());
            }
            break;
            case 3://Horizonal i*
            try {
                HorizontalIStarTraceController.selectMatriz(indice);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "MATRIZ ERROR" + error.getMessage());
            }
            break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
            case 5://Vertical rastreabilidade vertical i* to UC
            try {
                VerticalTraceController.selectMatriz(indice);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "MATRIZ ERROR" + error.getMessage());
            }
            break;

        }
    }//GEN-LAST:event_btnTraceabilityActionPerformed

    private void buttonSaveUseCasesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonSaveUseCases.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonSaveUseCasesMouseEntered

    private void buttonSaveUseCasesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonSaveUseCasesMouseExited
        buttonSaveUseCases.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_buttonSaveUseCasesMouseExited

    private void btnTraceabilityMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTraceabilityMouseExited
        btnTraceability.setBackground(new java.awt.Color(15, 157, 229));
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_btnTraceabilityMouseExited

    private void btnTraceabilityMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTraceabilityMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnTraceability.setBackground(new java.awt.Color(69, 185, 243));
    }//GEN-LAST:event_btnTraceabilityMouseEntered

    private void buttonHelpMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonHelpMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        buttonHelp.setBackground(new java.awt.Color(240, 240, 240));
    }//GEN-LAST:event_buttonHelpMouseEntered

    private void buttonHelpMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonHelpMouseExited
        buttonHelp.setBackground(new java.awt.Color(255, 255, 255));
        setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_buttonHelpMouseExited

    private void btnUCViewBpmnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUCViewBpmnMouseEntered
        setCursor(Cursor.HAND_CURSOR);
        btnUCViewBpmn.setBackground(new java.awt.Color(11, 113, 165));
    }//GEN-LAST:event_btnUCViewBpmnMouseEntered

    private void btnUCViewBpmnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUCViewBpmnMouseExited
        setCursor(Cursor.DEFAULT_CURSOR);
        btnUCViewBpmn.setBackground(new java.awt.Color(59, 141, 183));
    }//GEN-LAST:event_btnUCViewBpmnMouseExited

    private void btnUCViewBpmnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUCViewBpmnActionPerformed

    }//GEN-LAST:event_btnUCViewBpmnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel JLabelTracedElements;
    private javax.swing.JLabel JLabelTracedElements1;
    private javax.swing.JButton bntMenuTraceVertical;
    private javax.swing.JButton btnMenuBPMN;
    private javax.swing.JButton btnMenuHome;
    private javax.swing.JButton btnMenuTraceHorizontal;
    private javax.swing.JButton btnMenuUC;
    private javax.swing.JButton btnMenuiStar;
    private javax.swing.JButton btnTraceability;
    private javax.swing.JButton btnUCIStarView;
    private javax.swing.JButton btnUCViewBpmn;
    private javax.swing.JButton buttonHelp;
    private javax.swing.JButton buttonSaveUseCases;
    private javax.swing.JComboBox<String> choiceMatrixTrace;
    private javax.swing.JButton jButtonAddTracedElement;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelBackground;
    private javax.swing.JPanel jPanelHeader;
    private javax.swing.JPanel jPanelMenuButtons;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jtfFilter;
    private javax.swing.JLabel labelSearchIcon;
    private javax.swing.JLabel labelTypeTraceability;
    public javax.swing.JTable tableTraceability;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTextPane textUseCasesSave;

    /**
     * Atualiza as Tabelas relacionadas à rastreabilidade
     *
     */
    public void updateTableBPMNHorizontalTraceability() {
        /*
         * Atualiza Informações Arquivo
         */
        traceabilityInfo = new DefaultTableModel();
        tableTraceability.setAutoResizeMode(MAXIMIZED_HORIZ);
        traceabilityInfo.addColumn("Abreviation");
        traceabilityInfo.addColumn("Name");
        traceabilityInfo.addColumn("Model");
        traceabilityInfo.addColumn("Class");
        traceabilityInfo.addColumn("Segment");
        traceabilityInfo.addColumn(" ");
        traceabilityInfo.addColumn(" ");

        // adiciona os dados dos casos de uso no modelo da tabela
        for (int i = 0; i < HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().size(); i++) {
            String vetStakeholder[] = new String[7];
            vetStakeholder[0] = HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().get(i).getAbreviacao();
            vetStakeholder[1] = HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().get(i).getLabel();
            vetStakeholder[2] = HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().get(i).getModel();
            vetStakeholder[3] = HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().get(i).getClasse();
            vetStakeholder[4] = HorizontalBPMNTraceController.getTokensTraceability().getStakeholders().get(i).getFase();
            vetStakeholder[5] = "";
            vetStakeholder[6] = "";
            elementTracedReport.add(vetStakeholder);
            traceabilityInfo.addRow(vetStakeholder);
        }
        for (int i = 0; i < HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().size(); i++) {
            String vetInfExterna[] = new String[7];
            vetInfExterna[0] = HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().get(i).getAbreviacao();
            vetInfExterna[1] = HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().get(i).getLabel();
            vetInfExterna[2] = HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().get(i).getModel();
            vetInfExterna[3] = HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().get(i).getClasse();
            vetInfExterna[4] = HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna().get(i).getFase();
            vetInfExterna[5] = "";
            vetInfExterna[6] = "";
            elementTracedReport.add(vetInfExterna);
            traceabilityInfo.addRow(vetInfExterna);
        }
        for (int i = 0; i < HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().size(); i++) {
            String vetInfOrganizacional[] = new String[7];
            vetInfOrganizacional[0] = HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().get(i).getAbreviacao();
            vetInfOrganizacional[1] = HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().get(i).getLabel();
            vetInfOrganizacional[2] = HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().get(i).getModel();
            vetInfOrganizacional[3] = HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().get(i).getClasse();
            vetInfOrganizacional[4] = HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg().get(i).getFase();
            vetInfOrganizacional[5] = "";
            vetInfOrganizacional[6] = "";
            elementTracedReport.add(vetInfOrganizacional);
            traceabilityInfo.addRow(vetInfOrganizacional);
        }
        for (int i = 0; i < HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().size(); i++) {
            String vetObjetivoSistema[] = new String[7];
            vetObjetivoSistema[0] = HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().get(i).getAbreviacao();
            vetObjetivoSistema[1] = HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().get(i).getLabel();
            vetObjetivoSistema[2] = HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().get(i).getModel();
            vetObjetivoSistema[3] = HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().get(i).getClasse();
            vetObjetivoSistema[4] = HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema().get(i).getFase();
            vetObjetivoSistema[5] = "";
            vetObjetivoSistema[6] = "";
            elementTracedReport.add(vetObjetivoSistema);
            traceabilityInfo.addRow(vetObjetivoSistema);
        }
        for (int i = 0; i < HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().size(); i++) {
            String vetRequisitos[] = new String[7];
            vetRequisitos[0] = HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().get(i).getAbreviacao();
            vetRequisitos[1] = HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().get(i).getLabel();
            vetRequisitos[2] = HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().get(i).getModel();
            vetRequisitos[3] = HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().get(i).getClasse();
            vetRequisitos[4] = HorizontalBPMNTraceController.getTokensTraceability().getRequisitos().get(i).getFase();
            vetRequisitos[5] = "";
            vetRequisitos[6] = "";
            elementTracedReport.add(vetRequisitos);
            traceabilityInfo.addRow(vetRequisitos);
        }

        tableTraceability.setModel(traceabilityInfo);
        tableTraceability.getColumnModel().getColumn(0).setMaxWidth(100);
        tableTraceability.getColumnModel().getColumn(0).setMinWidth(100);
        tableTraceability.getColumnModel().getColumn(1).setPreferredWidth(MAXIMIZED_HORIZ);
        tableTraceability.getColumnModel().getColumn(2).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(2).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(3).setMaxWidth(240);
        tableTraceability.getColumnModel().getColumn(3).setMinWidth(240);
        tableTraceability.getColumnModel().getColumn(4).setMaxWidth(200);
        tableTraceability.getColumnModel().getColumn(4).setMinWidth(200);
        tableTraceability.getColumnModel().getColumn(5).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(5).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMinWidth(80);

        tableTraceability.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer2());
        tableTraceability.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor2(new JTextField()));

        tableTraceability.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererDelete());
        tableTraceability.getColumnModel().getColumn(6).setCellEditor(new ButtonDelete(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableTraceability.getModel());
        tableTraceability.setRowSorter(rowSorter);
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

    public void updateTableUCHorizontalTraceability() {
        traceabilityInfo = new DefaultTableModel();
        tableTraceability.setAutoResizeMode(MAXIMIZED_HORIZ);
        traceabilityInfo.addColumn("Abreviation");
        traceabilityInfo.addColumn("Name");
        traceabilityInfo.addColumn("Model");
        traceabilityInfo.addColumn("Class");
        traceabilityInfo.addColumn("Segment");
        traceabilityInfo.addColumn(" ");
        traceabilityInfo.addColumn(" ");

        String vetStakeholder[] = new String[7];
        for (int i = 0; i < HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().size(); i++) {
            vetStakeholder[0] = HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().get(i).getAbreviacao();
            vetStakeholder[1] = HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().get(i).getLabel();
            vetStakeholder[2] = HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().get(i).getModel();
            vetStakeholder[3] = HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().get(i).getClasse();
            vetStakeholder[4] = HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders().get(i).getFase();
            vetStakeholder[5] = "";
            vetStakeholder[6] = "";
            traceabilityInfo.addRow(vetStakeholder);
        }
        String vetInfExterna[] = new String[7];
        for (int i = 0; i < HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().size(); i++) {
            vetInfExterna[0] = HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().get(i).getAbreviacao();
            vetInfExterna[1] = HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().get(i).getLabel();
            vetInfExterna[2] = HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().get(i).getModel();
            vetInfExterna[3] = HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().get(i).getClasse();
            vetInfExterna[4] = HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna().get(i).getFase();
            vetInfExterna[5] = "";
            vetInfExterna[6] = "";
            traceabilityInfo.addRow(vetInfExterna);
        }
        String vetInfOrganizacional[] = new String[7];
        for (int i = 0; i < HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().size(); i++) {
            vetInfOrganizacional[0] = HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().get(i).getAbreviacao();
            vetInfOrganizacional[1] = HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().get(i).getLabel();
            vetInfOrganizacional[2] = HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().get(i).getModel();
            vetInfOrganizacional[3] = HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().get(i).getClasse();
            vetInfOrganizacional[4] = HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg().get(i).getFase();
            vetInfOrganizacional[5] = "";
            vetInfOrganizacional[6] = "";
            traceabilityInfo.addRow(vetInfOrganizacional);
        }
        String vetObjetivoSistema[] = new String[7];
        for (int i = 0; i < HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().size(); i++) {
            vetObjetivoSistema[0] = HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().get(i).getAbreviacao();
            vetObjetivoSistema[1] = HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().get(i).getLabel();
            vetObjetivoSistema[2] = HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().get(i).getModel();
            vetObjetivoSistema[3] = HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().get(i).getClasse();
            vetObjetivoSistema[4] = HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema().get(i).getFase();
            vetObjetivoSistema[5] = "";
            vetObjetivoSistema[6] = "";
            traceabilityInfo.addRow(vetObjetivoSistema);
        }
        String vetRequisitos[] = new String[7];
        for (int i = 0; i < HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().size(); i++) {
            vetRequisitos[0] = HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().get(i).getAbreviacao();
            vetRequisitos[1] = HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().get(i).getLabel();
            vetRequisitos[2] = HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().get(i).getModel();
            vetRequisitos[3] = HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().get(i).getClasse();
            vetRequisitos[4] = HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos().get(i).getFase();
            vetRequisitos[5] = "";
            vetRequisitos[6] = "";
            traceabilityInfo.addRow(vetRequisitos);
        }

        tableTraceability.setModel(traceabilityInfo);
        tableTraceability.getColumnModel().getColumn(0).setMaxWidth(100);
        tableTraceability.getColumnModel().getColumn(0).setMinWidth(100);
        tableTraceability.getColumnModel().getColumn(1).setPreferredWidth(MAXIMIZED_HORIZ);
        tableTraceability.getColumnModel().getColumn(2).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(2).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(3).setMaxWidth(240);
        tableTraceability.getColumnModel().getColumn(3).setMinWidth(240);
        tableTraceability.getColumnModel().getColumn(4).setMaxWidth(200);
        tableTraceability.getColumnModel().getColumn(4).setMinWidth(200);
        tableTraceability.getColumnModel().getColumn(5).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(5).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMinWidth(80);

        tableTraceability.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer2());
        tableTraceability.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor2(new JTextField()));

        tableTraceability.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererDelete());
        tableTraceability.getColumnModel().getColumn(6).setCellEditor(new ButtonDelete(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableTraceability.getModel());
        tableTraceability.setRowSorter(rowSorter);
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

    public void updateTableIStarHorizontalTraceability() {
        traceabilityInfo = new DefaultTableModel();
        tableTraceability.setAutoResizeMode(MAXIMIZED_HORIZ);
        traceabilityInfo.addColumn("Abreviation");//0
        traceabilityInfo.addColumn("Name");//1
        traceabilityInfo.addColumn("Model");//2
        traceabilityInfo.addColumn("Class");//3
        traceabilityInfo.addColumn("Segment");//4
        traceabilityInfo.addColumn(" ");//5
        traceabilityInfo.addColumn(" ");//6

        String vetActorsystem[] = new String[7];
        // adiciona os dados dos casos de uso no modelo da tabela
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().size(); i++) {
            vetActorsystem[0] = HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().get(i).getAbreviacao();
            vetActorsystem[1] = HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().get(i).getLabel();
            vetActorsystem[2] = HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().get(i).getModel();
            vetActorsystem[3] = HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().get(i).getClasse();
            vetActorsystem[4] = HorizontalIStarTraceController.getTokensTraceability().getAtorSistema().get(i).getFase();
            vetActorsystem[5] = "";
            vetActorsystem[6] = "";
            traceabilityInfo.addRow(vetActorsystem);
        }
        String vetStakeholder[] = new String[7];
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getStakeholders().size(); i++) {
            vetStakeholder[0] = HorizontalIStarTraceController.getTokensTraceability().getStakeholders().get(i).getAbreviacao();
            vetStakeholder[1] = HorizontalIStarTraceController.getTokensTraceability().getStakeholders().get(i).getLabel();
            vetStakeholder[2] = HorizontalIStarTraceController.getTokensTraceability().getStakeholders().get(i).getModel();
            vetStakeholder[3] = HorizontalIStarTraceController.getTokensTraceability().getStakeholders().get(i).getClasse();
            vetStakeholder[4] = HorizontalIStarTraceController.getTokensTraceability().getStakeholders().get(i).getFase();
            vetStakeholder[5] = "";
            vetStakeholder[6] = "";
            traceabilityInfo.addRow(vetStakeholder);
        }
        String vetInfExterna[] = new String[7];
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().size(); i++) {
            vetInfExterna[0] = HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().get(i).getAbreviacao();
            vetInfExterna[1] = HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().get(i).getLabel();
            vetInfExterna[2] = HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().get(i).getModel();
            vetInfExterna[3] = HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().get(i).getClasse();
            vetInfExterna[4] = HorizontalIStarTraceController.getTokensTraceability().getInformcaoExterna().get(i).getFase();
            vetInfExterna[5] = "";
            vetInfExterna[6] = "";
            traceabilityInfo.addRow(vetInfExterna);
        }
        String vetInfOrganizacional[] = new String[7];
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().size(); i++) {
            vetInfOrganizacional[0] = HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().get(i).getAbreviacao();
            vetInfOrganizacional[1] = HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().get(i).getLabel();
            vetInfOrganizacional[2] = HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().get(i).getModel();
            vetInfOrganizacional[3] = HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().get(i).getClasse();
            vetInfOrganizacional[4] = HorizontalIStarTraceController.getTokensTraceability().getInformacaoOrg().get(i).getFase();
            vetInfOrganizacional[5] = "";
            vetInfOrganizacional[6] = "";
            traceabilityInfo.addRow(vetInfOrganizacional);
        }
        String vetObjetivoSistema[] = new String[7];
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().size(); i++) {
            vetObjetivoSistema[0] = HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().get(i).getAbreviacao();
            vetObjetivoSistema[1] = HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().get(i).getLabel();
            vetObjetivoSistema[2] = HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().get(i).getModel();
            vetObjetivoSistema[3] = HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().get(i).getClasse();
            vetObjetivoSistema[4] = HorizontalIStarTraceController.getTokensTraceability().getObjetivoSistema().get(i).getFase();
            vetObjetivoSistema[5] = "";
            vetObjetivoSistema[6] = "";
            traceabilityInfo.addRow(vetObjetivoSistema);
        }
        String vetRequisitos[] = new String[7];
        for (int i = 0; i < HorizontalIStarTraceController.getTokensTraceability().getRequisitos().size(); i++) {
            vetRequisitos[0] = HorizontalIStarTraceController.getTokensTraceability().getRequisitos().get(i).getAbreviacao();
            vetRequisitos[1] = HorizontalIStarTraceController.getTokensTraceability().getRequisitos().get(i).getLabel();
            vetRequisitos[2] = HorizontalIStarTraceController.getTokensTraceability().getRequisitos().get(i).getModel();
            vetRequisitos[3] = HorizontalIStarTraceController.getTokensTraceability().getRequisitos().get(i).getClasse();
            vetRequisitos[4] = HorizontalIStarTraceController.getTokensTraceability().getRequisitos().get(i).getFase();
            vetRequisitos[5] = "";
            vetRequisitos[6] = "";
            traceabilityInfo.addRow(vetRequisitos);
        }

        tableTraceability.setModel(traceabilityInfo);
        tableTraceability.getColumnModel().getColumn(0).setMaxWidth(100);
        tableTraceability.getColumnModel().getColumn(0).setMinWidth(100);
        tableTraceability.getColumnModel().getColumn(1).setPreferredWidth(MAXIMIZED_HORIZ);
        tableTraceability.getColumnModel().getColumn(2).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(2).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(3).setMaxWidth(240);
        tableTraceability.getColumnModel().getColumn(3).setMinWidth(240);
        tableTraceability.getColumnModel().getColumn(4).setMaxWidth(200);
        tableTraceability.getColumnModel().getColumn(4).setMinWidth(200);
        tableTraceability.getColumnModel().getColumn(5).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(5).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMinWidth(80);

        tableTraceability.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer2());
        tableTraceability.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor2(new JTextField()));

        tableTraceability.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererDelete());
        tableTraceability.getColumnModel().getColumn(6).setCellEditor(new ButtonDelete(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableTraceability.getModel());
        tableTraceability.setRowSorter(rowSorter);
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

    public void updateTableVerticalBPMNtoUCTraceability() {
        traceabilityInfo = new DefaultTableModel();
        tableTraceability.setAutoResizeMode(MAXIMIZED_HORIZ);
        traceabilityInfo.addColumn("Abreviation");
        traceabilityInfo.addColumn("Name");
        traceabilityInfo.addColumn("Model");
        traceabilityInfo.addColumn("Class");
        traceabilityInfo.addColumn("Segment");
        traceabilityInfo.addColumn(" ");
        traceabilityInfo.addColumn(" ");

        String vetAtorSistema[] = new String[7];
        // adiciona os dados dos casos de uso no modelo da tabela
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getAtorSistema().size(); i++) {
            vetAtorSistema[0] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getAbreviacao();
            vetAtorSistema[1] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getLabel();
            vetAtorSistema[2] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getModel();
            vetAtorSistema[3] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getClasse();
            vetAtorSistema[4] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getFase();
            vetAtorSistema[5] = "";
            vetAtorSistema[6] = "";
            traceabilityInfo.addRow(vetAtorSistema);
        }
        String vetStakeholder[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getStakeholders().size(); i++) {
            vetStakeholder[0] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getAbreviacao();
            vetStakeholder[1] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getLabel();
            vetStakeholder[2] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getModel();
            vetStakeholder[3] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getClasse();
            vetStakeholder[4] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getFase();
            vetStakeholder[5] = "";
            vetStakeholder[6] = "";
            traceabilityInfo.addRow(vetStakeholder);
        }
        String vetInfExterna[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getInformcaoExterna().size(); i++) {
            vetInfExterna[0] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getAbreviacao();
            vetInfExterna[1] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getLabel();
            vetInfExterna[2] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getModel();
            vetInfExterna[3] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getClasse();
            vetInfExterna[4] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getFase();
            vetInfExterna[5] = "";
            vetInfExterna[6] = "";
            traceabilityInfo.addRow(vetInfExterna);
        }
        String vetInfOrganizacional[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getInformacaoOrg().size(); i++) {
            vetInfOrganizacional[0] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getAbreviacao();
            vetInfOrganizacional[1] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getLabel();
            vetInfOrganizacional[2] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getModel();
            vetInfOrganizacional[3] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getClasse();
            vetInfOrganizacional[4] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getFase();
            vetInfOrganizacional[5] = "";
            vetInfOrganizacional[6] = "";
            traceabilityInfo.addRow(vetInfOrganizacional);
        }
        String vetObjetivoSistema[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getObjetivoSistema().size(); i++) {
            vetObjetivoSistema[0] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getAbreviacao();
            vetObjetivoSistema[1] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getLabel();
            vetObjetivoSistema[2] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getModel();
            vetObjetivoSistema[3] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getClasse();
            vetObjetivoSistema[4] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getFase();
            vetObjetivoSistema[5] = "";
            vetObjetivoSistema[6] = "";
            traceabilityInfo.addRow(vetObjetivoSistema);
        }
        String vetRequisitos[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getRequisitos().size(); i++) {
            vetRequisitos[0] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getAbreviacao();
            vetRequisitos[1] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getLabel();
            vetRequisitos[2] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getModel();
            vetRequisitos[3] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getClasse();
            vetRequisitos[4] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getFase();
            vetRequisitos[5] = "";
            vetRequisitos[6] = "";
            traceabilityInfo.addRow(vetRequisitos);
        }

        tableTraceability.setModel(traceabilityInfo);
        tableTraceability.getColumnModel().getColumn(0).setMaxWidth(100);
        tableTraceability.getColumnModel().getColumn(0).setMinWidth(100);
        tableTraceability.getColumnModel().getColumn(1).setPreferredWidth(MAXIMIZED_HORIZ);
        tableTraceability.getColumnModel().getColumn(2).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(2).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(3).setMaxWidth(240);
        tableTraceability.getColumnModel().getColumn(3).setMinWidth(240);
        tableTraceability.getColumnModel().getColumn(4).setMaxWidth(200);
        tableTraceability.getColumnModel().getColumn(4).setMinWidth(200);
        tableTraceability.getColumnModel().getColumn(5).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(5).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMinWidth(80);

        tableTraceability.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer2());
        tableTraceability.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor2(new JTextField()));

        tableTraceability.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererDelete());
        tableTraceability.getColumnModel().getColumn(6).setCellEditor(new ButtonDelete(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableTraceability.getModel());
        tableTraceability.setRowSorter(rowSorter);
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

    public void updateTableVerticalIStartoUCTraceability() {
        traceabilityInfo = new DefaultTableModel();
        tableTraceability.setAutoResizeMode(MAXIMIZED_HORIZ);
        traceabilityInfo.addColumn("Abreviation");//0
        traceabilityInfo.addColumn("Name");//1
        traceabilityInfo.addColumn("Model");//2
        traceabilityInfo.addColumn("Class");//3
        traceabilityInfo.addColumn("Segment");
        traceabilityInfo.addColumn(" ");
        traceabilityInfo.addColumn(" ");

        String vetActorSystem[] = new String[7];
        // adiciona os dados dos casos de uso no modelo da tabela
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getAtorSistema().size(); i++) {
            vetActorSystem[0] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getAbreviacao();
            vetActorSystem[1] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getLabel();
            vetActorSystem[2] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getModel();
            vetActorSystem[3] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getClasse();
            vetActorSystem[4] = VerticalTraceController.getTokensVertical().getAtorSistema().get(i).getFase();
            vetActorSystem[5] = "";
            vetActorSystem[6] = "";
            traceabilityInfo.addRow(vetActorSystem);
        }

        String vetStakeholder[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getStakeholders().size(); i++) {
            vetStakeholder[0] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getAbreviacao();
            vetStakeholder[1] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getLabel();
            vetStakeholder[2] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getModel();
            vetStakeholder[3] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getClasse();
            vetStakeholder[4] = VerticalTraceController.getTokensVertical().getStakeholders().get(i).getFase();
            vetStakeholder[5] = "";
            vetStakeholder[6] = "";
            traceabilityInfo.addRow(vetStakeholder);
        }
        String vetInfExterna[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getInformcaoExterna().size(); i++) {
            vetInfExterna[0] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getAbreviacao();
            vetInfExterna[1] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getLabel();
            vetInfExterna[2] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getModel();
            vetInfExterna[3] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getClasse();
            vetInfExterna[4] = VerticalTraceController.getTokensVertical().getInformcaoExterna().get(i).getFase();
            vetInfExterna[5] = "";
            vetInfExterna[6] = "";
            traceabilityInfo.addRow(vetInfExterna);
        }
        String vetInfOrganizacional[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getInformacaoOrg().size(); i++) {
            vetInfOrganizacional[0] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getAbreviacao();
            vetInfOrganizacional[1] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getLabel();
            vetInfOrganizacional[2] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getModel();
            vetInfOrganizacional[3] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getClasse();
            vetInfOrganizacional[4] = VerticalTraceController.getTokensVertical().getInformacaoOrg().get(i).getFase();
            vetInfOrganizacional[5] = "";
            vetInfOrganizacional[6] = "";
            traceabilityInfo.addRow(vetInfOrganizacional);
        }
        String vetObjetivoSistema[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getObjetivoSistema().size(); i++) {
            vetObjetivoSistema[0] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getAbreviacao();
            vetObjetivoSistema[1] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getLabel();
            vetObjetivoSistema[2] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getModel();
            vetObjetivoSistema[3] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getClasse();
            vetObjetivoSistema[4] = VerticalTraceController.getTokensVertical().getObjetivoSistema().get(i).getFase();
            vetObjetivoSistema[5] = "";
            vetObjetivoSistema[6] = "";
            traceabilityInfo.addRow(vetObjetivoSistema);
        }
        String vetRequisitos[] = new String[7];
        for (int i = 0; i < VerticalTraceController.getTokensVertical().getRequisitos().size(); i++) {
            vetRequisitos[0] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getAbreviacao();
            vetRequisitos[1] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getLabel();
            vetRequisitos[2] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getModel();
            vetRequisitos[3] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getClasse();
            vetRequisitos[4] = VerticalTraceController.getTokensVertical().getRequisitos().get(i).getFase();
            vetRequisitos[5] = "";
            vetRequisitos[6] = "";
            traceabilityInfo.addRow(vetRequisitos);
        }

        tableTraceability.setModel(traceabilityInfo);
        tableTraceability.getColumnModel().getColumn(0).setMaxWidth(100);
        tableTraceability.getColumnModel().getColumn(0).setMinWidth(100);
        tableTraceability.getColumnModel().getColumn(1).setPreferredWidth(MAXIMIZED_HORIZ);
        tableTraceability.getColumnModel().getColumn(2).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(2).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(3).setMaxWidth(240);
        tableTraceability.getColumnModel().getColumn(3).setMinWidth(240);
        tableTraceability.getColumnModel().getColumn(4).setMaxWidth(200);
        tableTraceability.getColumnModel().getColumn(4).setMinWidth(200);
        tableTraceability.getColumnModel().getColumn(5).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(5).setMinWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMaxWidth(80);
        tableTraceability.getColumnModel().getColumn(6).setMinWidth(80);

        tableTraceability.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer2());
        tableTraceability.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor2(new JTextField()));

        tableTraceability.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererDelete());
        tableTraceability.getColumnModel().getColumn(6).setCellEditor(new ButtonDelete(new JTextField(), 0));

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tableTraceability.getModel());
        tableTraceability.setRowSorter(rowSorter);
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

    public String getSelectedCase() {
        return selectedCase;
    }

    public void setSelectedCase(String selectedCase) {
        this.selectedCase = selectedCase;
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
            JMenuItem menuItem1 = new JMenuItem(editor.bind(label1, new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, 1)));
            fileMenu.add(menuItem1, 3);
            fileMenu.add(new JPopupMenu.Separator(), 4);
            String label2 = mxResources.get("traceabilityMaker", null, "Vertical Traceability");
            JMenuItem menuItem2 = new JMenuItem(editor.bind(label2, new VerticalTraceController(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, 2)));
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
                JMenuItem menuItem1 = new JMenuItem(bpmnEditor.bind(label1, new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, 2)));
                fileMenu.add(menuItem1, 3);
                fileMenu.add(new JPopupMenu.Separator(), 4);
                String label2 = mxResources.get("traceabilityMaker", null, "Vertical Traceability");
                JMenuItem menuItem2 = new JMenuItem(bpmnEditor.bind(label2, new VerticalTraceController(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, 1)));
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
            JMenuItem menuItem1 = new JMenuItem(editor.bind(label1, (Action) new HorizontalControler(E4JiStar, E4JBPMN, E4JUseCases, useCasesViewIStar, useCasesViewBPMN, 3)));
            fileMenu.add(menuItem1, 3);
        }
        E4JUseCases.setVisible(true);
        this.setVisible(false);
    }

    @SuppressWarnings("empty-statement")
    private void tableTraceabilityValueChanged(ListSelectionEvent evt) {
        int colunm = tableTraceability.getSelectedColumn();
        int linha = tableTraceability.getSelectedRow();
        if (linha >= 0) {
            if (colunm == 6) {
                int dialogButton = JOptionPane.YES_NO_OPTION;
                try {
                    String nameElementTraced = String.valueOf(tableTraceability.getValueAt(linha, 1));
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Would you like to delete a " + nameElementTraced + "?", "Warning", dialogButton);
                    String abreviation = String.valueOf(tableTraceability.getValueAt(linha, 0));

                    if (dialogResult == JOptionPane.YES_OPTION) {
                        removeTracedElement(linha, abreviation);
                    }
                } catch (HeadlessException e) {
                    JOptionPane.showMessageDialog(null, "Ops some problem occurred");
                }
            }
        }

    }

    private void removeTracedElement(int linha, String abreviation) {
        String name = String.valueOf(tableTraceability.getValueAt(linha, 3));
        System.out.println("name:" + name);
        switch (name) {
            case "Ator Sistema":
                removeActorSystem(abreviation);
                break;
            case "Stakeholder":
                removeStakeholder(abreviation);
                break;
            case "Requisitos":
                removeRequisito(abreviation);
                break;
            case "Objetivo Do Sistema":
                removeObjSistema(abreviation);
                break;
            case "Informação Organizacional":
                removeInfoOrg(abreviation);
                break;
            case "Informação Externa":
                removeInfoExt(abreviation);
                break;
        }
        updateTable();
    }

    private void removeActorSystem(String abreviation) {
        TracedElement atorSistema;
        switch (type) {
            case 1: //Horizontal BPMN
                atorSistema = TraceBPMNHorizontal.getLista().getActorSystem(abreviation);
                TraceBPMNHorizontal.getLista().removeAtorSistema(atorSistema);
                break;
            case 2: //Horizontal UC
                atorSistema = TraceUCHorizontal.getLista().getActorSystem(abreviation);
                TraceUCHorizontal.getLista().removeAtorSistema(atorSistema);
                break;
            case 3://Horizonal i*
                atorSistema = TraceIStarHorizontal.getLista().getActorSystem(abreviation);
                TraceIStarHorizontal.getLista().removeAtorSistema(atorSistema);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                atorSistema = VerticalTraceController.getTokensVertical().getActorSystem(abreviation);
                VerticalTraceController.getTokensVertical().removeAtorSistema(atorSistema);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                atorSistema = VerticalTraceController.getTokensVertical().getActorSystem(abreviation);
                VerticalTraceController.getTokensVertical().removeAtorSistema(atorSistema);
                break;
        }
    }

    private void removeStakeholder(String abreviation) {
        TracedElement stakeholder;
        switch (type) {
            case 1: //Horizontal BPMN
                stakeholder = TraceBPMNHorizontal.getLista().getStakeholder(abreviation);
                TraceBPMNHorizontal.getLista().removeStakeholder(stakeholder);
                break;
            case 2: //Horizontal UC
                stakeholder = TraceUCHorizontal.getLista().getStakeholder(abreviation);
                TraceUCHorizontal.getLista().removeStakeholder(stakeholder);
                break;
            case 3://Horizonal i*
                stakeholder = TraceIStarHorizontal.getLista().getStakeholder(abreviation);
                TraceIStarHorizontal.getLista().removeStakeholder(stakeholder);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                stakeholder = VerticalTraceController.getTokensVertical().getStakeholder(abreviation);
                VerticalTraceController.getTokensVertical().removeStakeholder(stakeholder);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                stakeholder = VerticalTraceController.getTokensVertical().getStakeholder(abreviation);
                VerticalTraceController.getTokensVertical().removeStakeholder(stakeholder);
                break;
        }
    }

    private void removeRequisito(String abreviation) {
        TracedElement requisito;
        switch (type) {
            case 1: //Horizontal BPMN
                requisito = TraceBPMNHorizontal.getLista().getRequisito(abreviation);
                TraceBPMNHorizontal.getLista().removeRequisito(requisito);
                break;
            case 2: //Horizontal UC
                requisito = TraceUCHorizontal.getLista().getRequisito(abreviation);
                TraceUCHorizontal.getLista().removeRequisito(requisito);
                break;
            case 3://Horizonal i*
                requisito = TraceIStarHorizontal.getLista().getRequisito(abreviation);
                TraceIStarHorizontal.getLista().removeRequisito(requisito);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                requisito = VerticalTraceController.getTokensVertical().getRequisito(abreviation);
                VerticalTraceController.getTokensVertical().removeRequisito(requisito);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                requisito = VerticalTraceController.getTokensVertical().getRequisito(abreviation);
                VerticalTraceController.getTokensVertical().removeRequisito(requisito);
                break;
        }
    }

    private void removeObjSistema(String abreviation) {
        TracedElement objSistema;
        switch (type) {
            case 1: //Horizontal BPMN
                objSistema = TraceBPMNHorizontal.getLista().getObjetivoSistema(abreviation);
                TraceBPMNHorizontal.getLista().removeObjetivoSistema(objSistema);
                break;
            case 2: //Horizontal UC
                objSistema = TraceUCHorizontal.getLista().getObjetivoSistema(abreviation);
                TraceUCHorizontal.getLista().removeObjetivoSistema(objSistema);
                break;
            case 3://Horizonal i*
                objSistema = TraceIStarHorizontal.getLista().getObjetivoSistema(abreviation);
                TraceIStarHorizontal.getLista().removeObjetivoSistema(objSistema);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                objSistema = VerticalTraceController.getTokensVertical().getObjetivoSistema(abreviation);
                VerticalTraceController.getTokensVertical().removeObjetivoSistema(objSistema);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                objSistema = VerticalTraceController.getTokensVertical().getObjetivoSistema(abreviation);
                VerticalTraceController.getTokensVertical().removeObjetivoSistema(objSistema);
                break;
        }
    }

    private void removeInfoOrg(String abreviation) {
        TracedElement infoOrg;
        switch (type) {
            case 1: //Horizontal BPMN
                infoOrg = TraceBPMNHorizontal.getLista().getInfoOrg(abreviation);
                TraceBPMNHorizontal.getLista().removeInformacaoOrg(infoOrg);
                break;
            case 2: //Horizontal UC
                infoOrg = TraceUCHorizontal.getLista().getInfoOrg(abreviation);
                TraceUCHorizontal.getLista().removeInformacaoOrg(infoOrg);
                break;
            case 3://Horizonal i*
                infoOrg = TraceIStarHorizontal.getLista().getInfoOrg(abreviation);
                TraceIStarHorizontal.getLista().removeInformacaoOrg(infoOrg);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                infoOrg = VerticalTraceController.getTokensVertical().getInfoOrg(abreviation);
                VerticalTraceController.getTokensVertical().removeInformacaoOrg(infoOrg);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                infoOrg = VerticalTraceController.getTokensVertical().getInfoOrg(abreviation);
                VerticalTraceController.getTokensVertical().removeInformacaoOrg(infoOrg);
                break;
        }
    }

    private void removeInfoExt(String abreviation) {
        TracedElement infoExt;
        switch (type) {
            case 1: //Horizontal BPMN
                infoExt = TraceBPMNHorizontal.getLista().getInfoExterna(abreviation);
                TraceBPMNHorizontal.getLista().removeInformacaoExterna(infoExt);
                break;
            case 2: //Horizontal UC
                infoExt = TraceUCHorizontal.getLista().getInfoExterna(abreviation);
                TraceUCHorizontal.getLista().removeInformacaoExterna(infoExt);
                break;
            case 3://Horizonal i*
                infoExt = TraceIStarHorizontal.getLista().getInfoExterna(abreviation);
                TraceIStarHorizontal.getLista().removeInformacaoExterna(infoExt);
                break;
            case 4://Vertical rastreabilidade vertical BPMN to UC
                infoExt = VerticalTraceController.getTokensVertical().getInfoExterna(abreviation);
                VerticalTraceController.getTokensVertical().removeInformacaoExterna(infoExt);
                break;
            case 5://Vertical rastreabilidade vertical i* to UC
                infoExt = VerticalTraceController.getTokensVertical().getInfoExterna(abreviation);
                VerticalTraceController.getTokensVertical().removeInformacaoExterna(infoExt);
                break;
        }
    }

    public void atualizeType(int index) {
        this.type = index;
    }

//BUTTON RENDERER CLASS
    class ButtonRenderer2 extends JButton implements TableCellRenderer {

        //CONSTRUCTOR
        public ButtonRenderer2() {
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
    class ButtonEditor2 extends DefaultCellEditor {

        private List informations;
        protected JButton btn;
        private String lbl;
        private Boolean clicked;
        private int row;

        public ButtonEditor2(JTextField txt) {
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
            for (UCUseCase useCase : BPMNController.getUseCases()) {
                if (row == cont) {
                    String vetCasosDeUso[] = new String[6];
                    vetCasosDeUso[0] = "" + cont++;
                    vetCasosDeUso[1] = useCase.getName();
                    vetCasosDeUso[2] = useCase.getPrimaryActor().getName();

                    String actors = "";
                    // Percorre os atores, identificando atores associados ao atual
                    for (UCActor actor : useCase.getSecondaryActors()) {
                        actors += actor.getName() + "; ";
                    }
                    vetCasosDeUso[3] = actors;
                    String includedUseCases = "";
                    //Percorre os casos de usos incluídos
                    for (UCUseCase useCaseIncluded : useCase.getIncludedUseCases()) {
                        includedUseCases += (Integer.parseInt(useCaseIncluded.getCode()) + 1) + " - " + useCaseIncluded.getName() + "; ";
                    }
                    vetCasosDeUso[4] = includedUseCases;
                    vetCasosDeUso[5] = useCase.getGuidelineUsed();
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
                MoreInfoUCFromBPMN info = new MoreInfoUCFromBPMN(mainView, true, vetCasosDeUso);
                info.setVisible(true);
                //JOptionPane.showMessageDialog(btn, lbl + " Clicked");
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
class ButtonRendererDelete2 extends JButton implements TableCellRenderer {

    //CONSTRUCTOR
    public ButtonRendererDelete2() {
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
class ButtonDelete2 extends DefaultCellEditor {

    protected JButton btn;
    private String lbl;
    private Boolean clicked;
    private int row;

    public ButtonDelete2(JTextField txt, int row) {
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
