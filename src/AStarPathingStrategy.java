import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy
        implements PathingStrategy
{


    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors)
    {
        List<Point> path = new LinkedList<>();
        Comparator<Node> sortQueue = Comparator.comparingInt(Node::getF).thenComparing(Node::getG);
        PriorityQueue<Node> open_q = new PriorityQueue<>(sortQueue);
        HashMap<Point, Node> open_hash = new HashMap<>(); //see if its already in list
        HashMap<Point, Node> closed_hash = new HashMap<>(); //node = value, key = number
        //for each iteration: get node, add its neighbors, check if neighbor is final, else get next node and move og node to hash_closed

        // STEP 2
        Node scur = new Node(start, 0, heuristicDistance(start, end),null);
        open_q.add(scur);
        open_hash.put(scur.getLoc(),scur);


        // STEP 3
        while(open_q.size() > 0)
        {
            Node cur = open_q.poll();
            closed_hash.put(cur.getLoc(), cur);
            open_hash.remove(cur);

            if (withinReach.test(cur.getLoc(), end))
            {
                return makePath(path, cur);
            }
//            System.out.println("CUR: " + cur.getLoc()+ "---");

            List<Point> options = potentialNeighbors
                    .apply(cur.getLoc())
                    .filter(canPassThrough)
                    .filter(pt -> !pt.equals(start) && !pt.equals(end))
                    .filter(pt -> !closed_hash.containsKey(pt))
                    .collect(Collectors.toList());

            for (Point p : options)
            {

                int g = cur.getG() + adj(cur.getLoc(), p);
//                int g = cur.getG() + 10;
//                int gg = cur.getG() + 10; //cur_adj; //for else stmt
//                int g = heuristicDistance(cur.getLoc(), start) + (cur_adj);
//                int h = heuristicDistance(p, end);
                int h = Math.abs(end.x - p.x)*10 + Math.abs(end.y - p.y)*10;

                Node temp = new Node(p, g, h, cur);
                if (open_hash.containsKey(p) == false) //Step 3A and 3B
                {
                    open_q.add(temp);
                    open_hash.put(p, temp);
                }
                else if (open_hash.get(p).getG() > g)  //Step 3C
                {
//                    temp = open_hash.get(p);
                    open_q.remove(open_hash.get(p));
//                    temp.setG(g);
                    open_hash.replace(p, temp);
                    open_q.add(temp);
                }

            }
            //Step 4 move cur from open to closed

            //Step 5 = at the top
        }
        return path;
    }


    public List<Point> makePath(List<Point> listt, Node finall)
    {
        listt.add(finall.getLoc());

        if(finall.prior == null)
        {
            Collections.reverse(listt);
            listt.remove(0);
//            System.out.println(listt);
//            System.out.println(listt.size());

            return listt;
        }
        return makePath(listt, finall.getPrior());
    }

    public int adj(Point cur, Point next)
    {
        // distance from current node to adjacent node, x10 bc heuristic distance is squared (so its an int not double)
        int a = heuristicDistance(cur, next);
        return a;
    }

    public int heuristicDistance(Point cur, Point goal) //distance squared to get int
    {
        int xx = cur.x - goal.x;
        int yy = cur.y - goal.y;

        return (int) (Math.sqrt( (xx * xx) + (yy * yy))*10);

    }


    public class Node {
        private Point loc;
        private int g;
        private int h;
        private int f;
        private Node prior;

        public Node(Point loc, int g, int h, Node prior)
        {
            this.loc = loc;
            this.g = g;
            this.h = h;
            this.f = this.g + this.h;
            this.prior = prior;
        }
        public Point getLoc(){return loc;}
        public int getG() {return g;}
        public int getF() {return f;}
        public Node getPrior() {return prior;}
        }


}
