import processing.core.PImage;
import java.util.List;
public abstract class Transformable extends Actionables{


    public Transformable(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, animationPeriod, actionPeriod, health);
    }
    public abstract boolean transform(Entity entity, WorldModel world, EventScheduler scheduler, ImageStore imageStore);
    public abstract void executeActivity(Entity entity, WorldModel world, ImageStore imageStore, EventScheduler scheduler);
}
