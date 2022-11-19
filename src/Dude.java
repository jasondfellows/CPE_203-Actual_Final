import processing.core.PImage;
import java.util.List;

public abstract class Dude extends Transformable{
    private int resourceCount;
    private final int resourceLimit;
    private AStarPathing path;

    public Dude(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int resourceCount, int resourceLimit, int health){
        super(id, position, images, animationPeriod,actionPeriod, health);
        this.resourceCount = resourceCount;
        this.resourceLimit = resourceLimit;
    }

    public Point nextPositionDude(
            Entity d, WorldModel world, Point destPos) {
        int horiz = Integer.signum((int)destPos.x - (int)d.getPosition().x);
        Point newPos = new Point(d.getPosition().x + horiz, d.getPosition().y);
        if (horiz == 0 || world.isOccupied(world, newPos)&& world.getOccupancyCell(world, newPos).getClass() != Stump.class) {
            int vert = Integer.signum((int)destPos.y - (int)d.getPosition().y);
            newPos = new Point(d.getPosition().x, d.getPosition().y + vert);

            if (vert == 0 || world.isOccupied(world, newPos) && world.getOccupancyCell(world, newPos).getClass() != Stump.class) {
                newPos = d.getPosition();
            }
        }
        Point out = (AStarPathing.computePath(d.getPosition(), newPos, p ->  WorldModel.withinBounds(world, p)
                        && (!world.isOccupied(world, p) || world.getOccupancyCell(world, p).getClass() == Stump.class),
                        Point.CARDINAL_NEIGHBORS)).get(0);
        return out;
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
    public abstract boolean moveTo(Entity dude, WorldModel world, Entity target, EventScheduler scheduler);

}
