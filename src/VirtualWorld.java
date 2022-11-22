import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Optional;

import processing.core.*;

public final class VirtualWorld extends PApplet
{
    public static final int TIMER_ACTION_PERIOD = 100;//tdest

    public static final int VIEW_WIDTH = 800;
    public static final int VIEW_HEIGHT = 576;
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 32;
    public static final double WORLD_WIDTH_SCALE = 2;
    public static final double WORLD_HEIGHT_SCALE = 2;

    public static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    public static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;
    public static final int WORLD_COLS = 25;
    public static final int WORLD_ROWS = 18;

//    public static final String IMAGE_LIST_FILE_NAME = "C:/Users/jason/IdeaProjects/CPE 203/Projects/Project_4/imagelist";
public static final String IMAGE_LIST_FILE_NAME = "imagelist";
//edits
    public static final String DEFAULT_IMAGE_NAME = "background_default";
    public static final int DEFAULT_IMAGE_COLOR = 0x808080;

//    public static String LOAD_FILE_NAME = "C:/Users/jason/IdeaProjects/CPE 203/Projects/Project_4/world.sav";
public static String LOAD_FILE_NAME = "world.sav";

    public static final String FAST_FLAG = "-fast";
    public static final String FASTER_FLAG = "-faster";
    public static final String FASTEST_FLAG = "-fastest";
    public static final double FAST_SCALE = 0.5;
    public static final double FASTER_SCALE = 0.25;
    public static final double FASTEST_SCALE = 0.10;

    public static double timeScale = 1;

    public ImageStore imageStore;
    public WorldModel world;
    public WorldView view;
    public EventScheduler scheduler;

    public long nextTime;
    public Player player;
    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        this.imageStore = new ImageStore(
                createImageColored(TILE_WIDTH, TILE_HEIGHT,
                                   DEFAULT_IMAGE_COLOR));
        this.world = new WorldModel(WORLD_ROWS, WORLD_COLS,
                                    createDefaultBackground(imageStore));
        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH,
                                  TILE_HEIGHT);
        this.scheduler = new EventScheduler(timeScale);

        loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);
        loadWorld(world, LOAD_FILE_NAME, imageStore);

        scheduleActions(world, scheduler, imageStore);

        nextTime = System.currentTimeMillis() + TIMER_ACTION_PERIOD;
        player = WorldModel.parsePlayer(world, imageStore);
    }

    public void draw() {
        long time = System.currentTimeMillis();
        if (time >= nextTime) {
            this.scheduler.updateOnTime(this.scheduler, time);
            nextTime = time + TIMER_ACTION_PERIOD;
        }

        WorldView.drawViewport(view);
    }

    // Just for debugging and for P5
    // Be sure to refactor this method as appropriate
    public void mousePressed() {
        Point pressed = mouseToPoint(mouseX, mouseY);

        System.out.println("CLICK! " + pressed.x + ", " + pressed.y);

        Optional<Entity> entityOptional = world.getOccupant(world, pressed);
        if (entityOptional.isPresent())
        {
            Entity entity = entityOptional.get();
            System.out.println(entity.getId() + ": " + entity.toString() + " : " + entity.getHealth());
        }

        if(onYeti(pressed) == true && onYeti(player.getPosition()) == true)
            {
                if (player.getResourceCount() <= player.getResourceLimit())
                {
                    System.out.println("Player is on Yeti AND Yeti has been clicked AND Player has enough snow");
                    //um set to above resourceLimit so this if statement will never be entered again. Otherwise it might be even after Yeti is removed.
                    player.setResourceCount(player.getResourceLimit() * 10);
                    System.out.println("[Also, Player has 10 SNOW! code to get rid of Yeti and bring in lights]");
                    world.removeEntityAt(world, new Point(9, 5));
                }

                //QUESTION: At (9,5) there is a house apparently?? How do we get rid of?

            }

    }
    public boolean onYeti(Point p)
    {
        if (p.x <15 && p.x > 8 && p.y < 11 && p.y > 5)
        {
            if (!p.equals(new Point (9,6)) && !p.equals(new Point (9,7)) && !p.equals(new Point (9,8))
                    && (!p.equals(new Point (14,6)) && !p.equals(new Point (14,7)) && !p.equals(new Point (14,8)) ))
            {
                return true;
            }
        }
        return false;
    }



    public void keyPressed() {
        if (key == CODED) {
            int dx = 0;
            int dy = 0;

            switch (keyCode) {
                case UP:
                    dy = -1;
                    break;
                case DOWN:
                    dy = 1;
                    break;
                case LEFT:
                    dx = -1;
                    break;
                case RIGHT:
                    dx = 1;
                    break;
            }
            player.setNewPoint(dy, dx, world, scheduler, imageStore);
        }
    }

    public static Background createDefaultBackground(ImageStore imageStore) {
        return new Background(DEFAULT_IMAGE_NAME,
                              imageStore.getImageList(imageStore,
                                                     DEFAULT_IMAGE_NAME));
    }

    public static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            img.pixels[i] = color;
        }
        img.updatePixels();
        return img;
    }

    static void loadImages(
            String filename, ImageStore imageStore, PApplet screen)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            imageStore.loadImages(in, imageStore, screen);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void loadWorld(
            WorldModel world, String filename, ImageStore imageStore)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            WorldModel.load(in, world, imageStore);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void scheduleActions(
            WorldModel world, EventScheduler scheduler, ImageStore imageStore)
    {
        for (Entity entity : world.entities) {
            if (entity.getClass() != House.class && entity.getClass() != Stump.class)
                ((Actionables)entity).scheduleActions(entity, scheduler, world, imageStore);
        }
    }

    private Point mouseToPoint(int x, int y)
    {
        return view.viewport.viewportToWorld(view.viewport, mouseX/TILE_WIDTH, mouseY/TILE_HEIGHT);
    }

    public static void parseCommandLine(String[] args) {
        if (args.length > 1)
        {
            if (args[0].equals("file"))
            {

            }
        }
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG:
                    timeScale = Math.min(FAST_SCALE, timeScale);
                    break;
                case FASTER_FLAG:
                    timeScale = Math.min(FASTER_SCALE, timeScale);
                    break;
                case FASTEST_FLAG:
                    timeScale = Math.min(FASTEST_SCALE, timeScale);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        PApplet.main(VirtualWorld.class);
    }
}
