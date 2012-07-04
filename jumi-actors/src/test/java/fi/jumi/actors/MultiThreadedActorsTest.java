// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.EventizerProvider;
import fi.jumi.actors.failures.FailureHandler;
import fi.jumi.actors.logging.MessageListener;
import org.junit.*;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class MultiThreadedActorsTest extends ActorsContract<MultiThreadedActors> {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    protected MultiThreadedActors newActors(EventizerProvider eventizerProvider, FailureHandler failureHandler, MessageListener messageListener) {
        return new MultiThreadedActors(executor, eventizerProvider, failureHandler, messageListener);
    }

    @Override
    protected void processEvents() {
        // noop; background threads run automatically, rely on the timeouts in the contract tests for waiting
    }

    @After
    public void stopExecutor() throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS));
    }


    @Test
    public void actor_threads_are_backed_by_real_threads() throws InterruptedException {
        SpyDummyListener rawActor = new SpyDummyListener();

        ActorThread actorThread = actors.startActorThread();
        ActorRef<DummyListener> actorRef = actorThread.bindActor(DummyListener.class, rawActor);
        actorRef.tell().onSomething("event");
        awaitEvents(1);

        assertThat(rawActor.thread, is(notNullValue()));
        assertThat(rawActor.thread, is(not(Thread.currentThread())));
    }
}
