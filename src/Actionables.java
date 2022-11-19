import processing.core.PImage;
import java.util.List;
import java.util.Optional;
public abstract class Actionables extends Entity{
    private final int animationPeriod;
    private final int actionPeriod;
    public Actionables(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, health);
        this.animationPeriod = animationPeriod;
        this.actionPeriod = actionPeriod;
    }

    public abstract void scheduleActions(Entity entity, EventScheduler scheduler, WorldModel world, ImageStore imageStore);
    public int getAnimationPeriod(){
        return this.animationPeriod;
    };

    public int getActionPeriod(){
        return this.actionPeriod;
    }
}
