package nextstep.subway.domain.section;

import nextstep.subway.domain.station.Station;

import java.util.List;

public class MiddleCaseSectionStrategy implements SectionStrategy {
    @Override
    public void deleteSection(List<Section> sections, Station station) {
        Section leftSection = sections.stream()
                .filter(section -> section.getDownwardStation().equals(station))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("삭제할 역을 찾을 수 없습니다."));

        Section rightSection = sections.stream()
                .filter(section -> section.getUpwardStation().equals(station))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("삭제할 역을 찾을 수 없습니다."));

        leftSection.updateSection(leftSection.getUpwardStation(), rightSection.getDownwardStation(), leftSection.getDistance() + rightSection.getDistance());
        sections.remove(rightSection);
    }
}
