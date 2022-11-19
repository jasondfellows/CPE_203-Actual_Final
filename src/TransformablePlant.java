import processing.core.PImage;

import java.util.List;

public abstract class TransformablePlant extends Transformable{
    public TransformablePlant(String id, Point position, List<PImage> images, int animationPeriod, int actionPeriod, int health){
        super(id, position, images, animationPeriod, actionPeriod, health);
    }
    public abstract boolean transformPlant(Entity t, WorldModel world, EventScheduler scheduler, ImageStore imageStore);
}
