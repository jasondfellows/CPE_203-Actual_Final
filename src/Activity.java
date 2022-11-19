public class Activity extends Action{
    public WorldModel world;
    public ImageStore imageStore;

    public Activity(Entity entity, WorldModel world, ImageStore imageStore){
        super(entity);
        this.imageStore = imageStore;
        this.world = world;
    }

    public void executeAction(Action action, EventScheduler scheduler){
        if (action.getEntity().getClass() == DudeNotFull.class || action.getEntity().getClass() == Sapling.class || action.getEntity().getClass() == Fairy.class || action.getEntity().getClass() == Tree.class || action.getEntity().getClass() == DudeFull.class){
            if (action.getEntity().getClass() == Fairy.class) {
                ((Fairy) action.getEntity()).executeFairyActivity(action.getEntity(), this.world, this.imageStore, scheduler);
            }
            else {
                ((Transformable) action.getEntity()).executeActivity(action.getEntity(), this.world, this.imageStore, scheduler);
            }
        }
    }
}
