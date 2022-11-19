import processing.core.PImage;

import java.util.List;

public class House extends Entity{

    public House(String id, Point position, List<PImage> images, int health){
        super(id, position, images, health);
    }


    public String toString(){
        return "House";
    }
}
