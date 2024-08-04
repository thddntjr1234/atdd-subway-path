package nextstep.subway.domain.path;

import nextstep.subway.domain.section.Section;
import org.jgrapht.graph.DefaultWeightedEdge;

public class SectionEdge extends DefaultWeightedEdge {
    private Section section;

    public SectionEdge() {
    }

    public SectionEdge(Section section) {
        this.section = section;
    }

    private Long getUpwardStationId() {
        return section.getUpwardStation().getId();
    }

    private Long getDownwardStationId() {
        return section.getDownwardStation().getId();
    }
}
