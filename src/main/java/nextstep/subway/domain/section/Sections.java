package nextstep.subway.domain.section;

import nextstep.subway.domain.station.Station;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Embeddable
public class Sections {
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "line_id")
    private List<Section> sections = new ArrayList<>();

    public void add(Section section) {
        this.sections.add(section);
    }

    public Sections() {
    }

    public Sections(List<Section> sections) {
        this.sections = sections;
    }


    public void addSection(Section newSection) {
        // 구간과 구간 내의 역이 노선에 이미 등록되어 있는 경우를 검증
        validateNewSection(newSection);

        if (isFirstAddCase(newSection)) {
            addSectionToFirst(newSection);
            return;
        }

        if (isLastAddCase(newSection)) {
            addSectionToLast(newSection);
            return;
        }

        if (isMiddleAddCase(newSection)) {
            addSectionToMiddle(newSection);
            return;
        }

        throw new IllegalArgumentException("추가할 구간의 역이 유효하지 않습니다.");
    }

    private void validateNewSection(Section newSection) {
        Station upStation = newSection.getUpwardStation();
        Station downStation = newSection.getDownwardStation();

        List<Station> stations = sections.stream()
                .flatMap(section -> Stream.of(section.getUpwardStation(), section.getDownwardStation()))
                .distinct()
                .collect(Collectors.toList());

        if (stations.contains(upStation) && stations.contains(downStation)) {
            throw new IllegalArgumentException("추가할 구간의 역이 모두 노선에 등록되어 있습니다.");
        }

        if (sections.contains(newSection)) {
            throw new IllegalArgumentException("추가할 구간이 이미 노선에 등록되어 있습니다.");
        }
    }

    private boolean isFirstAddCase(Section newSection) {
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("노선에 존재하는 구간이 없습니다.");
        }

        Section firstSection = sections.get(0);
        return firstSection.getUpwardStation().equals(newSection.getDownwardStation());
    }

    private boolean isLastAddCase(Section newSection) {
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("노선에 존재하는 구간이 없습니다.");
        }

        Section lastSection = sections.get(sections.size() - 1);
        return lastSection.getDownwardStation().equals(newSection.getUpwardStation());
    }

    private boolean isMiddleAddCase(Section newSection) {
        if (sections.stream()
                .anyMatch(e -> e.getUpwardStation().equals(newSection.getUpwardStation()))) {
            return true;
        }

        if (sections.stream()
                .anyMatch(e -> e.getDownwardStation().equals(newSection.getDownwardStation()))) {
            return true;
        }

        return false;
    }

    private void addSectionToFirst(Section newSection) {
        if (!sections.get(0).getUpwardStation().equals(newSection.getDownwardStation())) {
            throw new IllegalArgumentException("노선의 처음에 등록할 구간의 하행역은 기존 노선의 상행종점역이어야 합니다.");
        }

        sections.add(0, newSection);
    }

    private void addSectionToLast(Section newSection) {
        if (!sections.get(sections.size() - 1).getDownwardStation().equals(newSection.getUpwardStation())) {
            throw new IllegalArgumentException("노선의 끝에 등록할 구간의 상행역은 기존 노선의 하행종점역이어야 합니다.");
        }

        sections.add(newSection);
    }

    private void addSectionToMiddle(Section newSection) {
        Optional<Section> basedOnDownStation = sections.stream()
                .filter(e -> e.getDownwardStation().equals(newSection.getDownwardStation()))
                .findFirst();

        if (basedOnDownStation.isPresent()) {
            // 하행역 동일 기준 추가 프로세스
            validateMiddleAdditionCase(newSection, basedOnDownStation.get());
            middleCaseDownStationProcess(newSection);
            return;
        }

        Optional<Section> basedOnUpStation = sections.stream()
                .filter(e -> e.getUpwardStation().equals(newSection.getUpwardStation()))
                .findFirst();

        if (basedOnUpStation.isPresent()) {
            // 상행역 동일 기준 추가 프로세스
            validateMiddleAdditionCase(newSection, basedOnUpStation.get());
            middleCaseUpStationProcess(newSection);
        }

    }

    private void validateMiddleAdditionCase(Section newSection, Section targetSection) {
        // 가운데 추가 케이스의 경우 기준역이 같은 섹션의 distance를 차감해야 하기 때문에 distance 값 유효성 검증 진행
        if (targetSection.getDistance() <= newSection.getDistance()) {
            throw new IllegalArgumentException("추가할 구간의 거리가 기존 노선의 거리보다 짧아야 합니다.");
        }
    }

    private void middleCaseUpStationProcess(Section newSection) {
        // 상단 기준인 섹션의 index를 얻는다
        Section rightSection = sections.stream()
                .filter(section -> section.getUpwardStation().equals(newSection.getUpwardStation()))
                .findFirst().get();
        int replacementTargetSectionIndex = sections.indexOf(rightSection);

        // 해당 index의 섹션의 상행역을 newSection.downStation으로 업데이트하고 newSection의 distance만큼 감소시킨다.
        rightSection.updateSection(newSection.getDownwardStation(), rightSection.getDownwardStation(), rightSection.getDistance() - newSection.getDistance());

        // 기존 구간을 수정한 구간으로 교체하고 새로 추가되는 구간을 기존 구간의 앞에 추가한다.
        sections.set(replacementTargetSectionIndex, rightSection);
        sections.add(replacementTargetSectionIndex, newSection);
    }

    private void middleCaseDownStationProcess(Section newSection) {
        // 하단 기준인 섹션의 indx를 얻는다
        Section leftSection = sections.stream()
                .filter(section -> section.getDownwardStation().equals(newSection.getDownwardStation()))
                .findFirst().get();
        int replacementTargetSectionIndex = sections.indexOf(leftSection);

        // 해당 index의 섹션의 하행역을 newSection.upStation으로 업데이트하고 newSection의 distance만큼 감소시킨다.
        leftSection.updateSection(leftSection.getUpwardStation(), newSection.getUpwardStation(), leftSection.getDistance() - newSection.getDistance());

        // 수정한 구간을 기존 구간의 위치로 교체하고 새 구간을 그 뒤에 추가한다.
        sections.set(replacementTargetSectionIndex, leftSection);
        sections.add(replacementTargetSectionIndex + 1, newSection);
    }

    public void deleteSection(Station station) {
        if (sections.size() == 1) {
            throw new NoSuchElementException("구간이 1개인 경우 구간을 삭제할 수 없습니다.");
        }

        Section section = sections.stream()
                .filter(element -> element.getDownwardStation().equals(station))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구간입니다."));


        if (!isSectionLastElement(section)) {
            throw new IllegalArgumentException("마지막 구간에 해당하는 역만 삭제할 수 있습니다.");
        }

        sections.remove(section);
    }

    private boolean isSectionLastElement(Section section) {
        Section lastSection = sections.get(sections.size() - 1);
        return section.equals(lastSection);
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Station> getStations() {
        return sections.stream()
                .flatMap(section -> Stream.of(section.getUpwardStation(), section.getDownwardStation()))
                .distinct()
                .collect(Collectors.toList());
    }
}
