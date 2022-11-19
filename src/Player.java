import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Player extends Transformable{
    private int resourceCount;
    private final int resourceLimit;
    private AStarPathing path;

    public Player(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int resourceCount, int resourceLimit, int health){
        super(id, position, images, animationPeriod,actionPeriod, health);
        this.resourceCount = resourceCount;
        this.resourceLimit = resourceLimit;
    }

    public Point nextPositionPlayer(
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

    public void executeActivity(
            Entity d,
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                world.findNearest(world, d.getPosition(), new ArrayList<>(Arrays.asList(House.class)));

        if (fullTarget.isPresent() && this.moveTo(d, world,
                fullTarget.get(), scheduler))
        {
            this.transform(d, world, scheduler, imageStore);
        }
        else {
            scheduler.scheduleEvent(scheduler, d,
                    new Activity(d, world, imageStore),
                    this.getActionPeriod());
        }
    }
    public void setNewPoint(int dy, int dx, WorldModel world, EventScheduler scheduler, ImageStore imagestore){
        Point testPos = new Point(this.position.x + dx, this.position.y + dy);
        if (withinBoundsPlayer(testPos)) {
            if (world.getOccupancyCell(world, testPos) == null || world.getOccupancyCell(world, testPos).getClass() != House.class) {//null means background tile
                this.position.x += dx;
                this.position.y += dy;
                nextImage(this);
            }
            if (world.getOccupancyCell(world, testPos) != null && world.getOccupancyCell(world, testPos).getClass() == snowyTree.class){
                ((snowyTree)(world.getOccupancyCell(world, testPos))).transform(world.getOccupancyCell(world, testPos), world, scheduler, imagestore);
            }
        }
    }

    public static boolean withinBoundsPlayer(Point pos) {
        return pos.y >= 0 && pos.y < VirtualWorld.WORLD_ROWS && pos.x >= 0
                && pos.x < VirtualWorld.WORLD_COLS;
    }
    public boolean transform(
            Entity d,
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        Player miner = new Player(d.getId(),
                d.getPosition(), d.getImages(), this.getAnimationPeriod(),
                this.getActionPeriod(), 0,
                this.getResourceLimit(), 0);

        world.removeEntity(world, d);
        scheduler.unscheduleAllEvents(scheduler, d);

        world.addEntity(world, miner);
        this.scheduleActions(miner, scheduler, world, imageStore);
        return true;
    }

    public void scheduleActions(
            Entity d,
            EventScheduler scheduler,
            WorldModel world,
            ImageStore imageStore)
    {
        scheduler.scheduleEvent(scheduler, d,
                new Activity(d, world, imageStore),
                this.getActionPeriod());
        scheduler.scheduleEvent(scheduler, d,
                new Animation(d, 0),
                this.getAnimationPeriod());
    }

    public boolean moveTo(Entity dude, WorldModel world, Entity target, EventScheduler scheduler)
    {
        if (target.getPosition().adjacent(dude.getPosition(), target.getPosition())) {
            return true;
        }
        else {
            Point nextPos = nextPositionPlayer(dude, world, target.getPosition());

            if (!dude.getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(world, nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(scheduler, occupant.get());
                }

                world.moveEntity(world, dude, nextPos);
            }
            return false;
        }
    }

//    public boolean transform(Entity t, WorldModel world, EventScheduler scheduler, ImageStore imageStore)
//    {
//        if (((Tree)t).getHealth() <= 0) {
//            Stump stump = new Stump(t.getId(),
//                    t.getPosition(), imageStore.getImageList(imageStore, Functions.STUMP_KEY), 0);
//
//            world.removeEntity(world, t);
//            scheduler.unscheduleAllEvents(scheduler, t);
//
//            world.addEntity(world, stump);
//
//            return true;
//        }
//        return false;
//    }

//    public boolean transform(
//            Entity d,
//            WorldModel world,
//            EventScheduler scheduler,
//            ImageStore imageStore)
//    {
//        DudeNotFull miner = new DudeNotFull(d.getId(),
//                d.getPosition(), d.getImages(), this.getAnimationPeriod(),
//                this.getActionPeriod(), 0,
//                this.getResourceLimit(), 0);
//
//        world.removeEntity(world, d);
//        scheduler.unscheduleAllEvents(scheduler, d);
//
//        world.addEntity(world, miner);
//        this.scheduleActions(miner, scheduler, world, imageStore);
//        return true;
//    }
    public String toString(){
        return "Dude_Full";
    }

}

//    public boolean transform(Entity t, WorldModel world, EventScheduler scheduler, ImageStore imageStore)
//    {
//
//        snowyTree st = new snowyTree(t.getId(),
//                t.getPosition(), imageStore.getImageList(imageStore, Functions.SNOWY_TREE_KEY), 0, 0, 1);
//
//        world.removeEntity(world, t);
//        scheduler.unscheduleAllEvents(scheduler, t);
//
//        world.addEntity(world, st);
//
//        return true;
//
//    }
