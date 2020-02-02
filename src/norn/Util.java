package norn;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {
    /**
     * Returns string of the elements in the set sorted by their natural ordering,
     * converted to strings, and joined with ", ".
     *
     * @param set Set of comparable elements
     * @param <L> A comparable type
     * @return An ordered string of elements in the set, joined with ", "
     */
    public static <L extends Comparable<L>> String setToOrderedString(Set<L> set) {
        String out = set.stream().sorted().map(Objects::toString).collect(Collectors.joining(", "));
        return out.length() != 0 ? out : "∅";
    }
}
