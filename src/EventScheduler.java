import java.util.*;

/**
 * Keeps track of events that have been scheduled.
 */
public final class EventScheduler
{
    private PriorityQueue<Event> eventQueue;
    private Map<Entity, List<Event>> pendingEvents;
    private double timeScale;



    public EventScheduler(double timeScale) {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
    }

    public void scheduleEvent(
            EventScheduler scheduler,
            Entity entity,
            Action action,
            long afterPeriod)
    {
        System.out.println(this.pendingEvents.size());
        System.out.println(this.eventQueue.size());
        if (VirtualWorld.done){
            if(entity.getClass() == DudeNotFull.class){
                return;
            }
        }
        long time = System.currentTimeMillis() + (long)(afterPeriod
                * this.timeScale);
        Event event = new Event(action, time, entity);

        this.eventQueue.add(event);

        // update list of pending events for the given entity
        List<Event> pending = this.pendingEvents.getOrDefault(entity,
                new LinkedList<>());
        pending.add(event);
        this.pendingEvents.put(entity, pending);
    }

    public void unscheduleAllEvents(
            EventScheduler scheduler, Entity entity)
    {
        List<Event> pending = this.pendingEvents.remove(entity);

        if (pending != null) {
            for (Event event : pending) {
                this.eventQueue.remove(event);
            }
        }
    }

    public void removePendingEvent(
            EventScheduler scheduler, Event event)
    {
        List<Event> pending = this.pendingEvents.get(event.entity);

        if (pending != null) {
            pending.remove(event);
        }
    }

    public void updateOnTime(EventScheduler scheduler, long time) {
        while (!this.eventQueue.isEmpty()
                && this.eventQueue.peek().time < time) {
            Event next = this.eventQueue.poll();

            this.removePendingEvent(scheduler, next);

            next.action.executeAction(next.action, scheduler);
        }
    }

}
