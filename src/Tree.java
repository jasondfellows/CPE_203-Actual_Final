import processing.core.PImage;

import java.util.List;

public class Tree extends TransformablePlant{
    public Tree(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, animationPeriod, actionPeriod, health);
    }

    public void scheduleActions(Entity t, EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(scheduler, t,
                new Activity(t, world, imageStore),
                this.getActionPeriod());
        scheduler.scheduleEvent(scheduler, t,
                new Animation(t, 0),
                this.getAnimationPeriod());
    }
    public boolean transform(Entity t, WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        if (this.getHealth() >= 5) { //turns tree into snowy tree
            snowyTree st = new snowyTree("snowytree_" + t.getId(),
                    t.getPosition(), imageStore.getImageList(imageStore, Functions.SNOWY_TREE_KEY), Functions.getNumFromRange(Functions.TREE_ANIMATION_MAX, Functions.TREE_ANIMATION_MIN),
                    Functions.getNumFromRange(Functions.TREE_ACTION_MAX, Functions.TREE_ACTION_MIN),
                    Functions.getNumFromRange(Functions.TREE_HEALTH_MAX, Functions.TREE_HEALTH_MIN)
            );

            world.removeEntity(world, t);
            scheduler.unscheduleAllEvents(scheduler, t);

            world.addEntity(world, st);
            this.scheduleActions(st, scheduler, world, imageStore);

            return true;
        }
        return false;
    }
    public void executeActivity(Entity t, WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        t.setHealth(t.getHealth() + 1);
        if (!transformPlant(t, world, scheduler, imageStore)) {
            scheduler.scheduleEvent(scheduler, t,
                    new Activity(t, world, imageStore),
                    this.getActionPeriod());
        }
    }

    public boolean transformPlant(Entity t, WorldModel world, EventScheduler scheduler, ImageStore imageStore){
        return transform(t, world, scheduler, imageStore);
    }

    public String toString(){
        return "Tree";
    }
}
