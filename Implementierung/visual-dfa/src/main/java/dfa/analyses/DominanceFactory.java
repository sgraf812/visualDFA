package dfa.analyses;

import dfa.framework.*;

/**
 * A {@code DominanceAnalysis} creates {@code DominanceAnalysis}-Instances from {@code SimpleBlockGraph}.
 * 
 * @author Sebastian Graf
 */
public class DominanceFactory extends DFAFactory<DominanceElement> {

    @Override
    public String getName() {
        return "Dominance";
    }

    @Override
    public DFADirection getDirection() {
        return DFADirection.FORWARD;
    }

    @Override
    public DataFlowAnalysis<DominanceElement> getAnalysis(SimpleBlockGraph blockGraph) {
        return new DominanceAnalysis(blockGraph);
    }

}
