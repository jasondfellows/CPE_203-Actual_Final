import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DudeNotFull extends Dude{
    public DudeNotFull(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int resourceCount, int resourceLimit, int health){
        super(id, position, images, animationPeriod,actionPeriod, resourceCount, resourceLimit, health);
    }
    public void executeActivity(
            Entity d,
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler)
    {
        Point target =
                world.findNearestBackground(world, d.getPosition());
        if (target == null){
            target = new Point(3, 17);
            if (d.getPosition().adjacent(d.getPosition(), target)){
                world.removeEntity(world, d);
                scheduler.unscheduleAllEvents(scheduler, d);
                world.transformIgloo(world, imageStore);
                world.numInHouse ++;
            }
            if (world.numInHouse == 4){
                world.LightAll(world, imageStore, scheduler);
                world.numInHouse ++;
                VirtualWorld.done = true;
            }
        }

        if (!moveTo(d, world,
                target,
                scheduler, imageStore)
                || !transform(d, world, scheduler, imageStore))
        {
            scheduler.scheduleEvent(scheduler, d,
                    new Activity(d, world, imageStore),
                    this.getActionPeriod());
        }
    }

    public boolean transform(
            Entity d,
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
        if (this.getResourceCount() >= 0) {
            DudeFull miner = new DudeFull(d.getId(),
                    d.getPosition(), d.getImages(), this.getAnimationPeriod(),
                    this.getActionPeriod(), 0,
                    this.getResourceLimit(), 0);

            world.removeEntity(world, d);
            scheduler.unscheduleAllEvents(scheduler, d);

            world.addEntity(world, miner);
            this.scheduleActions(miner, scheduler, world, imageStore);

            return true;
        }

        return false;
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

    public boolean moveTo(
            Entity dude,
            WorldModel world,
            Point target,
            EventScheduler scheduler, ImageStore imageStore)
    {
        if (target.adjacent(dude.getPosition(), target)) {
            this.setResourceCount(this.getResourceCount() + 1);
            world.setBackgroundCell(world, target, new Background("snowreg2", imageStore.getImageList(imageStore, "snowreg2")));
            transform(dude, world, scheduler, imageStore);
            return true;
        }
        else {
            Point nextPos = nextPositionDude(dude, world, target);

            if (!dude.getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(dude, world, nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(scheduler, occupant.get());
                }

                world.moveEntity(world, dude, nextPos);
            }
            return false;
        }
    }

    public String toString(){
        return "Dude_Not_Full";
    }
}
