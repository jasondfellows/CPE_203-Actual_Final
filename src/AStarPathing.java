import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

class AStarPathing
{
    private List<Point> closed;
    private List<Point> open;
    private int numRuns;
    private Point wPos;
    private Point goalPos;
    public AStarPathing(){
        closed = new LinkedList<>();
        open = new LinkedList<>();
        numRuns = 0;
    }

    public static List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   Function<Point, Stream<Point>> potentialNeighbors)
    {
        List<Point> s = potentialNeighbors.apply(start) //this is wrong, replace w/ updated correct a star pathing
                .filter(canPassThrough)
                .sorted((p1, p2) -> p1.compareTo(p2, start, end))
                .toList();
        return s;
    }

}

