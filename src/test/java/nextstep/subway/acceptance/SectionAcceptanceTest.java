package nextstep.subway.acceptance;

import nextstep.subway.domain.line.dto.LineCreateRequest;
import nextstep.subway.domain.line.dto.LineResponse;
import nextstep.subway.domain.section.dto.SectionCreateRequest;
import nextstep.subway.domain.section.dto.SectionResponse;
import nextstep.subway.domain.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/line-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class SectionAcceptanceTest {
    private StationResponse 계양역;
    private StationResponse 국제업무지구역;
    private StationResponse 송도달빛축제공원역;
    private StationResponse 석남역; // 실패 케이스를 위한 역
    private LineResponse 인천1호선;

    @BeforeEach
    void setup() {
        계양역 = StationCommonApi.createStation("계양역").as(StationResponse.class);
        국제업무지구역 = StationCommonApi.createStation("국제업무지구역").as(StationResponse.class);
        송도달빛축제공원역 = StationCommonApi.createStation("송도달빛축제공원역").as(StationResponse.class);
        석남역 = StationCommonApi.createStation("석남역").as(StationResponse.class);
        인천1호선 = LineCommonApi.createLine(new LineCreateRequest("인천1호선", "bg-blue-400", 계양역.getId(), 국제업무지구역.getId(), 10)).as(LineResponse.class);
    }

    /**
     * Given: 등록된 노선이 있고
     * When: 관리자가 새로운 구간을 노선에 추가하면
     * Then: 노선에 새로운 구간이 추가된다.
     */
    @DisplayName("지하철 구간을 등록한다.")
    @Test
    void addSection() {
        //given
        SectionCreateRequest request = new SectionCreateRequest(국제업무지구역.getId(), 송도달빛축제공원역.getId(), 3);

        //when
        var response = LineCommonApi.addSection(인천1호선.getId(), request);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        //then
        var line = LineCommonApi.findLineById(인천1호선.getId()).as(LineResponse.class);
        assertThat(line.getStations().size()).isEqualTo(3);

        var addedSection = response.as(SectionResponse.class);
        assertThat(addedSection).isEqualTo(new SectionResponse(국제업무지구역.getId(), 송도달빛축제공원역.getId(), 3));
    }

    /**
     * Given: 등록된 노선과 노선에 추가로 등록된 구간이 있고
     * When: 관리자가 새로운 구간을 삭제하면
     * Then: 새로운 구간이 제거된다.
     */
    @DisplayName("지하철 구간을 삭제한다.")
    @Test
    void deleteSection() {
        //given
        SectionCreateRequest request = new SectionCreateRequest(국제업무지구역.getId(), 송도달빛축제공원역.getId(), 3);
        LineCommonApi.addSection(인천1호선.getId(), request);

        //when
        LineCommonApi.deleteSection(인천1호선.getId(), 송도달빛축제공원역.getId());

        //then
        LineResponse line = LineCommonApi.findLineById(인천1호선.getId()).as(LineResponse.class);
        List<String> names = line.getStations().stream().map(StationResponse::getName).collect(Collectors.toList());

        assertThat(names).containsExactly("계양역", "국제업무지구역");
    }

    /**
     * Given: 등록된 노선이 존재하고
     * When: 관리자가 등록된 노선의 하행역이 아닌 역을 새로운 구간의 상행역으로 추가하면
     * Then: 지하철 구간 등록에 실패한다.
     */
    @DisplayName("하행역 등록 조건을 위반하여 구간 등록에 실패한다.")
    @Test
    void createInvalidDownStationSection() {
        //given
        SectionCreateRequest request = new SectionCreateRequest(석남역.getId(), 송도달빛축제공원역.getId(), 3);

        //when
        var response = LineCommonApi.addSection(인천1호선.getId(), request);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given: 등록된 노선이 존재하고
     * When: 관리자가 노선에 등록된 역을 새로운 구간의 하행역으로 추가하면
     * Then: 지하철 구간 등록에 실패한다.
     */
    @DisplayName("중복역 등록 조건을 위반하여 구간 등록에 실패한다.")
    @Test
    void createDuplicatedStationSection() {
        //given
        SectionCreateRequest request = new SectionCreateRequest(국제업무지구역.getId(), 계양역.getId(), 3);

        //when
        var response = LineCommonApi.addSection(인천1호선.getId(), request);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given: 추가된 구간이 없는 노선이 등록되어 있고
     * When: 해당 노선의 하행역을 제거하면
     * Then: 지하철 구간 제거에 실패한다.
     */
    @DisplayName("지하철 구간 삭제 중 구간이 1개인 노선의 구간 삭제에 실패한다.")
    @Test
    void deleteMinimumSection() {
        //given

        //when
        var response = LineCommonApi.deleteSection(인천1호선.getId(), 송도달빛축제공원역.getId());

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given: 추가된 구간이 있는 노선이 등록되어 있고
     * When: 해당 노선에 등록되지 않은 역을 제거하면
     * Then: 지하철 구간 제거에 실패한다.
     */
    @DisplayName("지하철 구간 삭제 중 등록되지 않은 구간을 제거에 실패한다.")
    @Test
    void deleteUnknownSection() {
        //given

        //when
        var response = LineCommonApi.deleteSection(인천1호선.getId(), 석남역.getId());

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
