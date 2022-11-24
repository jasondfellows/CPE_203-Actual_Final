import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DudeFull extends Dude {
    public DudeFull(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int resourceCount, int resourceLimit, int health) {
        super(id, position, images, animationPeriod, actionPeriod, resourceCount, resourceLimit, health);
    }

    public void executeActivity(
            Entity d,
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler)
    {
        Optional<Entity> fullTarget =
                world.findNearest(world, d.getPosition(), new ArrayList<>(Arrays.asList(snowyTree.class)));
        if (fullTarget.isEmpty()){
            ((DudeFull)d).transform(d, world, scheduler, imageStore);
        }

        if (fullTarget.isPresent() && this.moveTo(d, world,
                fullTarget.get(), scheduler))
        {
            this.transform(d, world, scheduler, imageStore);
            fullTarget.get().setHealth(1);
            ((snowyTree)fullTarget.get()).transform(fullTarget.get(), world, scheduler, imageStore);
        }
        else {
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
        DudeNotFull miner = new DudeNotFull(d.getId(),
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
            Point nextPos = nextPositionDude(dude, world, target.getPosition());

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

    public String toString(){
        return "Dude_Full";
    }
}
