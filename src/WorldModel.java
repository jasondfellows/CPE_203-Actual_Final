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
    public int numInHouse;
    public LinkedList<UnlitTree> UnlitTrees;
    public static LinkedList<Fairy> EvilSnowmen;
    public static Random speedPicker;
    public static int numSnowmen = 4;
    public WorldModel(int numRows, int numCols, Background defaultBackground) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.background = new Background[numRows][numCols];
        this.occupancy = new Entity[numRows][numCols];
        this.entities = new HashSet<>();
        this.numInHouse = 0;
        this.UnlitTrees = new LinkedList<>();
        this.EvilSnowmen = new LinkedList<>();
        this.speedPicker = new Random();

        for (int row = 0; row < numRows; row++) {
            Arrays.fill(this.background[row], defaultBackground);
        }
    }
    public void setBackgroundCell(
            WorldModel world, Point pos, Background background)
    {
        world.background[pos.y][pos.x] = background;
    }

    public Background getBackgroundCell(WorldModel world, Point pos) {
        return world.background[pos.y][pos.x];
    }

    public void setOccupancyCell(
            WorldModel world, Point pos, Entity entity)
    {
        world.occupancy[pos.y][pos.x] = entity;
    }

    public Entity getOccupancyCell(WorldModel world, Point pos) {
        return world.occupancy[pos.y][pos.x];
    }

    public Optional<Entity> getOccupant(Entity entity, WorldModel world, Point pos) {
        if (world.isOccupied(entity, world, pos)) {
            return Optional.of(world.getOccupancyCell(world, pos));
        }
        else {
            return Optional.empty();
        }
    }
    public void tryAddEntity(WorldModel world, Entity entity) {
        if (world.isOccupied(entity, world, entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
//            throw new IllegalArgumentException("position occupied");
            return;
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
    public Point findNearestBackground(
            WorldModel world, Point pos) {
        List<Point> bgs = new LinkedList<>();
        int x = 0;
        while (x < 24){
            int y = 0;
            while (y < 17) {
                if (world.background[y][x].getId().equals("litsnow")) {
                    bgs.add(new Point(x, y));
                }
                y ++;
            }
            x ++;
        }
        return this.findClosestTile(bgs, pos);
    }
    public Point findClosestTile(List<Point> points, Point pos){
        double minDist = 1000;
        Point minPt = null;
        for (Point p : points){
            if (Point.distance(p, pos) < minDist){
                minDist = Point.distance(p, pos);
                minPt = p;
            }
        }
        return minPt;
    }
    public Optional<Entity> nearestTile(
            List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = pos.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities) {
                int otherDistance = pos.distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }
    public Optional<Entity> nearestEntity(
            List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        else {
            Entity nearest = entities.get(0);
            int nearestDistance = pos.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities) {
                int otherDistance = pos.distanceSquared(other.getPosition(), pos);

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
            if (entity.getClass() == UnlitTree.class){
                this.UnlitTrees.add(((UnlitTree)entity));
            }
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
            String[] properties, WorldModel world, ImageStore imageStore, Point pt, EventScheduler scheduler)
    {
//        DudeNotFull entity = new DudeNotFull("dude",
//                pt, imageStore.getImageList(imageStore, Functions.DUDE_KEY), Functions.DUDE_ANIMATION_PERIOD,
//                Functions.DUDE_ACTION_PERIOD, 1,
//                Functions.DUDE_LIMIT, 0
//        );
        DudeNotFull entity = new DudeNotFull("reindeer",
                pt, imageStore.getImageList(imageStore, "reindeer"), Functions.DUDE_ANIMATION_PERIOD,
                Functions.DUDE_ACTION_PERIOD, 1,
                Functions.DUDE_LIMIT, 0
        );
        world.tryAddEntity(world, entity);
        scheduler.scheduleEvent(scheduler, entity, new Activity(entity, world, imageStore),
                entity.getActionPeriod());


        return properties.length == Functions.DUDE_NUM_PROPERTIES;
    }

    public static boolean parseFairy(
            String[] properties, WorldModel world, ImageStore imageStore)
    {
        if (properties.length == Functions.FAIRY_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.FAIRY_COL]),
                    Integer.parseInt(properties[Functions.FAIRY_ROW]));
            Fairy entity = new Fairy("evilSnowman",
                    pt,  imageStore.getImageList(imageStore, Functions.EVIL_SNOWMAN_KEY),
                    Integer.parseInt(properties[Functions.FAIRY_ANIMATION_PERIOD]),
                    speedPicker.nextInt(110, 125), 0
                   );
            EvilSnowmen.add(entity);
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
                    0
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
                case Functions.EVIL_SNOWMAN_KEY:
                    return parseFairy(properties, world, imageStore);
                case Functions.HOUSE_1KEY, Functions.HOUSE_2KEY, Functions.HOUSE_3KEY, Functions.HOUSE_4KEY, Functions.HOUSE_5KEY, Functions.HOUSE_6KEY, Functions.HOUSE_7KEY, Functions.HOUSE_8KEY, Functions.HOUSE_9KEY, Functions.HOUSE_10KEY, Functions.HOUSE_11KEY, Functions.HOUSE_12KEY:
                    return true;
                case Functions.SNOWY_TREE_KEY:
                    return parseTree(properties, world, imageStore);
                case Functions.SAPLING_KEY:
                    return parseSapling(properties, world, imageStore);
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
    public boolean isOccupied(Entity entity, WorldModel world, Point pos) {
        if (entity.getClass() == Fairy.class){
            if (world.getBackgroundCell(world, pos).getId().startsWith("yeti")){
                return true;
            }
            else if (world.getOccupancyCell(world, pos) != null){
                return world.getOccupancyCell(world, pos).getClass() != Player.class;
            }
        }
        if (this.withinBounds(world, pos)) {
            if (world.getBackgroundCell(world, pos).getId().startsWith("house") || (world.getBackgroundCell(world, pos).getId().startsWith("unliti"))){
                return true;
            }
            else if (world.getOccupancyCell(world, pos) != null){
                return true;
            }
        }
        return false;
    }
    public void worldEvent(WorldModel world, EventScheduler scheduler, ImageStore images, Point pos){
        int i = 1;
        for (Fairy s : EvilSnowmen){
            DudeNotFull d = s.transformFairy(s, world, scheduler, images);
            if (Point.distance(d.getPosition(), pos) > 4){
                scheduler.unscheduleAllEvents(scheduler, d);
                numSnowmen --;
            }
        }
        setBackgroundCell(world, pos, new Background("litsnow",images.getImageList(images, "litsnow")));
        while (i < 5){
            setBackgroundCell(world, new Point(pos.x, pos.y - i), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x, pos.y + i), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x - i, pos.y), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x + i, pos.y), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x + i, pos.y -i), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x + i, pos.y + i), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x - i, pos.y - i), new Background("litsnow",images.getImageList(images, "litsnow")));
            setBackgroundCell(world, new Point(pos.x - i, pos.y + i), new Background("litsnow",images.getImageList(images, "litsnow")));
            i += 1;
        }
    }
    public void transformIgloo(WorldModel world, ImageStore images){
        setBackgroundCell(world, new Point(0, 15), new Background("unlitigloo1",images.getImageList(images, "unlitigloo1")));
        setBackgroundCell(world, new Point(1, 15), new Background("unlitigloo2",images.getImageList(images, "unlitigloo2")));
        setBackgroundCell(world, new Point(2, 15), new Background("unlitigloo3",images.getImageList(images, "unlitigloo3")));
        setBackgroundCell(world, new Point(3, 15), new Background("unlitigloo4",images.getImageList(images, "unlitigloo4")));
        setBackgroundCell(world, new Point(0, 16), new Background("unlitigloo5",images.getImageList(images, "unlitigloo5")));
        setBackgroundCell(world, new Point(1, 16), new Background("unlitigloo6",images.getImageList(images, "unlitigloo6")));
        setBackgroundCell(world, new Point(2, 16), new Background("unlitigloo7",images.getImageList(images, "unlitigloo7")));
        setBackgroundCell(world, new Point(3, 16), new Background("unlitigloo8",images.getImageList(images, "unlitigloo8")));
        setBackgroundCell(world, new Point(0, 17), new Background("unlitigloo9",images.getImageList(images, "unlitigloo9")));
        setBackgroundCell(world, new Point(1, 17), new Background("unlitigloo10",images.getImageList(images, "unlitigloo10")));
        setBackgroundCell(world, new Point(2, 17), new Background("unlitigloo11",images.getImageList(images, "unlitigloo11")));
        setBackgroundCell(world, new Point(3, 17), new Background("unlitigloo12",images.getImageList(images, "unlitigloo12")));
    }

    public void LightAll(WorldModel world, ImageStore images, EventScheduler scheduler){
        setBackgroundCell(world, new Point(0, 15), new Background("igloo1",images.getImageList(images, "igloo1")));
        setBackgroundCell(world, new Point(1, 15), new Background("igloo2",images.getImageList(images, "igloo2")));
        setBackgroundCell(world, new Point(2, 15), new Background("igloo3",images.getImageList(images, "igloo3")));
        setBackgroundCell(world, new Point(3, 15), new Background("igloo4",images.getImageList(images, "igloo4")));
        setBackgroundCell(world, new Point(0, 16), new Background("igloo5",images.getImageList(images, "igloo5")));
        setBackgroundCell(world, new Point(1, 16), new Background("igloo6",images.getImageList(images, "igloo6")));
        setBackgroundCell(world, new Point(2, 16), new Background("igloo7",images.getImageList(images, "igloo7")));
        setBackgroundCell(world, new Point(3, 16), new Background("igloo8",images.getImageList(images, "igloo8")));
        setBackgroundCell(world, new Point(0, 17), new Background("igloo9",images.getImageList(images, "igloo9")));
        setBackgroundCell(world, new Point(1, 17), new Background("igloo10",images.getImageList(images, "igloo10")));
        setBackgroundCell(world, new Point(2, 17), new Background("igloo11",images.getImageList(images, "igloo11")));
        setBackgroundCell(world, new Point(3, 17), new Background("igloo12",images.getImageList(images, "igloo12")));

        for (UnlitTree t : this.UnlitTrees){
            t.transform(t, world, scheduler, images);
        }
        setBackgroundCell(world, new Point(10, 8), new Background("redH",images.getImageList(images, "redH")));
        setBackgroundCell(world, new Point(11, 8), new Background("greenA",images.getImageList(images, "greenA")));
        setBackgroundCell(world, new Point(12, 8), new Background("redP",images.getImageList(images, "redP")));
        setBackgroundCell(world, new Point(13, 8), new Background("greenP",images.getImageList(images, "greenP")));
        setBackgroundCell(world, new Point(14, 8), new Background("redY",images.getImageList(images, "redY")));
        setBackgroundCell(world, new Point(8, 10), new Background("greenH",images.getImageList(images, "greenH")));
        setBackgroundCell(world, new Point(9, 10), new Background("redO",images.getImageList(images, "redO")));
        setBackgroundCell(world, new Point(10, 10), new Background("greenL",images.getImageList(images, "greenL")));
        setBackgroundCell(world, new Point(11, 10), new Background("redI",images.getImageList(images, "redI")));
        setBackgroundCell(world, new Point(12, 10), new Background("greenD",images.getImageList(images, "greenD")));
        setBackgroundCell(world, new Point(13, 10), new Background("redA",images.getImageList(images, "redA")));
        setBackgroundCell(world, new Point(14, 10), new Background("greenY",images.getImageList(images, "greenY")));
        setBackgroundCell(world, new Point(15, 10), new Background("redS",images.getImageList(images, "redS")));
        setBackgroundCell(world, new Point(16, 10), new Background("greenMk",images.getImageList(images, "greenMk")));
    }


    public void removeYeti(WorldModel world, ImageStore images) {
        Background a = new Background("snowreg2", images.getImageList(images, "snowreg2"));
        setBackgroundCell(world, new Point(9, 5), a);
        setBackgroundCell(world, new Point(10, 5), a);
        setBackgroundCell(world, new Point(11, 5), a);
        setBackgroundCell(world, new Point(12, 5), a);
        setBackgroundCell(world, new Point(13, 5), a);
        setBackgroundCell(world, new Point(9, 6), a);
        setBackgroundCell(world, new Point(10, 6), a);
        setBackgroundCell(world, new Point(11, 6), a);
        setBackgroundCell(world, new Point(12, 6), a);
        setBackgroundCell(world, new Point(13, 6), a);
        setBackgroundCell(world, new Point(9, 7), a);
        setBackgroundCell(world, new Point(10, 7), a);
        setBackgroundCell(world, new Point(11, 7), a);
        setBackgroundCell(world, new Point(12, 7), a);
        setBackgroundCell(world, new Point(13, 7), a);
        setBackgroundCell(world, new Point(9, 8), a);
        setBackgroundCell(world, new Point(10, 8), a);
        setBackgroundCell(world, new Point(11, 8), a);
        setBackgroundCell(world, new Point(12, 8), a);
        setBackgroundCell(world, new Point(13, 8), a);
        setBackgroundCell(world, new Point(9, 9), a);
        setBackgroundCell(world, new Point(10, 9), a);
        setBackgroundCell(world, new Point(11, 9), a);
        setBackgroundCell(world, new Point(12, 9), a);
        setBackgroundCell(world, new Point(13, 9), a);
    }


}
