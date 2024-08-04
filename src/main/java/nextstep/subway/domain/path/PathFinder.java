package nextstep.subway.domain.path;

import nextstep.subway.domain.section.Section;
import nextstep.subway.domain.station.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PathFinder {

    private WeightedMultigraph<Long, SectionEdge> graph;

    public PathFinder() {
        graph = new WeightedMultigraph<>(SectionEdge.class);
    }

    public PathFinder(WeightedMultigraph<Long, SectionEdge> graph) {
        this.graph = graph;
    }

    public Optional<GraphPath> findPath(Station source, Station target, List<Section> edges) {
        validateStaions(source, target);

        setUpWeightedEdges(edges);
        DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(graph);
        return Optional.ofNullable(dijkstraShortestPath.getPath(source.getId(), target.getId()));
    }

    private void validateStaions(Station source, Station target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("출발역과 도착역이 유효한 역이 아닙니다.");
        }

        if (source.equals(target)) {
            throw new IllegalArgumentException("출발역과 도착역은 서로 같을 수 없습니다");
        }
    }

    private void setUpWeightedEdges(List<Section> edges) {
        edges.stream()
                .flatMap(edge -> Stream.of(edge.getUpwardStation().getId(), edge.getDownwardStation().getId()))
                .distinct()
                .forEach(graph::addVertex);

        edges.forEach(edge -> {
            SectionEdge sectionEdge = graph.addEdge(edge.getUpwardStation().getId(), edge.getDownwardStation().getId());
            graph.setEdgeWeight(sectionEdge, edge.getDistance());
        });
    }
}
