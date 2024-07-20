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

    @BeforeEach
    public void setUp() {
        lineService = new LineService(stationService, lineRepository);
    }

    @DisplayName("새로운 구간을 추가한다.")
    @Test
    void addSection() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        var 계양역 = new Station(1L, "계양역");
        var 국제업무지구역 = new Station(2L, "국제업무지구역");
        var 송도달빛축제공원역 = new Station(3L, "송도달빛축제공원역");
        var 인천1호선_구간 = new Section(계양역, 국제업무지구역, 15);
        var 인천1호선_신구간 = new Section(국제업무지구역, 송도달빛축제공원역, 3);
        var 인천1호선 = new Line("인천1호선", "bg-blue-400", 인천1호선_구간);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(인천1호선));
        when(stationService.findByStationId(2L)).thenReturn(국제업무지구역);
        when(stationService.findByStationId(3L)).thenReturn(송도달빛축제공원역);


        // when
        // lineService.addSection 호출
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);
        lineService.addSection(1L, 인천1호선_신구간_생성_요청);

        // then
        // lineService.findLineById 메서드를 통해 검증
        assertThat(lineService.findLineById(1L).getStations().size()).isEqualTo(3);
        assertThat(lineService.findLineById(1L).getStations().stream()
                .map(StationResponse::getName)
                .collect(Collectors.toList())).containsExactly("계양역", "국제업무지구역", "송도달빛축제공원역");
    }
}
