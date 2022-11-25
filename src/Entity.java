import java.util.*;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public abstract class Entity
{
    private String id;
    Point position;
    private List<PImage> images;
    private int imageIndex;
    private int health;


    public Entity(
            String id,
            Point position,
            List<PImage> images, int health)
    {
        this.id = id;
        this.position = position;
        this.images = images;
        this.health = health;
        this.imageIndex = 0;
    }
    public abstract String toString();

    public int getImageIndex(){
        return this.imageIndex;
    }
    public List<PImage> getImages(){
        return this.images;
    }
    public void setImages(List<PImage> z) {this.images = z;}

    public String getId(){
        return this.id;
    }

    public Point getPosition(){
        return this.position;
    }

    public int getHealth(){
        return this.health;
    }

    public void setHealth(int h){
        this.health = h;
    }

    public void setPosition(Point p){
        this.position = p;
    }
    public void nextImage(Entity entity) {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public static Optional<PImage> getBackgroundImage(
            WorldModel world, Point pos)
    {
        if (world.withinBounds(world, pos)) {
            return Optional.of(Background.getCurrentImage(world.getBackgroundCell(world, pos)));
        }
        else {
            return Optional.empty();
        }
    }

}
