import processing.core.PImage;
import java.util.List;

public abstract class Dude extends Transformable{
    private int resourceCount;
    private final int resourceLimit;
    private PathingStrategy strategy = new AStarPathingStrategy();

    public Dude(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int resourceCount, int resourceLimit, int health){
        super(id, position, images, animationPeriod,actionPeriod, health);
        this.resourceCount = resourceCount;
        this.resourceLimit = resourceLimit;
    }

    public Point nextPositionDude(
            Entity d, WorldModel world, Point destPos) {
        int horiz = Integer.signum(destPos.x - d.getPosition().x);
        Point newPos = new Point(d.getPosition().x + horiz, d.getPosition().y);
        if (horiz == 0 || world.isOccupied(d, world, newPos)) {
            int vert = Integer.signum(destPos.y - d.getPosition().y);
            newPos = new Point(d.getPosition().x, d.getPosition().y + vert);

            if (vert == 0 || world.isOccupied(d, world, newPos)) {
                newPos = d.getPosition();
            }
        }
        List<Point> out = (strategy.computePath(d.getPosition(), destPos, p -> !world.isOccupied(d, world, p), (p1, p2) -> Point.neighbors(p1, p2),
                Point.CARDINAL_NEIGHBORS));
        return out.get(0);
    }

    public int getResourceCount(){
        return this.resourceCount;
    }

    public void setResourceCount(int i){
        this.resourceCount = i;
    }

    public int getResourceLimit(){
        return this.resourceLimit;
    }


}
