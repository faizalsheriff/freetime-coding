package com.rockwellcollins.cs.hcms.core.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;
import com.rockwellcollins.cs.hcms.core.profiling.CountdownTimer;

/**
 * A Queue Processor contains a background thread that checks to see if an item
 * is on the queue, if an item exists on the queue, it fires an event and
 * notifies all QueueListeners and is then removed from the queue.
 * 
 * If a timeout has been specified and timeout before an item is received by the
 * queue, the QueueProcessor will notify a timeout on all the QueueListeners.
 * 
 * The queues are synchronized.
 * 
 * @author getownse
 * 
 * @param <T>
 *            the type of queue-able item
 * 
 * @see QueueListener
 */
public class QueueProcessor<T> {

	private static final long JOIN_TIMEOUT = 1000;

	private Queue<T> queue;

	private List<QueueListener<T>> listeners;

	private Thread thread;

	private boolean running = false;

	private String name;

	private int timeout = 0;

	private int priority = 5;

	private final Object lock = new Object();
	
	private final CountdownTimer pollTimer = new CountdownTimer();;

	/**
	 * Create a new QueueProcessor with the given name
	 * 
	 * @param name
	 *            name of the queue processor
	 */
	public QueueProcessor(final String name) {
		this.name = name;
	}

	/**
	 * Add a listener to the Queue Processor. When an item is received or a
	 * timeout occurs, every registered listener will get a callback.
	 * 
	 * @param listener
	 *            queue listener
	 */
	public final void addListener(final QueueListener<T> listener) {

		synchronized (getListeners()) {
			getListeners().add(listener);
		}
	}

	/**
	 * Add an item to the queue to be processed.
	 * 
	 * @param item
	 *            item to be processed.
	 */
	public void addQueue(final T item) {
		getQueue().add(item);
	}

	/**
	 * @return name of the queue processor
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return thread priority of queue processor
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * QueueProcessor will timeout if it does not receive a queue item and call
	 * the listeners.
	 * 
	 * @return timeout in ms
	 */
	public final int getTimeout() {
		return this.timeout;
	}

	/**
	 * @return processor thread currently running
	 */
	public final boolean isRunning() {
		return running;
	}

	private final void run() {

		while (running) {

			try {
				T item;

				if (getTimeout() == 0) {

					item = getQueue().take();

				} else {

					item = getQueue().poll(getTimeout(), TimeUnit.MILLISECONDS);
				}

				if (running) {

					if (item == null) {

						notifyListenersTimeout();

					} else {
						// prevent background timer from being starved
						// during continuous heavy queue traffic
						if (getTimeout() != 0) {
							if (pollTimer.hasExpired()) {
								notifyListenersTimeout();								
							}
						}

						notifyListenersTake(item);

					}
				}
			} catch (final InterruptedException e) {
				UnitManager.Logging.logWarning("Queue Processor '" + getName()
						+ "' was Interrupted.  Continuing.");
			}
		}
	}

	/**
	 * Sets the thread priority of the queue processor
	 * 
	 * @param priority
	 *            thread priority
	 */
	public void setPriority(final int priority) {

		synchronized (lock) {

			if (getThread() != null) {
				getThread().setPriority(priority);
			}

			this.priority = priority;
		}
	}

	/**
	 * Sets the received item timeout in ms
	 * 
	 * @param timeout
	 *            timeout in ms
	 */
	public void setTimeout(final int timeout) {
		this.timeout = timeout;
		if (timeout != 0) {
			pollTimer.startTimer(timeout);
		}
	}

	protected final List<QueueListener<T>> getListeners() {

		if (listeners == null) {

			listeners = new ArrayList<QueueListener<T>>();
		}

		return listeners;
	}

	protected final void setThread(final Thread thread) {
		this.thread = thread;
	}

	protected final void setName(final String name) {
		this.name = name;
	}

	/**
	 * Starts the thread processor
	 * 
	 * @throws QueueProcessorException
	 */
	public final void start() throws QueueProcessorException {

		synchronized (lock) {

			if (isRunning()) {

				throw new QueueProcessorException("Queue Processor '"
						+ toString() + "' already started.");

			}

			try {

				setThread(UnitManager.Threading.createThread(this,
						new Runnable() {
							public void run() {
								QueueProcessor.this.run();
							}
						}, "Queue Processor '" + getName() + "'"));
				setRunning(true);
				getThread().setPriority(getPriority());
				getThread().start();

			} catch (final CoreThreadException e) {

				throw new QueueProcessorException(
						"UnitManager could not create Thread for Queue Processor '"
								+ getName() + "'.", e);

			} catch (final Exception e) {

				throw new QueueProcessorException(
						"Exception while creating thread in Queue Processor '"
								+ toString() + "'", e);
			}

		}
	}

	protected void setRunning(final boolean running) {

		this.running = running;
	}

	protected Thread getThread() {

		return thread;
	}

	/**
	 * Stops the thread processor
	 * 
	 * @throws QueueProcessorException
	 */
	public final void stop() throws QueueProcessorException {

		synchronized (lock) {

			if (isRunning()) {

				setRunning(false);

				synchronized (getQueue()) {
					getQueue().notifyAll();
				}

				try {

					getThread().join(JOIN_TIMEOUT);

				} catch (final InterruptedException e) {

					try {

						getThread().interrupt();
						getThread().join(JOIN_TIMEOUT);

					} catch (final InterruptedException e1) {

						throw new QueueProcessorException(
								"Interrupted Exception while joining Queue Processor '"
										+ getName() + "' thread.", e1);
					}
				}
			}
		}
	}

	/**
	 * @return get the max processor queue size
	 */
	public int getQueueSize() {
		synchronized (getQueue()) {
			return getQueue().size();
		}
	}

	/**
	 * @return true is the queue is empty, otherwise false
	 */
	public boolean isQueueEmpty() {
		synchronized (getQueue()) {
			return getQueue().isEmpty();
		}
	}

	protected final Queue<T> getQueue() {

		synchronized (this) {

			if (queue == null) {
				queue = new Queue<T>();
			}

			return queue;
		}
	}

	protected final void notifyListenersTake(final T item) {

		synchronized (getListeners()) {

			for (final QueueListener<T> listener : getListeners()) {
				listener.queueProcessorItemReceived(this, item);
			}
		}
	}

	protected final void notifyListenersTimeout() {

		synchronized (getListeners()) {

			for (final QueueListener<T> listener : getListeners()) {
				listener.queueProcessorTimeout(this);
			}
		}
		pollTimer.restart();
	}

	protected final void setQueue(final Queue<T> queue) {
		synchronized (this) {
			this.queue = queue;
		}
	}

	protected final void setListeners(final List<QueueListener<T>> listeners) {
		this.listeners = listeners;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void clear() {
		synchronized (getQueue()) {
			getQueue().clear();
		}
	}
}
