/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unioeste.jgoose.controller;

import br.unioeste.jgoose.TraceabilityHorizontal.TraceUCHorizontal;
import br.unioeste.jgoose.e4j.swing.EditorJFrame;
import br.unioeste.jgoose.model.TokensTraceability;
import br.unioeste.jgoose.model.TokensUseCase;
import br.unioeste.jgoose.model.UCActor;
import br.unioeste.jgoose.model.UCElement;
import br.unioeste.jgoose.model.UCLink;
import br.unioeste.jgoose.model.UCUseCase;
import br.unioeste.jgoose.view.Matriz;
import br.unioeste.jgoose.view.TraceabilityView;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 *
 * @author Victor Augusto Pozzan
 */
@SuppressWarnings(value = "serial")
public class HorizontalUseCaseTraceController extends AbstractAction {

    private EditorJFrame traceabilityView;
    private static final Logger LOG = Logger.getLogger("console");
    private static TraceUCHorizontal traceUCHorizontal;
    private TraceabilityView viewTraceability = null;
    private static TokensTraceability tokensTraceUC;

    private Map<mxCell, Element> vertex = new HashMap<>();
    private Map<mxCell, Element> edges = new HashMap<>();
    private Map<mxCell, Object> mapped = new HashMap<>();
    private Map<mxCell, mxCell> deleteds = new HashMap<>();

    private mxGraphComponent component;
    private mxGraph graph;
    private mxIGraphModel model;

    private TokensUseCase modelUC;

    public HorizontalUseCaseTraceController(EditorJFrame E4JUseCases) {
        this.traceabilityView = traceabilityView;
    }

    public static void setTokensHorizontal(TokensTraceability tokens) {
        HorizontalUseCaseTraceController.tokensTraceUC = tokens;
    }

    public static TokensTraceability getTokensTraceability() {
        return tokensTraceUC;
    }

        public static Matriz propertiesMatriz(int indice) {
        String title;
        switch (indice) {
            case 0: //Informação Externa x Informação Externa
                title = " ← <AGREGAÇÃO> Informação Externa x Informação Externa  <AGREGAÇÃO>";
                Matriz matriz0 = new Matriz(title, tokensTraceUC.getInformcaoExterna().size(),
                        tokensTraceUC.getInformcaoExterna().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna(),
                        true);
                return matriz0;
            case 1: //Informação Externa x Informação Organizacional
                title = " ← <REC> Informação Externa x Informação Organizacional <REC>";
                Matriz matriz1 = new Matriz(title, tokensTraceUC.getInformcaoExterna().size(),
                        tokensTraceUC.getInformacaoOrg().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(),
                        false);
                return matriz1;
            case 2: //Informação Organizacional x Informação Organizacional
                title = " ← <AGREGAÇÃO> Informação Organizacional x Informação Organizacional  <AGREGAÇÃO>";
                Matriz matriz2 = new Matriz(title, tokensTraceUC.getInformacaoOrg().size(),
                        tokensTraceUC.getInformacaoOrg().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(),
                        true);
                return matriz2;                
            case 3: //Informação Organizacional x Requisitos
                title = " ← <REC> Informação Organizacional x Requisitos  <REC>";
                Matriz matriz3 = new Matriz(title, tokensTraceUC.getInformacaoOrg().size(),
                        tokensTraceUC.getRequisitos().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        false);
                return matriz3;                 
            case 4: //Objetivo do Sistema x Objetivo do Sistema
                title = " ← <AGREGAÇÃO> Objetivo do Sistema x Objetivo do Sistema  <AGREGAÇÃO>";
                Matriz matriz4 = new Matriz(title, tokensTraceUC.getObjetivoSistema().size(),
                        tokensTraceUC.getObjetivoSistema().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        true);
                return matriz4;                 
            case 5: //Objetivo do Sistema x Requisitos
                title = " ← <SAT> Objetivo do Sistema x Requisitos  <SAT>";
                Matriz matriz5 = new Matriz(title, tokensTraceUC.getObjetivoSistema().size(),
                       tokensTraceUC.getRequisitos().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        false);
                return matriz5;  
            case 6: //Stakeholder x Stakeholder
                title = " ← <AGREGAÇÃO> Stakeholder x Stakeholder  <AGREGAÇÃO>";
                Matriz matriz6 = new Matriz(title, tokensTraceUC.getStakeholders().size(),
                        tokensTraceUC.getStakeholders().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getStakeholders(),
                        HorizontalBPMNTraceController.getTokensTraceability().getStakeholders(),
                        true);
                return matriz6;                 
            case 7: //Stakeholder x Requisitos
                title = " ← <RESP> Stakeholder x Requisitos  <RESP>";
                Matriz matriz7 = new Matriz(title, tokensTraceUC.getStakeholders().size(),
                        tokensTraceUC.getRequisitos().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getStakeholders(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        false);
                return matriz7;                    
            case 8: //Requisitos x Requisitos
                title = " ← <AGREGAÇÃO> Requisitos x Requisitos  <AGREGAÇÃO>";
                Matriz matriz8 = new Matriz(title, tokensTraceUC.getRequisitos().size(),
                        tokensTraceUC.getRequisitos().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        true);
                return matriz8;                    
            case 9: //Requisitos x Informação Externa
                title = " ← <REC> Requisitos x Informação Externa  <REC>";
                Matriz matriz9 = new Matriz(title, tokensTraceUC.getRequisitos().size(),
                        tokensTraceUC.getInformcaoExterna().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getRequisitos(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformcaoExterna(),
                        false);
                return matriz9;               
            case 10: //Objetivo do Sistema x Informação Organizacional
                title = " ← <REC> Objetivo do Sistema x Informação Organizacional  <REC>";
                Matriz matriz10 = new Matriz(title, tokensTraceUC.getObjetivoSistema().size(),
                        tokensTraceUC.getInformacaoOrg().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(),
                        false);
                return matriz10;                       
            case 11: //Objetivo do Sistema x Objetivo do Sistema
                title = " ← <AGREGAÇÃO> Objetivo do Sistema x Objetivo do Sistema  <AGREGAÇÃO>";
                Matriz matriz11 = new Matriz(title, tokensTraceUC.getObjetivoSistema().size(),
                        tokensTraceUC.getObjetivoSistema().size(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),
                        true);
                return matriz11;                      
        }
        Matriz mm = new Matriz(0, 0 , "This Matri doesn't exist");
        return mm;
    }

    
    public static void selectMatriz(int indice) {
        String title;
        switch (indice) {
            case 0: //Informação Externa x Informação Externa
                title = " ← <AGREGAÇÃO> Informação Externa x Informação Externa  <AGREGAÇÃO>";
                Matriz m0 = new Matriz(tokensTraceUC.getInformcaoExterna().size(), tokensTraceUC.getInformcaoExterna().size(), title);
                m0.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna(), true);//row
                break;
            case 1: //Informação Externa x Informação Organizacional
                title = " ← <REC> Informação Externa x Informação Organizacional <REC>";
                Matriz m1 = new Matriz(tokensTraceUC.getInformcaoExterna().size(), tokensTraceUC.getInformacaoOrg().size(), title);
                m1.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg(), false);//row          

                break;
            case 2: //Informação Organizacional x Informação Organizacional
                title = " ← <AGREGAÇÃO> Informação Organizacional x Informação Organizacional  <AGREGAÇÃO>";
                Matriz m2 = new Matriz(tokensTraceUC.getInformacaoOrg().size(), tokensTraceUC.getInformacaoOrg().size(), title);
                m2.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg(), true);//row
                break;
            case 3: //Informação Organizacional x Requisitos
                title = " ← <REC> Informação Organizacional x Requisitos  <REC>";
                Matriz m3 = new Matriz(tokensTraceUC.getInformacaoOrg().size(), tokensTraceUC.getRequisitos().size(), title);
                m3.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getInformacaoOrg(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(), false);//row
                break;
            case 4: //Objetivo do Sistema x Objetivo do Sistema
                title = " ← <AGREGAÇÃO> Objetivo do Sistema x Objetivo do Sistema  <AGREGAÇÃO>";
                Matriz m4 = new Matriz(tokensTraceUC.getObjetivoSistema().size(), tokensTraceUC.getObjetivoSistema().size(), title);
                m4.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema(), true);//row
                break;
            case 5: //Objetivo do Sistema x Requisitos
                title = " ← <SAT> Objetivo do Sistema x Requisitos  <SAT>";
                Matriz m5 = new Matriz(tokensTraceUC.getObjetivoSistema().size(), tokensTraceUC.getRequisitos().size(), title);
                m5.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(), false);//row
                break;
            case 6: //Stakeholder x Stakeholder
                title = " ← <AGREGAÇÃO> Stakeholder x Stakeholder  <AGREGAÇÃO>";
                Matriz m6 = new Matriz(tokensTraceUC.getStakeholders().size(), tokensTraceUC.getStakeholders().size(), title);
                m6.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders(), true);//row
                break;
            case 7: //Stakeholder x Requisitos
                title = " ← <RESP> Stakeholder x Requisitos  <RESP>";
                Matriz m7 = new Matriz(tokensTraceUC.getStakeholders().size(), tokensTraceUC.getRequisitos().size(), title);
                m7.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getStakeholders(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(), false);//row

                break;
            case 8: //Requisitos x Requisitos
                title = " ← <AGREGAÇÃO> Requisitos x Requisitos  <AGREGAÇÃO>";
                Matriz m8 = new Matriz(tokensTraceUC.getRequisitos().size(), tokensTraceUC.getRequisitos().size(), title);
                m8.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(), true);//row
                break;
            case 9: //Requisitos x Informação Externa
                title = " ← <REC> Requisitos x Informação Externa  <REC>";
                Matriz m9 = new Matriz(tokensTraceUC.getRequisitos().size(), tokensTraceUC.getInformcaoExterna().size(), title);
                m9.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getRequisitos(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getInformcaoExterna(), false);//row
                break;
            case 10: //Objetivo do Sistema x Informação Organizacional
                title = " ← <REC> Objetivo do Sistema x Informação Organizacional  <REC>";
                Matriz m10 = new Matriz(tokensTraceUC.getObjetivoSistema().size(), tokensTraceUC.getInformacaoOrg().size(), title);
                m10.matriz(HorizontalBPMNTraceController.getTokensTraceability().getObjetivoSistema(),//col
                        HorizontalBPMNTraceController.getTokensTraceability().getInformacaoOrg(), false);//row
                break;
            case 11: //Objetivo do Sistema x Objetivo do Sistema
                title = " ← <AGREGAÇÃO> Objetivo do Sistema x Objetivo do Sistema  <AGREGAÇÃO>";
                Matriz m11 = new Matriz(tokensTraceUC.getObjetivoSistema().size(), tokensTraceUC.getObjetivoSistema().size(), title);
                m11.matriz(HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema(),//col
                        HorizontalUseCaseTraceController.getTokensTraceability().getObjetivoSistema(), true);//row
                break;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LOG.debug("BPMN Traceability Horizontal init.");
        vertex.clear();
        edges.clear();
        modelUC = new TokensUseCase();

        UCController.setTokensUC(modelUC);

        Object source = e.getSource();

        if (source instanceof mxGraphComponent) {
            this.component = (mxGraphComponent) source;
            this.graph = component.getGraph();
            this.model = graph.getModel();

            // get all cells
            Object[] cells = this.selectAll();

            // filter cells and put them in the map structure (elements and links)
            // 1) compute all vertex, including the ones nested in containers
            //    (the use cases are usually drawn inside the system boundary)
            for (Object c : cells) {
                findVertices((mxCell) c);
            }

            // 2) compute all edges
            for (Object c : cells) {
                mxCell cell = (mxCell) c;

                findEdges(cell);
            }

            LOG.debug("total elements interpreted: " + vertex.size());
            LOG.debug("total links interpreted: " + edges.size());


        }

    }

    private Object[] selectAll() {
        // first, get user selected cells
        graph.clearSelection();
        graph.selectAll();
        Object[] cells = graph.getSelectionCells();
        graph.clearSelection();

        LOG.debug("Total root cells founded: " + cells.length);
        return cells;
    }

    private Object convertEdge(mxCell cell) {
        Object result = null;
        Element element = null;
        Object v = cell.getValue();

        if (v instanceof Element) {
            element = (Element) v;
        } else {
            LOG.debug("Value of cell is not a Element type.");
            return null;
        }

        String tagName = element.getTagName();
        String type = element.getAttribute("type");

        if (tagName == null || type == null) {
            LOG.debug("tagname and type is null.");
            return null;
        }

        edges.put(cell, element);

        // 1) get elements of this edge
        mxCell source = (mxCell) cell.getSource();
        mxCell target = (mxCell) cell.getTarget();

        // 2) get mapped elements to create a link between then
        Object mappedSource = mapped.get(source);
        if (mappedSource == null) {
            LOG.error("mapped source dont founded.");
        }

        Object mappedTarget = mapped.get(target);
        if (mappedTarget == null) {
            LOG.error("mapped target dont founded.");
        }

        if (mappedSource != null && mappedTarget != null) {
            UCLink ucLink = new UCLink();

            ucLink.setCode(cell.getId());
            ucLink.setLabel(element.getAttribute("label").replaceAll("\n", " ").replaceAll("\\s+", " ").replaceAll("^\\s+", ""));
            ucLink.setFrom((UCElement) mappedSource);
            ucLink.setTo((UCElement) mappedTarget);

            switch (type) {
                case "extend":
                    ucLink.setType(UCLink.EXTEND);
                    break;
                case "association":
                    ucLink.setType(UCLink.ASSOCIATION);
                    break;
                case "generalization":
                    ucLink.setType(UCLink.GENERALIZATION);
                    break;
                case "include":
                    ucLink.setType(UCLink.INCLUDE);
                    break;
                default:
                    LOG.debug("case for "
                            + "'" + type
                            + "' link type is not implemented yet.");
                    break;
            }

            result = ucLink.getCode();
            mapped.put(cell, ucLink);
            modelUC.addLink(ucLink);

            // update link in 'from' and 'to'
            if (mappedSource instanceof UCElement) {
                UCElement ucElementSource = (UCElement) mappedSource;
                ucElementSource.addLinkFrom(ucLink);
            }

            if (mappedTarget instanceof UCElement) {
                UCElement ucElementTarget = (UCElement) mappedTarget;
                ucElementTarget.addLinkTo(ucLink);
            }
        }
        return result;
    }

    /**
     * Converts the vertex and all of its descendants. selectAll() only returns the root cells, so the
     * elements placed inside a container (e.g. the system boundary) would be ignored, while the edges
     * that touch them were still found by findEdges(), producing "mapped source/target dont founded".
     */
    private void findVertices(mxCell cell) {
        if (cell.isVertex()) {
            this.convertVertex(cell);
        }
        for (int i = 0; i < cell.getChildCount(); i++) {
            findVertices((mxCell) cell.getChildAt(i));
        }
    }

    private void findEdges(mxCell cell) {
        if (!(cell.getValue() instanceof Element)) {
            return;
        }

        if (cell.isEdge()) {
            convertEdge((mxCell) cell);
            return;
        } else if (cell.getChildCount() == 0) {
            return;
        } else {
            for (int i = 0; i < cell.getChildCount(); i++) {
                findEdges((mxCell) cell.getChildAt(i));
            }
        }
    }

    private Object convertVertex(mxCell cell) {
        Object result = null;
        Object father = null;

        Element element = null;
        Object v = cell.getValue();

        if (v instanceof Element) {
            element = (Element) v;
        } else {
            LOG.debug("Value of cell is not a Element type.");
            return null;
        }

        String tagName = element.getTagName();
        String type = element.getAttribute("type");

        if (tagName == null || type == null) {
            LOG.debug("tagname and type is null.");
            return null;
        }

        System.out.println("TagNAME:" + tagName);
        switch (tagName) {
            // is a vertex
            case "actorLink":
                vertex.put(cell, element);

                UCActor actor = new UCActor();
                actor.setCode(cell.getId());
                actor.setName(element.getAttribute("label").replaceAll("\n", " ").replaceAll("\\s+", " ").replaceAll("^\\s+", ""));
                System.out.println("TYPE ACTOR:" + type);

                // set father, if exists
                father = cell.getParent();

                modelUC.addActor(actor);
                mapped.put(cell, actor);
                result = actor.getCode();

                break;
            case "ielement":
                vertex.put(cell, element);

                UCUseCase usecase = new UCUseCase();
                usecase.setCode(cell.getId());
                usecase.setName(element.getAttribute("label").replaceAll("\n", " ").replaceAll("\\s+", " ").replaceAll("^\\s+", ""));

                modelUC.addUseCase(usecase);
                mapped.put(cell, usecase);
                result = usecase.getCode();

                break;
        }
        return result;
    }
}
