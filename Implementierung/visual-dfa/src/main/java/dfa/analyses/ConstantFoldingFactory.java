package dfa.analyses;

import dfa.framework.DFADirection;
import dfa.framework.DFAFactory;
import dfa.framework.DataFlowAnalysis;
import dfa.framework.SimpleBlockGraph;

/**
 * @author Nils Jessen
 * @author Sebastian Rauch
 * 
 * A {@code ConstantFoldingAnalysis} creates {@code ConstantFoldingAnalysis}-Instances from {@code SimpleBlockGraph}.
 *
 */
public class ConstantFoldingFactory extends DFAFactory<ConstantFoldingElement> {

    @Override
    public String getName() {
        return "Constant-Folding";
    }

    @Override
    public DFADirection getDirection() {
        return DFADirection.FORWARD;
    }

    @Override
    public DataFlowAnalysis<ConstantFoldingElement> getAnalysis(SimpleBlockGraph blockGraph) {
        return new ConstantFoldingAnalysis(blockGraph);
    }

}
