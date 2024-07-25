package nextstep.subway.domain.section;

import nextstep.subway.domain.station.Station;

import java.util.List;

public class FirstCaseSectionStrategy implements SectionStrategy {
    @Override
    public void deleteSection(List<Section> sections, Station station) {
        Section section = sections.stream()
                .filter(element -> element.getUpwardStation().equals(station))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("삭제할 역을 찾을 수 없습니다."));
        sections.remove(section);
    }
}
