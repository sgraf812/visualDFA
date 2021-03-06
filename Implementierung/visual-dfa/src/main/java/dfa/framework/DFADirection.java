package dfa.framework;

/**
 * A {@code DFADirection} describes the direction of a data-flow-analysis.
 * 
 * @author Sebastian Rauch
 */
public enum DFADirection {

    /**
     * for forward-analyses
     */
    FORWARD("forward"),

    /**
     * for backward-analyses
     */
    BACKWARD("backward");

    private String description;

    private DFADirection(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
