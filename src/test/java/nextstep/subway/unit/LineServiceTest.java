package nextstep.subway.unit;

import nextstep.subway.domain.line.Line;
import nextstep.subway.domain.line.LineRepository;
import nextstep.subway.domain.line.LineService;
import nextstep.subway.domain.section.Section;
import nextstep.subway.domain.section.dto.SectionCreateRequest;
import nextstep.subway.domain.station.Station;
import nextstep.subway.domain.station.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LineServiceTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private LineService lineService;

    @Test
    void addSection() {
        // given
        // stationRepository와 lineRepository를 활용하여 초기값 셋팅
        var 계양역 = new Station("계양역");
        var 국제업무지구역 = new Station("국제업무지구역");
        var 송도달빛축제공원역 = new Station("송도달빛축제공원역");
        // 역 저장
        Arrays.asList(계양역, 국제업무지구역, 송도달빛축제공원역)
                .forEach(station -> stationRepository.save(station));

        // 노선 저장
        var 인천1호선_구간 = new Section(계양역, 국제업무지구역, 15);
        var 인천1호선 = new Line("인천1호선", "bg-blue-400", 인천1호선_구간);
        인천1호선 = lineRepository.save(인천1호선);

        // 새 구간 request dto 생성
        var 인천1호선_신구간 = new Section(국제업무지구역, 송도달빛축제공원역, 3);
        var 인천1호선_신구간_생성_요청 = SectionCreateRequest.of(인천1호선_신구간);

        // when
        // lineService.addSection 호출
        lineService.addSection(인천1호선.getId(), 인천1호선_신구간_생성_요청);

        // then
        // line.getSections 메서드를 통해 검증
        Line line = lineRepository.findById(인천1호선.getId()).get();
        assertThat(line.getSections().size()).isEqualTo(2);
        assertThat(line.getSections().stream()
                .flatMap(section -> Stream.of(section.getUpwardStation().getName(), section.getDownwardStation().getName()))
                .collect(Collectors.toList()))
                .containsExactly("계양역", "국제업무지구역", "송도달빛축제공원역");

    }
}
