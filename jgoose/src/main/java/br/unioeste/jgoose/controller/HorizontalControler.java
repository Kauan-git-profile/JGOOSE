/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unioeste.jgoose.controller;

import br.unioeste.jgoose.TraceabilityHorizontal.TraceBPMNHorizontal;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceIStarHorizontal;
import br.unioeste.jgoose.TraceabilityHorizontal.TraceUCHorizontal;
import br.unioeste.jgoose.e4j.swing.EditorJFrame;
import br.unioeste.jgoose.view.TraceabilityView;
import br.unioeste.jgoose.view.TraceabilityView1;
import br.unioeste.jgoose.view.UseCasesViewBPMN;
import br.unioeste.jgoose.view.UseCasesViewIStar;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Victor August Pozzan
 */
public class HorizontalControler extends AbstractAction {

    private static HorizontalIStarTraceController horizontalIStarTraceController;
    private static TraceIStarHorizontal traceIStarHorizontal;

    private static HorizontalBPMNTraceController horizontalBPMNTraceController;
    private static TraceBPMNHorizontal traceBPMNHorizontal;

    private static HorizontalUseCaseTraceController horizontalUseCaseTraceController;
    private static TraceUCHorizontal traceUCHorizontal;

    private static TraceabilityView viewTraceability = null;
    private static TraceabilityView1 viewTraceability1 = null;

    public static int type;
    public static EditorJFrame E4J;
    public static int index = -1;

    private static EditorJFrame E4JiStar = null;
    private static EditorJFrame E4JUseCases = null;
    private static EditorJFrame E4JBPMN = null;
    private static UseCasesViewBPMN useCasesViewBPMN = null;
    private static UseCasesViewIStar useCasesViewIStar = null;

    public HorizontalControler(EditorJFrame E4JiStar, EditorJFrame E4JBPMN, EditorJFrame E4JUseCases,
            UseCasesViewIStar useCasesViewIStar, UseCasesViewBPMN useCasesViewBPMN, int type) {

        this.E4JBPMN = E4JBPMN;
        this.E4JiStar = E4JiStar;
        this.E4JUseCases = E4JUseCases;
        this.useCasesViewIStar = useCasesViewIStar;
        this.useCasesViewBPMN = useCasesViewBPMN;

        this.type = type;
        this.E4J = E4J;
        index = -1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openHorizontalTraceabilityView(e);
    }

    public static void openHorizontalTraceabilityView(ActionEvent e) {
        switch (type) {
            case 1: // I* Horizontal Trace
                horizontalIStarTraceController = new HorizontalIStarTraceController(E4J);
                horizontalIStarTraceController.actionPerformed(e);
                index = 3;
                break;

            case 2: // BPMN Horizontal Trace
                horizontalBPMNTraceController = new HorizontalBPMNTraceController(E4J);
                horizontalBPMNTraceController.actionPerformed(e);
                index = 1;
                break;

            case 3: // UC Horizontal Trace
                horizontalUseCaseTraceController = new HorizontalUseCaseTraceController(E4J);
                horizontalUseCaseTraceController.actionPerformed(e);
                traceUCHorizontal = new TraceUCHorizontal();
                traceUCHorizontal.TraceElementsUCHorizontal();
                
                DatabaseConnection.criarSchema();
                new RastreabilidadeDAO().salvar(TraceUCHorizontal.getLista(), "UC Horizontal");
                
                index = 2;
                break;

            default:
                JOptionPane.showMessageDialog(null, "Sorry! An error occurred");
                break;
        }
        setVisebleFalse();
        openViewTraceabilityHorizontal();

    }

    public static void openViewTraceabilityHorizontal() {
        System.out.println("INDEX: "+index);
        System.out.println("TYPE: "+type);
        //if (index != -1) {
            if (viewTraceability1 == null) {
                viewTraceability1 = new TraceabilityView1(index, E4JiStar, E4JBPMN, E4JUseCases,
                        useCasesViewIStar, useCasesViewBPMN);
                viewTraceability1.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            }
            switch (type) {
                case 1: //update i* trace Horizontal
                    traceIStarHorizontal = new TraceIStarHorizontal();
                    traceIStarHorizontal.TraceElementsIStarHorizontal();
                    viewTraceability1.updateTableIStarHorizontalTraceability();
                    break;

                case 2: //update BPMN trace Horizontal
                    traceBPMNHorizontal = new TraceBPMNHorizontal();
                    traceBPMNHorizontal.TraceElementsBPMNHorizontal();
                    viewTraceability1.updateTableBPMNHorizontalTraceability();
                    break;

                case 3: //update UC trace Horizontale
                    viewTraceability1.updateTableUCHorizontalTraceability();
                    break;
            }
            viewTraceability1.atualizeType(index);
            viewTraceability1.setVisible(true);
       // } else {
       //     JOptionPane.showMessageDialog(null, "You need Mapping Horizontal first");
       // }
    }

    private static void setVisebleFalse() {
        if (E4JBPMN != null) {
            E4JBPMN.setVisible(false);
        }
        if (E4JiStar != null) {
            E4JiStar.setVisible(false);
        }
        if (E4JUseCases != null) {
            E4JUseCases.setVisible(false);
        }
    }
}
