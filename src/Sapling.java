import processing.core.PImage;

import java.util.List;

public class Sapling extends TransformablePlant {
    public int healthLimit;

    public Sapling(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health, int healthLimit) {
        super(id, position, images, animationPeriod, actionPeriod, health);
        this.healthLimit = healthLimit;
    }

    public void scheduleActions(Entity s, EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(scheduler, s,
                new Activity(s, world, imageStore),
                this.getActionPeriod());
        scheduler.scheduleEvent(scheduler, s,
                new Animation(s, 0),
                this.getAnimationPeriod());
    }
    public boolean transform(Entity s, WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.getHealth() <= 0) {
            Stump stump = new Stump(s.getId(),
                    s.getPosition(),
                    imageStore.getImageList(imageStore, Functions.STUMP_KEY), 0);

            world.removeEntity(world, s);
            scheduler.unscheduleAllEvents(scheduler, s);

            world.addEntity(world, stump);

            return true;
        } else if (this.getHealth() >= this.healthLimit) { //turns sapling into tree
            Tree tree = new Tree("tree_" + s.getId(),
                    s.getPosition(), imageStore.getImageList(imageStore, Functions.TREE_KEY), Functions.getNumFromRange(Functions.TREE_ANIMATION_MAX, Functions.TREE_ANIMATION_MIN),
                    Functions.getNumFromRange(Functions.TREE_ACTION_MAX, Functions.TREE_ACTION_MIN),
                    Functions.getNumFromRange(Functions.TREE_HEALTH_MAX, Functions.TREE_HEALTH_MIN)
                    );

            world.removeEntity(world, s);
            scheduler.unscheduleAllEvents(scheduler, s);

            world.addEntity(world, tree);
            this.scheduleActions(tree, scheduler, world, imageStore);

            return true;
        }

        return false;
    }

    public void executeActivity(Entity t, WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        (t).setHealth(t.getHealth() + 1);
        if (!this.transformPlant(t, world, scheduler, imageStore)) {

            scheduler.scheduleEvent(scheduler, t,
                    new Activity(t, world, imageStore),
                    this.getActionPeriod());
        }
    }

    public boolean transformPlant(Entity s,
                                  WorldModel world,
                                  EventScheduler scheduler,
                                  ImageStore imageStore){
        return this.transform(s, world, scheduler, imageStore);
    }
    public String toString(){
        return "Sapling";
    }
}
