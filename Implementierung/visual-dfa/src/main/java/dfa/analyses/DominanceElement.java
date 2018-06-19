package dfa.analyses;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dfa.framework.LatticeElement;
import soot.toolkits.graph.Block;

import java.util.Objects;
import java.util.Set;

/**
 * A {@code DominanceElement} is a {@code LatticeElement} used by {@code DominanceAnalysis}.
 * 
 * @author Sebastian Graf
 */
public class DominanceElement implements LatticeElement {
    public static final DominanceElement BOTTOM = new DominanceElement(null);
    private final ImmutableSet<Block> dominators; // dominators == null <=> this == BOTTOM

    private boolean isBottom() {
        return this == BOTTOM;
    }

    public DominanceElement(Set<Block> dominators) {
        if (dominators != null) {
            this.dominators = ImmutableSet.<Block>builder().addAll(dominators).build();
        } else {
            this.dominators = null; // the BOTTOM case
        }
    }

    public DominanceElement addDominator(Block dominator) {
        if (this.isBottom()) {
            return this;
        }
        return new DominanceElement(ImmutableSet.<Block>builder().addAll(dominators).add(dominator).build());
    }

    public DominanceElement join(DominanceElement other) {
        if (this.isBottom()) {
            return other;
        }
        if (other.isBottom()) {
            return this;
        }
        return new DominanceElement(Sets.intersection(this.dominators, other.dominators));
    }

    public static DominanceElement singleton(Block dominator) {
        return new DominanceElement(Sets.<Block>newHashSet(dominator));
    }

    @Override
    public String getStringRepresentation() {
        if (this.isBottom()) {
            return "<BOTTOM>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;
        for (Block dom : dominators) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(dom.getIndexInMethod());
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DominanceElement that = (DominanceElement) o;
        return Objects.equals(dominators, that.dominators);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dominators);
    }
}
