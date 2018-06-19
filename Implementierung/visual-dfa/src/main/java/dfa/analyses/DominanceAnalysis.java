package dfa.analyses;

import dfa.framework.BlockState;
import dfa.framework.DataFlowAnalysis;
import dfa.framework.SimpleBlockGraph;
import soot.Unit;
import soot.toolkits.graph.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static dfa.analyses.DominanceElement.BOTTOM;
import static dfa.analyses.DominanceElement.singleton;

public class DominanceAnalysis implements DataFlowAnalysis<DominanceElement> {
    private final SimpleBlockGraph blockGraph;
    private final Map<Unit, Block> unitToBlock = new HashMap<>();

    public DominanceAnalysis(SimpleBlockGraph blockGraph) {
        this.blockGraph = blockGraph;
    }

    private Block getContainingBlock(Unit unit) {
        if (unitToBlock.containsKey(unit)) {
            return unitToBlock.get(unit);
        }

        for (Block b : blockGraph) {
            for (Unit u : b) {
                if (u.equals(unit)) {
                    unitToBlock.put(unit, b);
                    return b;
                }
            }
        }

        throw new RuntimeException("No containing block for Unit " + unit);
    }

    @Override
    public DominanceElement transition(DominanceElement element, Unit unit) {
        return element.addDominator(this.getContainingBlock(unit));
    }

    @Override
    public DominanceElement join(Set<DominanceElement> elements) {
        DominanceElement ret = BOTTOM;
        for (DominanceElement e : elements) {
            ret = ret.join(e);
        }
        return ret;
    }

    @Override
    public Map<Block, BlockState<DominanceElement>> getInitialStates() {
        Map<Block, BlockState<DominanceElement>> ret = new HashMap<>();
        for (Block b : this.blockGraph) {
            DominanceElement e = this.isHead(b) ? singleton(b) : BOTTOM;
            ret.put(b, new BlockState<>(e, e));
        }
        return ret;
    }

    private boolean isHead(Block block) {
        return this.blockGraph.getHeads().contains(block);
    }
}
