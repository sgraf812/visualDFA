package gui.visualgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;

import dfa.framework.AbstractBlock;
import dfa.framework.BasicBlock;
import dfa.framework.BlockState;
import dfa.framework.ControlFlowGraph;
import dfa.framework.DFAExecution;
import dfa.framework.ElementaryBlock;
import dfa.framework.LatticeElement;
import gui.StatePanelOpen;

/**
 * Main interface for interaction. Provides important methods to get data from DFAFramework and to build the visual
 * graph. Additionally, it handles creation of VisualGraphPanel and graph export.
 * 
 * @author Patrick Petrovic
 */
public class GraphUIController {
    private VisualGraphPanel panel;
    private mxGraph graph;
    private DFAExecution<? extends LatticeElement> dfa;
    private StatePanelOpen statePanel = null;
    private final Map<AbstractBlock, UIAbstractBlock> mappedAbstractBlocks = new HashMap<>();

    /**
     * Creates a new {@code GraphUIController}.
     *
     * @param panel
     *        the {@code VisualGraphPanel} this controller should operate on
     */
    public GraphUIController(VisualGraphPanel panel) {
        this.panel = panel;
        this.graph = panel.getMxGraph();
    }

    /**
     * Gets all relevant data from DFAFramework and orders {@code VisualGraphPanel} to create and render the
     * corresponding visual graph.
     *
     * @param dfa
     *        the {@code DFAExecution} of the current data-flow analysis
     */
    public void start(final DFAExecution<? extends LatticeElement> dfa) {
        if (this.dfa != null) {
            throw new IllegalStateException("Visual graph was already built.");
        }

        this.dfa = dfa;

        ControlFlowGraph dfaGraph = dfa.getCFG();
        List<BasicBlock> dfaBasicBlocks = dfaGraph.getBasicBlocks();
        Map<BasicBlock, UIBasicBlock> mappedBasicBlocks = new HashMap<>();
        final List<UIAbstractBlock> uiBlocks = new ArrayList<>();
        final Map<mxCell, UIAbstractBlock> mxCellMap = new HashMap<>();

        // First step: Build all visual blocks from DFAFramework data.
        for (int i = 0; i < dfaBasicBlocks.size(); i++) {
            BasicBlock dfaBasicBlock = dfaBasicBlocks.get(i);
            UIBasicBlock basicBlock = new UIBasicBlock(graph, dfaBasicBlock, dfa);

            uiBlocks.add(basicBlock);
            basicBlock.setBlockNumber(i);
            List<ElementaryBlock> elementaryBlocks = dfaBasicBlock.getElementaryBlocks();

            if (elementaryBlocks.size() != 0) {
                List<UILineBlock> lineBlocks = new ArrayList<>();

                // The first UILineBlock is a special case: it has no predecessor.
                ElementaryBlock firstElementaryBlock = elementaryBlocks.get(0);
                UILineBlock firstLineBlock =
                        new UILineBlock(firstElementaryBlock, panel.getGraphComponent(), graph, basicBlock);

                lineBlocks.add(firstLineBlock);
                uiBlocks.add(firstLineBlock);
                firstLineBlock.setBlockNumber(0);
                basicBlock.insertLineBlock(lineBlocks.get(0));
                mappedAbstractBlocks.put(firstElementaryBlock, firstLineBlock);

                for (int n = 1; n < elementaryBlocks.size(); n++) {
                    ElementaryBlock currentElementaryBlock = elementaryBlocks.get(n);
                    UILineBlock newLineBlock = new UILineBlock(currentElementaryBlock, panel.getGraphComponent(), graph,
                            basicBlock, lineBlocks.get(n - 1));

                    lineBlocks.add(newLineBlock);
                    uiBlocks.add(newLineBlock);
                    newLineBlock.setBlockNumber(n);
                    basicBlock.insertLineBlock(lineBlocks.get(n));
                    mappedAbstractBlocks.put(currentElementaryBlock, newLineBlock);
                }
            }

            mappedAbstractBlocks.put(dfaBasicBlock, basicBlock);
            mappedBasicBlocks.put(dfaBasicBlock, basicBlock);
            panel.insertBasicBlock(basicBlock);
        }

        panel.setBlockMap(mappedAbstractBlocks);

        // Now all visual blocks have been built, so we can create edges.
        for (BasicBlock dfaBasicBlock : dfaBasicBlocks) {
            UIBasicBlock currentBlock = mappedBasicBlocks.get(dfaBasicBlock);
            List<UIBasicBlock> successors = new ArrayList<>();

            for (BasicBlock dfaSuccessor : dfaGraph.getSuccessors(dfaBasicBlock)) {
                successors.add(mappedBasicBlocks.get(dfaSuccessor));
            }

            for (UIBasicBlock successor : successors) {
                panel.insertEdge(new UIEdge(graph, currentBlock, successor));
            }
        }

        panel.renderGraph(dfa);

        for (UIAbstractBlock block : uiBlocks) {
            mxCellMap.put(block.getMxCell(), block);
        }

        graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxEventSource.mxIEventListener() {
            @Override
            public void invoke(Object o, mxEventObject mxEventObject) {
                // Weird API: "removed" cells are actually the newly selected cells.
                @SuppressWarnings("unchecked")
                ArrayList<mxCell> selectedCells = (ArrayList<mxCell>) mxEventObject.getProperty("removed");

                if (selectedCells != null && selectedCells.size() > 0) {
                    mxCell selectedCell = selectedCells.get(0);
                    panel.setSelectedBlock(mxCellMap.get(selectedCell));

                } else {
                    panel.setSelectedBlock(null);

                }

                updateStatePanel();
            }
        });
    }

    /**
     * Sets the {@code StatePanelOpen} to show the results in. If {@code null}, no results will be shown.
     *
     * @param statePanel
     *        the state panel
     */
    public void setStatePanel(StatePanelOpen statePanel) {
        this.statePanel = statePanel;
    }

    /**
     * Refreshes the (already rendered) visual graph using the {@code DFAExecution} instance previously given to {@code
     * start()}.
     */
    public void refresh() {
        if (dfa == null) {
            throw new IllegalStateException("Graph has not been built using start() yet.");
        }

        panel.renderGraph(dfa);

        // If Jump to Action is enabled, the block selection will change, automatically triggering updateStatePanel().
        if (!panel.isJumpToActionEnabled()) {
            updateStatePanel();
        }
    }

    /**
     * Deletes the current visual graph and clears the {@code DFAExecution} previously given to {@code start()}
     */
    public void stop() {
        dfa = null;
        panel.deleteGraph();
        graph = panel.getMxGraph();
    }

    private void updateStatePanel() {
        UIAbstractBlock selectedBlock = panel.getSelectedBlock();

        if (statePanel != null && selectedBlock != null) {
            AbstractBlock selectedAbstractBlock = selectedBlock.getDFABlock();
            UIAbstractBlock uiAbstractBlock = mappedAbstractBlocks.get(selectedAbstractBlock);

            BlockState<? extends LatticeElement> currentState =
                    dfa.getCurrentAnalysisState().getBlockState(selectedAbstractBlock);
            LatticeElement inState = currentState.getInState();
            LatticeElement outState = currentState.getOutState();
            String inStateString = inState == null ? "<not set>" : inState.getStringRepresentation();
            String outStateString = outState == null ? "<not set>" : outState.getStringRepresentation();

            String text = uiAbstractBlock.getText();
            int blockNumber = uiAbstractBlock.getBlockNumber();

            // Parent block selected? Then lineNumber == -1, but first code line for StatePanelOpen should be 0 as it
            // shows the whole block.
            int lineNumber = Math.max(uiAbstractBlock.getLineNumber(), 0);

            statePanel.setIn(inStateString);
            statePanel.setOut(outStateString);
            statePanel.setSelectedLine(text, blockNumber, lineNumber);
        } else if (statePanel != null) {
            statePanel.reset();
        }
    }
}
