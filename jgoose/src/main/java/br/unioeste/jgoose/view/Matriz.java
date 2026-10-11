/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unioeste.jgoose.view;

import br.unioeste.jgoose.controller.HorizontalBPMNTraceController;
import br.unioeste.jgoose.model.TokensTraceability;
import br.unioeste.jgoose.model.TracedElement;
import br.unioeste.jgoose.model.TracedRequisitos;
import br.unioeste.jgoose.model.TracedStakeholders;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JOptionPane;

/**
 *
 * @author Victor Augusto Pozzan
 */
public class Matriz extends JFrame {

    private final JFrame frame = new JFrame("Matriz de Rastreabilidade");
    private JTable table;
    private DefaultTableModel model;
    private JTable headerTable;
    private Integer sizeRow;
    private Integer sizeCol;
    private String title;
    private List<TracedElement> elementColumn;
    private List<TracedElement> elementRow;
    private boolean matrizQuadrada;
    private Image iconJGOOSE = Toolkit.getDefaultToolkit().getImage("./src/main/resources/icons/jgoose.gif");


    public Matriz(Integer sizeCol, Integer sizeRow, String title) {
        frame.setIconImage(iconJGOOSE);
        this.sizeRow = sizeRow;
        this.sizeCol = sizeCol;
        this.title = title;
    }

    public Matriz(String title, int col, int row, List<TracedElement> elementColumn, List<TracedElement> elementRow, boolean b) {
        this.title = title;
        this.sizeCol = col;
        this.sizeRow = row;
        this.elementColumn = elementColumn;
        this.elementRow = elementRow;
        this.matrizQuadrada = b;
    }

    public void matriz(final List<TracedElement> elementCol, final List<TracedElement> elementRow, boolean MatrizQuadrada) {

        table = new JTable(sizeRow, sizeCol);

        int aux1 = 0, aux2 = 0, aux3 = 0, aux4 = 0, ii = 0;
        for (int i = 0; i < sizeCol; i++) {
            for (int j = 0; j < elementCol.get(i).getListConcflicts().size(); j++) {
                for (int k = 0; k < elementRow.size(); k++) {
                    if (elementCol.get(i).getListConcflicts().get(j)[0] == elementRow.get(k).getCode()) {
                        if (MatrizQuadrada && i != k) {
                            table.setValueAt(elementCol.get(i).getListConcflicts().get(j)[1], k, i);
                        } else if (!MatrizQuadrada) {
                            table.setValueAt(elementCol.get(i).getListConcflicts().get(j)[1], k, i);
                        }
                    }
                }
            }
        }
        // sorter = new TableRowSorter<TableModel>(table.getModel());
        // table.setRowSorter(sorter);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderValue(elementCol.get(i).getAbreviacao());
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(50);
        }

        headerTable = new JTable(model);

        table.getTableHeader().resizeAndRepaint();
        model = new DefaultTableModel() {

            private static final long serialVersionUID = 1L;

            @Override
            public int getColumnCount() {
                return 1;
            }

            //@Override
            //public boolean isCellEditable(int row, int col) {
            //    return false;
            //}
            @Override
            public int getRowCount() {
                return table.getRowCount();
            }

            @Override
            public Class<?> getColumnClass(int colNum) {
                switch (colNum) {
                    case 0:
                        return String.class;
                    default:
                        return super.getColumnClass(colNum);
                }
            }
        };

        headerTable = new JTable(model);
        for (int i = 0; i < table.getRowCount(); i++) {
            headerTable.setValueAt(elementRow.get(i).getAbreviacao(), i, 0);
        }

        headerTable.setShowGrid(false);
        headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        headerTable.setPreferredScrollableViewportSize(new Dimension(100, 0));
        headerTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        headerTable.getRowHeight(10);
        headerTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable x, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                boolean selected = table.getSelectionModel().isSelectedIndex(row);
                Component component = table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, value, false, false, -1, -2);
                ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
                /*if (selected) {
                    component.setFont(component.getFont().deriveFont(Font.BOLD));
                    component.setForeground(Color.red);
                } else {
                    component.setFont(component.getFont().deriveFont(Font.PLAIN));
                }*/
                return component;
            }
        });

        /*table.getRowSorter().addRowSorterListener(new RowSorterListener() {

            @Override
            public void sorterChanged(RowSorterEvent e) {
                model.fireTableDataChanged();
            }
        });*/
 /*table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                model.fireTableRowsUpdated(0, model.getRowCount() - 1);
            }
        });*/
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(Color.gray);
        JScrollPane jScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setRowHeaderView(headerTable);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(jScrollPane);
        frame.add(new JLabel((title)), BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setSize(500, 500);

        final JTextField abbLinha = new JTextField();
        abbLinha.setSize(50, 50);
        abbLinha.setText("                ");
        panel.add(abbLinha);

        final JTextField abbCol = new JTextField();
        abbCol.setSize(50, 50);
        abbCol.setText("                ");
        panel.add(abbCol);

        String[] vetor = {" ", "A", "M", "B"};
        final JComboBox typeDependency = new JComboBox(vetor);
        typeDependency.setSize(50, 50);
        panel.add(typeDependency);

        JButton bntSetDependency = new JButton();
        bntSetDependency.setSize(10, 10);
        bntSetDependency.setText("OK");
        panel.add(bntSetDependency);

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int ii = table.getSelectedRow();
                int jj = table.getSelectedColumn();

                for (int i = 0; i < table.getRowCount(); i++) {
                    if (i == ii) {
                        abbLinha.setText(elementRow.get(i).getAbreviacao());
                    }
                }
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (i == jj) {
                        abbCol.setText(elementCol.get(i).getAbreviacao());
                    }
                }
                System.out.println("OTARO");
                System.out.println("O BABACA:" + table.getValueAt(ii, jj));
                String abb = " ";
                try {
                    if (null != table.getValueAt(ii, jj)) {
                        switch (table.getValueAt(ii, jj).toString()) {
                            case "A":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            case "M":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            case "B":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            default:
                                break;
                        }
                    }

                    switch (abb) {
                        case "A":
                            typeDependency.setSelectedIndex(1);
                            break;
                        case "M":
                            typeDependency.setSelectedIndex(2);
                            break;
                        case "B":
                            typeDependency.setSelectedIndex(3);
                            break;
                        default:
                            typeDependency.setSelectedIndex(0);
                            break;
                    }
                } catch (Error ee) {
                    typeDependency.setSelectedIndex(0);

                }

            }
        });

        bntSetDependency.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int ii = table.getSelectedRow();
                int jj = table.getSelectedColumn();

                if (ii < 0 || jj < 0) {
                    JOptionPane.showMessageDialog(null,
                        "Selecione uma célula da matriz antes de definir a dependência.");
                    return;
                }

                String[] vetor = new String[2];
                String abb = " ";
                try {
                    if (null != table.getValueAt(ii, jj)) {
                        switch (table.getValueAt(ii, jj).toString()) {
                            case "A":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            case "M":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            case "B":
                                abb = table.getValueAt(ii, jj).toString();
                                break;
                            default:
                                break;
                        }
                    }

                    switch (abb) {
                        case "A":
                            table.setValueAt(abb, ii, jj);
                            vetor[1] = "A";
                            break;
                        case "M":
                            table.setValueAt(abb, ii, jj);
                            vetor[1] = "M";
                            break;
                        case "B":
                            table.setValueAt(abb, ii, jj);
                            vetor[1] = "B";
                            break;
                        default:
                            table.setValueAt(abb, ii, jj);
                            vetor[1] = "";
                            break;

                    }
                } catch (Error ee) {
                    typeDependency.setSelectedIndex(0);

                }

                for (int i = 0;
                        i < table.getRowCount();
                        i++) {
                    if (i == ii) {
                        for (int j = 0; j < table.getColumnCount(); j++) {
                            if (j == jj) {
                                vetor[0] = elementCol.get(j).getCode();
                                elementRow.get(i).setVetConflict(vetor);
                                vetor[0] = elementRow.get(i).getCode();
                                elementCol.get(j).setVetConflict(vetor);
                                break;
                            }
                        }
                        break;
                    }
                }

            }
        }
        );

        panel.setBackground(Color.DARK_GRAY);

        frame.add(panel, BorderLayout.SOUTH);

        //       frame.add(model, BorderLayout.CENTER);
        /*frame.add(new JButton(new AbstractAction("Toggle filter") {

            private static final long serialVersionUID = 1L;
            private RowFilter<TableModel, Object> filter = new RowFilter<TableModel, Object>() {

                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends TableModel, ? extends Object> entry) {
                    return ((Number) entry.getValue(0)).intValue() % 2 == 0;
                }
            };

            @Override
            public void actionPerformed(ActionEvent e) {
                if (sorter.getRowFilter() != null) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(filter);
                }
            }
        }), BorderLayout.SOUTH);*/
        frame.pack();

        frame.setLocation(SwingConstants.CENTER, SwingConstants.CENTER);

        frame.setVisible(
                true);
    }

    public Integer getSizeRow() {
        return sizeRow;
    }

    public void setSizeRow(Integer sizeRow) {
        this.sizeRow = sizeRow;
    }

    public Integer getSizeCol() {
        return sizeCol;
    }

    public void setSizeCol(Integer sizeCol) {
        this.sizeCol = sizeCol;
    }

    public List<TracedElement> getElementColumn() {
        return elementColumn;
    }

    public void setElementColumn(List<TracedElement> elementColumn) {
        this.elementColumn = elementColumn;
    }

    public List<TracedElement> getElementRow() {
        return elementRow;
    }

    public void setElementRow(List<TracedElement> elementRow) {
        this.elementRow = elementRow;
    }

    public boolean isMatrizQuadrada() {
        return matrizQuadrada;
    }

    public void setMatrizQuadrada(boolean matrizQuadrada) {
        this.matrizQuadrada = matrizQuadrada;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();

        setBackground(new java.awt.Color(51, 255, 255));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu jPopupMenu1;
    // End of variables declaration//GEN-END:variables
}
