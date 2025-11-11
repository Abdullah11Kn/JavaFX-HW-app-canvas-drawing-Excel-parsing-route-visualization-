package domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TermSchedule {
    private final Map<String, CourseOffering> offeringsByCrn;

    /** Builds an index of course offerings keyed by CRN for fast lookups. */
    public TermSchedule(Collection<CourseOffering> offerings) {
        if (offerings == null) {
            throw new IllegalArgumentException("Offerings collection is required");
        }
        this.offeringsByCrn = offerings.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(CourseOffering::getCrn, o -> o, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    /** Returns an immutable view of every course offering in the term. */
    public Collection<CourseOffering> allOfferings() {
        return Collections.unmodifiableCollection(offeringsByCrn.values());
    }

    /** Looks up a single course offering by CRN or returns null when absent. */
    public CourseOffering findByCrn(String crn) {
        if (crn == null || crn.isBlank()) {
            return null;
        }
        return offeringsByCrn.get(crn.trim());
    }

    /** Resolves a collection of CRNs into the matching course offerings. */
    public List<CourseOffering> findAllByCrns(Iterable<String> crns) {
        if (crns == null) {
            throw new IllegalArgumentException("CRNs iterable is required");
        }
        java.util.ArrayList<CourseOffering> result = new java.util.ArrayList<>();
        crns.forEach(crn -> {
            CourseOffering offering = findByCrn(crn);
            if (offering != null) {
                result.add(offering);
            }
        });
        return Collections.unmodifiableList(result);
    }
}
