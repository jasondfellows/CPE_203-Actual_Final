import processing.core.PImage;

import java.util.*;

/**
 * Represents the 2D World in which this simulation is running.
 * Keeps track of the size of the world, the background image for each
 * location in the world, and the entities that populate the world.
 */
public final class WorldModel
{
    public int numRows;
    public int numCols;
    private Background background[][];
    private Entity occupancy[][];
    public Set<Entity> entities;

    public WorldModel(int numRows, int numCols, Background defaultBackground) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.background = new Background[numRows][numCols];
        this.occupancy = new Entity[numRows][numCols];
        this.entities = new HashSet<>();

        for (int row = 0; row < numRows; row++) {
            Arrays.fill(this.background[row], defaultBackground);
        }
    }
    public void setBackgroundCell(
            WorldModel world, Point pos, Background background)
    {
        world.background[(int)pos.y][(int)pos.x] = background;
    }

    public Background getBackgroundCell(WorldModel world, Point pos) {
        return world.background[(int)pos.y][(int)pos.x];
    }

    public void setOccupancyCell(
            WorldModel world, Point pos, Entity entity)
    {
        world.occupancy[(int)pos.y][(int)pos.x] = entity;
    }

    public Entity getOccupancyCell(WorldModel world, Point pos) {
        return world.occupancy[(int)pos.y][(int)pos.x];
    }

    public Optional<Entity> getOccupant(WorldModel world, Point pos) {
        if (world.isOccupied(world, pos)) {
            return Optional.of(world.getOccupancyCell(world, pos));
        }
        else {
            return Optional.empty();
        }
    }
    public void tryAddEntity(WorldModel world, Entity entity) {
        if (world.isOccupied(world, entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        this.addEntity(world, entity);
    }

    public Optional<Entity> findNearest(
            WorldModel world, Point pos, List<Class> kinds)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Class kind: kinds)
        {
            for (Entity entity : world.entities) {
                if (entity.getClass() == kind) {
                    ofType.add(entity);
                }
            }
        }

        return this.nearestEntity(ofType, pos);
    }
    public Optional<Entity> nearestEntity(
            List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = (int)pos.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities) {
                int otherDistance = (int)pos.distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    public void addEntity(WorldModel world, Entity entity) {
        if (this.withinBounds(world, entity.getPosition())) {
            world.setOccupancyCell(world, entity.getPosition(), entity);
            world.entities.add(entity);
        }
    }

    public void removeEntityAt(WorldModel world, Point pos) {
        if (world.withinBounds(world, pos) && world.getOccupancyCell(world, pos) != null) {
            Entity entity = world.getOccupancyCell(world, pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            world.entities.remove(entity);
            world.setOccupancyCell(world, pos, null);
        }
    }

    public void removeEntity(WorldModel world, Entity entity) {
        this.removeEntityAt(world, entity.getPosition());
    }

    public void moveEntity(WorldModel world, Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (world.withinBounds(world, pos) && !pos.equals(oldPos)) {
            world.setOccupancyCell(world, oldPos, null);
            this.removeEntityAt(world, pos);
            world.setOccupancyCell(world, pos, entity);
            entity.setPosition(pos);
        }
    }
    public void setBackground(
            WorldModel world, Point pos, Background background)
    {
        if (this.withinBounds(world, pos)) {
            world.setBackgroundCell(world, pos, background);
        }
    }

    public static boolean parseHouse(
            String[] properties, WorldModel world, ImageStore imageStore, String key)
    {
        if (properties.length == Functions.HOUSE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.HOUSE_COL]),
                    Integer.parseInt(properties[Functions.HOUSE_ROW]));
            House entity = new House(properties[Functions.HOUSE_ID], pt,
                    imageStore.getImageList(imageStore,
                            key), 0);
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.HOUSE_NUM_PROPERTIES;
    }

    public static boolean parseBackground(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.BGND_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.BGND_COL]),
                    Integer.parseInt(properties[Functions.BGND_ROW]));
            String id = properties[Functions.BGND_ID];
            world.setBackground(world, pt,
                    new Background(id, imageStore.getImageList(imageStore, id)));
        }

        return properties.length == Functions.BGND_NUM_PROPERTIES;
    }

    public static boolean parseSapling(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.SAPLING_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.SAPLING_COL]),
                    Integer.parseInt(properties[Functions.SAPLING_ROW]));
            String id = properties[Functions.SAPLING_ID];
            int health = Integer.parseInt(properties[Functions.SAPLING_HEALTH]);
            Entity entity = new Sapling(id, pt, imageStore.getImageList(imageStore, Functions.SAPLING_KEY),
                    Functions.SAPLING_ACTION_ANIMATION_PERIOD, Functions.SAPLING_ACTION_ANIMATION_PERIOD, health, Functions.SAPLING_HEALTH_LIMIT);
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.SAPLING_NUM_PROPERTIES;
    }

    public static Player parsePlayer(
            WorldModel world, ImageStore imageStore)
    {
         Point pt = new Point(0, 0);
            Player entity = new Player(Functions.DUDE_KEY,
                    pt, imageStore.getImageList(imageStore, Functions.DUDE_KEY), Functions.PLAYER_ANIMATION_PERIOD,
                    Functions.DUDE_ACTION_PERIOD, 0,
                    Functions.DUDE_LIMIT, 0
            );
            world.addEntity(world, entity);

        return entity;
    }
    public static boolean parseDude(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.DUDE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.DUDE_COL]),
                    Integer.parseInt(properties[Functions.DUDE_ROW]));
            DudeNotFull entity = new DudeNotFull(properties[Functions.DUDE_ID],
                    pt, imageStore.getImageList(imageStore, Functions.DUDE_KEY), Integer.parseInt(properties[Functions.DUDE_ANIMATION_PERIOD]),
                    Integer.parseInt(properties[Functions.DUDE_ACTION_PERIOD]), 0,
                    Integer.parseInt(properties[Functions.DUDE_LIMIT]), 0
                    );
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.DUDE_NUM_PROPERTIES;
    }

    public static boolean parseFairy(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.FAIRY_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.FAIRY_COL]),
                    Integer.parseInt(properties[Functions.FAIRY_ROW]));
            Fairy entity = new Fairy(properties[Functions.FAIRY_ID],
                    pt,  imageStore.getImageList(imageStore, Functions.FAIRY_KEY),
                    Integer.parseInt(properties[Functions.FAIRY_ANIMATION_PERIOD]),
                    Integer.parseInt(properties[Functions.FAIRY_ACTION_PERIOD]), 0
                   );
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.FAIRY_NUM_PROPERTIES;
    }

    public static boolean parseTree(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.TREE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.TREE_COL]),
                    Integer.parseInt(properties[Functions.TREE_ROW]));
            snowyTree entity = new snowyTree(properties[Functions.TREE_ID],
                    pt, imageStore.getImageList(imageStore, Functions.SNOWY_TREE_KEY),
                    Integer.parseInt(properties[Functions.TREE_ANIMATION_PERIOD]),
                    Integer.parseInt(properties[Functions.TREE_ACTION_PERIOD]),
                    Integer.parseInt(properties[Functions.TREE_HEALTH])
                    );
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.TREE_NUM_PROPERTIES;
    }

    public static boolean parseObstacle(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.OBSTACLE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.OBSTACLE_COL]),
                    Integer.parseInt(properties[Functions.OBSTACLE_ROW]));
            Obstacle entity = new Obstacle(properties[Functions.OBSTACLE_ID], pt, imageStore.getImageList(imageStore,
                    Functions.OBSTACLE_KEY),
                    Integer.parseInt(properties[Functions.OBSTACLE_ANIMATION_PERIOD]), 0, 0);
            world.tryAddEntity(world, entity);
        }

        return properties.length == Functions.OBSTACLE_NUM_PROPERTIES;
    }

    public static boolean parseYETI(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.YETI_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.YETI_COL]),
                    Integer.parseInt(properties[Functions.YETI_ROW]));

            House entity = new House(properties[Functions.YETI_ID], pt,
                    imageStore.getImageList(imageStore, Functions.YETI_1KEY), 0);
            world.tryAddEntity(world, entity);
            System.out.println("add yeti");
        }

        return properties.length == Functions.HOUSE_NUM_PROPERTIES;
    }

    public static boolean withinBounds(WorldModel world, Point pos) {
        return pos.y >= 0 && pos.y < world.numRows && pos.x >= 0
                && pos.x < world.numCols;
    }

    public static boolean processLine(
            String line, WorldModel world, ImageStore imageStore)
    {
        String[] properties = line.split("\\s");
        if (properties.length > 0) {
            String key = properties[0];
            switch (properties[Functions.PROPERTY_KEY]) {
                case Functions.BGND_KEY:
                    return parseBackground(properties, world, imageStore);
                case Functions.DUDE_KEY:
                    return true;
                    //return parseDude(properties, world, imageStore);
                case Functions.OBSTACLE_KEY:
                    return parseObstacle(properties, world, imageStore);
                case Functions.FAIRY_KEY:
                    return parseFairy(properties, world, imageStore);
                case Functions.HOUSE_1KEY, Functions.HOUSE_2KEY, Functions.HOUSE_3KEY, Functions.HOUSE_4KEY, Functions.HOUSE_5KEY, Functions.HOUSE_6KEY, Functions.HOUSE_7KEY, Functions.HOUSE_8KEY, Functions.HOUSE_9KEY, Functions.HOUSE_10KEY, Functions.HOUSE_11KEY, Functions.HOUSE_12KEY:
                    return parseHouse(properties, world, imageStore, key);
                case Functions.SNOWY_TREE_KEY:
                    return parseTree(properties, world, imageStore);
                case Functions.SAPLING_KEY:
                    return parseSapling(properties, world, imageStore);
                case Functions.YETI_1KEY:
                    return parseYETI(properties, world, imageStore);
            }
        }

        return false;
    }

    public static void load(
            Scanner in, WorldModel world, ImageStore imageStore)
    {
        int lineNumber = 0;
        while (in.hasNextLine()) {
            try {
                if (!processLine(in.nextLine(), world, imageStore)) {
                    System.err.println(String.format("invalid entry on line %d",
                            lineNumber));
                }
            }
            catch (NumberFormatException e) {
                System.err.println(
                        String.format("invalid entry on line %d", lineNumber));
            }
            catch (IllegalArgumentException e) {
                System.err.println(
                        String.format("issue on line %d: %s", lineNumber,
                                e.getMessage()));
            }
            lineNumber++;
        }
    }
    public boolean isOccupied(WorldModel world, Point pos) {
        return this.withinBounds(world, pos) && world.getOccupancyCell(world, pos) != null;
    }

}
