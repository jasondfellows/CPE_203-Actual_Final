public class Animation extends Action{
    public int repeatCount;

    public Animation(Entity entity, int repeatCount){
        super(entity);
        this.repeatCount = repeatCount;
    }

    public void executeAction(Action action, EventScheduler scheduler){
        action.getEntity().nextImage(action.getEntity());

        if (this.repeatCount != 1) {
            scheduler.scheduleEvent(scheduler, action.getEntity(),
                    new Animation(action.getEntity(),
                            Math.max(this.repeatCount - 1,
                                    0)),
                    ((Actionables)action.getEntity()).getAnimationPeriod());
        }
    }
}
