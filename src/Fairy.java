import processing.core.PImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Fairy extends Actionables{
    public Fairy(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, animationPeriod, actionPeriod, health);
    }


    public void scheduleActions(Entity f, EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(scheduler, f,
                new Activity(f, world, imageStore),
                this.getActionPeriod());
        scheduler.scheduleEvent(scheduler, f,
                new Animation(f, 0),
                this.getAnimationPeriod());
    }
    public boolean moveToFairy(
            Entity f,
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (target.getPosition().equals(f.getPosition())) {
            //reset snow count
            if (((Player)target).getResourceCount() > 0) {
                ((Player) target).setResourceCount(((Player)target).getResourceCount()/2);
            }
//            world.removeEntity(world, target);
//            scheduler.unscheduleAllEvents(scheduler, target);
            return true;
        }
        else {
            Point nextPos = this.nextPositionFairy(f, world, target.getPosition());

            if (!this.getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(f, world, nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(scheduler, occupant.get());
                }

                world.moveEntity(world, f, nextPos);
            }
            return false;
        }
    }
    public Point nextPositionFairy(
            Entity f, WorldModel world, Point destPos)
    {
        int horiz = Integer.signum((destPos.x - f.getPosition().x));
        Point newPos = new Point(f.getPosition().x + horiz, f.getPosition().y);

        if (horiz == 0 || world.isOccupied(f, world, newPos)) {
            int vert = Integer.signum(destPos.y - f.getPosition().y);
            newPos = new Point(f.getPosition().x, f.getPosition().y + vert);

            if (vert == 0 || world.isOccupied(f, world, newPos)) {
                newPos = f.getPosition();
            }
        }

        return newPos;
    }
    public void executeFairyActivity(
            Entity f,
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler)
    {
        Optional<Entity> fairyTarget =
                world.findNearest(world, f.getPosition(), new ArrayList<>(Arrays.asList(Player.class)));

        if (fairyTarget.isPresent()) {
            this.moveToFairy(f, world, fairyTarget.get(), scheduler);
        }

        scheduler.scheduleEvent(scheduler, f,
                new Activity(f, world, imageStore),
                this.getActionPeriod());
    }

    public String toString(){
        return "Fairy";
    }

    public boolean transformFairy(
            Entity d,
            WorldModel world,
            EventScheduler scheduler,
            ImageStore imageStore)
    {
            DudeNotFull miner = new DudeNotFull("happySnowman",
                    d.getPosition(), imageStore.getImageList(imageStore, "happySnowman"), Functions.DUDE_ANIMATION_PERIOD,
                    Functions.DUDE_ACTION_PERIOD, 0,
                    1, 0);

            world.removeEntity(world, d);
            scheduler.unscheduleAllEvents(scheduler, d);

            world.addEntity(world, miner);
            this.scheduleActions(miner, scheduler, world, imageStore);

            return true;

    }

}
