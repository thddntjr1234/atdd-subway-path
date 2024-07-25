package nextstep.subway.domain.section;

import nextstep.subway.domain.station.Station;

import java.util.List;

public interface SectionStrategy {
    void deleteSection(List<Section> sections, Station station);
}
