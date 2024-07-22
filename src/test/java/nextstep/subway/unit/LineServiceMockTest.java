package nextstep.subway.unit;

import nextstep.subway.domain.line.Line;
import nextstep.subway.domain.line.LineRepository;
import nextstep.subway.domain.line.LineService;
import nextstep.subway.domain.section.Section;
import nextstep.subway.domain.section.dto.SectionCreateRequest;
import nextstep.subway.domain.station.Station;
import nextstep.subway.domain.station.StationService;
import nextstep.subway.domain.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LineServiceMockTest {
    @Mock
    private LineRepository lineRepository;
    @Mock
    private StationService stationService;
    private LineService lineService;

    private Line 인천1호선;
    private Section 인천1호선_구간;
    private Station 계양역;
    private Station 국제업무지구역;
    private Station 인천터미널역;
    private Station 송도달빛축제공원역;
    private Station 신검암중앙역;

    @BeforeEach
    public void setUp() {
        lineService = new LineService(stationService, lineRepository);
        계양역 = new Station(1L, "계양역");
        국제업무지구역 = new Station(2L, "국제업무지구역");
        송도달빛축제공원역 = new Station(3L, "송도달빛축제공원역");
        신검암중앙역 = new Station(4L, "신검암중앙역");
        인천터미널역 = new Station(5L, "인천터미널역");
        인천1호선_구간 = new Section(계양역, 국제업무지구역, 15);
        인천1호선 = new Line("인천1호선", "bg-blue-400", 인천1호선_구간);
    }

    @DisplayName("새로운 구간을 노선의 끝에 추가한다.")
    @Test
    void addSectionToLast() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        //TODO: var를 사용하면 타입을 신경쓰지 않고 테스트의 과정에 집중할 수 있지만, 이걸 수정해야 한다면 타입을 확인하기 위한 과정 때문
        // 에 오히려 리팩토링에 방해가 될 것 같다. 그래서 도메인 레벨의 클래스 타입보다는 RestAssured Response 와 같이 제한적인 부분에만 사용하는 것이 좋다고 생각하는데 리뷰어 의견이 궁금하다.
        var 인천1호선_신구간 = new Section(국제업무지구역, 송도달빛축제공원역, 3);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(인천1호선));
        when(stationService.findByStationId(2L)).thenReturn(국제업무지구역);
        when(stationService.findByStationId(3L)).thenReturn(송도달빛축제공원역);


        // when
        // lineService.addSection 호출
        // TODO: 서비스 메서드는 DTO를 통해 전달받기 때문에 테스트 코드에서 엔티티를 Request 객체로 다시 매핑해 주어야 하는 불편함이 생긴다.
        //  또한 이 서비스 메서드가 하나의 DTO에 의존하게 되기 때문에 다른 DTO를 통한 사용에 제한이 될 수 있다고 생각한다.
        //  하지만 DTO의 목적이 계층 간의 데이터 전달이고, 엔티티를 계층 간 전달하면서 사용하는 것은 문제가 될 것 같은데, 왜 문제가 될 것 같은지를
        //  잘 모르겠다. 리뷰어는 어떻게 생각하시는지 궁금하다.
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);
        lineService.addSection(1L, 인천1호선_신구간_생성_요청);

        // then
        // lineService.findLineById 메서드를 통해 검증
        assertThat(lineService.findLineById(1L).getStations().size()).isEqualTo(3);
        assertThat(lineService.findLineById(1L).getStations().stream()
                .map(StationResponse::getName)
                .distinct()
                .collect(Collectors.toList())).containsExactly("계양역", "국제업무지구역", "송도달빛축제공원역");
    }

    @DisplayName("새로운 구간을 노선의 처음에 추가한다.")
    @Test
    void addSectionToFirst() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        var 인천1호선_신구간 = new Section(신검암중앙역, 계양역, 6);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(인천1호선));
        when(stationService.findByStationId(1L)).thenReturn(계양역);
        when(stationService.findByStationId(4L)).thenReturn(신검암중앙역);

        // when
        // lineService.addSection 호출
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);
        lineService.addSection(1L, 인천1호선_신구간_생성_요청);

        // then
        // lineService.findLineById 메서드를 통해 검증
        assertThat(lineService.findLineById(1L).getStations().size()).isEqualTo(3);
        assertThat(lineService.findLineById(1L).getStations().stream()
                .map(StationResponse::getName)
                .distinct()
                .collect(Collectors.toList())).containsExactly("신검암중앙역", "계양역", "국제업무지구역");
    }

    @DisplayName("기준역이 하행종점역인 새로운 구간을 노선의 가운데에 추가한다.")
    @Test
    void addSectionToMiddleBasedOnDownStation() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        var 인천1호선_신구간 = new Section(인천터미널역, 국제업무지구역, 3);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(인천1호선));
        when(stationService.findByStationId(2L)).thenReturn(국제업무지구역);
        when(stationService.findByStationId(5L)).thenReturn(인천터미널역);

        // when
        // lineService.addSection 호출
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);
        lineService.addSection(1L, 인천1호선_신구간_생성_요청);

        // then
        // lineService.findLineById 메서드를 통해 검증
        assertThat(lineService.findLineById(1L).getStations().size()).isEqualTo(3);
        assertThat(lineService.findLineById(1L).getStations().stream()
                .map(StationResponse::getName)
                .distinct()
                .collect(Collectors.toList())).containsExactly("계양역", "인천터미널역", "국제업무지구역");
    }

    @DisplayName("기준역이 상행종점역인 새로운 구간을 노선의 가운데에 추가한다.")
    @Test
    void addSectionToMiddleBasedOnUpStation() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        var 인천1호선_신구간 = new Section(계양역, 인천터미널역, 5);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(인천1호선));
        when(stationService.findByStationId(1L)).thenReturn(계양역);
        when(stationService.findByStationId(5L)).thenReturn(인천터미널역);

        // when
        // lineService.addSection 호출
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);
        lineService.addSection(1L, 인천1호선_신구간_생성_요청);

        // then
        // lineService.findLineById 메서드를 통해 검증
        assertThat(lineService.findLineById(1L).getStations().size()).isEqualTo(3);
        assertThat(lineService.findLineById(1L).getStations().stream()
                .map(StationResponse::getName)
                .distinct()
                .collect(Collectors.toList())).containsExactly("계양역", "인천터미널역", "국제업무지구역");
    }
}
