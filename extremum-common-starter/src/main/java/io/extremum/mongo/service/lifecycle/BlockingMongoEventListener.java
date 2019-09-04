package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.springdata.lifecycle.ReactiveOrigin;
import org.springframework.data.mongodb.core.mapping.event.*;

/**
 * This listener is explicitly blocking. This means that if it gets an event that is
 * marked as a 'reactive' event, it ignores such an event.
 * An event is marked as a 'reactive' one using {@link ReactiveOrigin} marker interface.
 *
 * @author rpuch
 */
public class BlockingMongoEventListener<E> extends AbstractMongoEventListener<E> {
    @Override
    public final void onBeforeConvert(BeforeConvertEvent<E> event) {
        super.onBeforeConvert(event);

        if (isNotReactive(event)) {
            onBeforeConvertBlockingly(event);
        }
    }

    private boolean isNotReactive(MongoMappingEvent<?> event) {
        return !(event instanceof ReactiveOrigin);
    }

    @Override
    public final void onBeforeSave(BeforeSaveEvent<E> event) {
        super.onBeforeSave(event);

        if (isNotReactive(event)) {
            onBeforeSaveBlockingly(event);
        }
    }

    @Override
    public final void onAfterLoad(AfterLoadEvent<E> event) {
        super.onAfterLoad(event);

        if (isNotReactive(event)) {
            onAfterLoadBlockingly(event);
        }
    }

    @Override
    public final void onAfterDelete(AfterDeleteEvent<E> event) {
        super.onAfterDelete(event);

        if (isNotReactive(event)) {
            onAfterDeleteBlockingly(event);
        }
    }

    @Override
    public final void onBeforeDelete(BeforeDeleteEvent<E> event) {
        super.onBeforeDelete(event);

        if (isNotReactive(event)) {
            onBeforeDeleteBlockingly(event);
        }
    }

    @Override
    public final void onAfterSave(AfterSaveEvent<E> event) {
        super.onAfterSave(event);

        if (isNotReactive(event)) {
            onAfterSaveBlockingly(event);
        }
    }

    @Override
    public final void onAfterConvert(AfterConvertEvent<E> event) {
        super.onAfterConvert(event);

        if (isNotReactive(event)) {
            onAfterConvertBlockingly(event);
        }
    }

    protected void onBeforeConvertBlockingly(BeforeConvertEvent<E> event) {
        // doing nothing
    }

    protected void onBeforeSaveBlockingly(BeforeSaveEvent<E> event) {
        // doing nothing
    }

    protected void onAfterLoadBlockingly(AfterLoadEvent<E> event) {
        // doing nothing
    }

    protected void onAfterDeleteBlockingly(AfterDeleteEvent<E> event) {
        // doing nothing
    }

    protected void onBeforeDeleteBlockingly(BeforeDeleteEvent<E> event) {
        // doing nothing
    }

    protected void onAfterSaveBlockingly(AfterSaveEvent<E> event) {
        // doing nothing
    }

    protected void onAfterConvertBlockingly(AfterConvertEvent<E> event) {
        // doing nothing
    }
}
