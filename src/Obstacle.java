import processing.core.PImage;

import java.util.List;

public class Obstacle extends Actionables{
    public Obstacle(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, animationPeriod, actionPeriod, health);
    }


    public void scheduleActions(Entity o, EventScheduler scheduler, WorldModel world, ImageStore imageStore){
        scheduler.scheduleEvent(scheduler, o,
                new Animation(o, 0),
                this.getAnimationPeriod());
    }

    public String toString(){
        return "Obstacle";
    }
}
