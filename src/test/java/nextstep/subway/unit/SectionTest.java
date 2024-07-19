package nextstep.subway.unit;

import nextstep.subway.domain.line.Line;
import nextstep.subway.domain.section.Section;
import nextstep.subway.domain.station.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("지하철 노선 구간 단위테스트")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SectionTest {
    private Line line;
    private Station upStation;
    private Station downStation;

    @BeforeEach
    void setup() {
        //given
        upStation = new Station(1L, "계양역");
        downStation = new Station(2L, "국제업무지구역");
        Section section = new Section(upStation, downStation, 15);
        line = new Line("인천1호선", "bg-blue-400", section);
    }

    @DisplayName("이미 해당 노선에 등록된 역은 새로운 구간의 하행역으로 추가할 수 없다.")
    @Test
    void downStationFailure() {
        //when
        Station newUpStation = upStation;
        Station newDownStation = new Station("계양역");
        Section newSection = new Section(newUpStation, newDownStation, 3);

        //then
        assertThatThrownBy(() -> line.addSection(newSection)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("새로운 구간의 상행역은 해당 노선에 등록되어있는 하행 종점역이어야 한다.")
    @Test
    void upStationFailure() {
        //when
        Station newUpStation = upStation;
        Station newDownStation = new Station(3L, "송도달빛축제공원역");
        Section newSection = new Section(newUpStation, newDownStation, 3);

        //then
        assertThatThrownBy(() -> line.addSection(newSection)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("정상적인 구간을 추가했을 때 새로운 구간이 노선에 추가된다.")
    @Test
    void successAdd() {
        //when
        Station newUpStation = downStation;
        Station newDownStation = new Station(3L, "송도달빛축제공원역");
        Section newSection = new Section(newUpStation, newDownStation, 3);

        line.addSection(newSection);

        //then
        assertThat(line.getSections()).containsAnyOf(newSection);
    }

    @DisplayName("구간이 1개인 경우 역을 삭제할 수 없다.")
    @Test
    void deleteFailure() {
        //given
        Station deleteTarget = downStation;
        assertThatThrownBy(() -> line.deleteSection(deleteTarget)).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("정상 구간을 제거한다.")
    @Test
    void successDelete() {
        //when
        Station newUpStation = downStation;
        Station newDownStation = new Station(3L, "송도달빛축제공원역");
        Section newSection = new Section(newUpStation, newDownStation, 3);
        line.addSection(newSection);

        line.deleteSection(newDownStation);
        assertThat(line.getSections().get(0)).isEqualTo(new Section(upStation, downStation, 15));
    }
}
