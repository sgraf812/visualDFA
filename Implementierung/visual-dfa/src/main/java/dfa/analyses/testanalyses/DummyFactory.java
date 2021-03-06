package dfa.analyses.testanalyses;

import dfa.framework.DFADirection;
import dfa.framework.DFAFactory;
import dfa.framework.DataFlowAnalysis;
import dfa.framework.SimpleBlockGraph;

/**
 * Dummy factory for tests.
 * 
 * @author Patrick Petrovic
 */
public class DummyFactory extends DFAFactory<DummyElement> {

    @Override
    public String getName() {
        return "Dummy Analysis";
    }

    @Override
    public DFADirection getDirection() {
        return DFADirection.FORWARD;
    }

    @Override
    public DataFlowAnalysis<DummyElement> getAnalysis(SimpleBlockGraph blockGraph) {
        return new DummyAnalysis(blockGraph);
    }

}
