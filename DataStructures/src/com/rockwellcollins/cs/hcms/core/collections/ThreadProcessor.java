package com.rockwellcollins.cs.hcms.core.collections;

import com.rockwellcollins.cs.hcms.core.CoreThreadException;
import com.rockwellcollins.cs.hcms.core.UnitManager;

/**
 * A thread processor is a thread wrapper with a callback (listener) when the
 * thread executes. It has additional code for the tread startup and shutdown.
 * The thread is also created by the UnitManager.
 * 
 * @author getownse
 * 
 */
public class ThreadProcessor {

	private static final int JOIN_TIMEOUT = 1000;

	private Thread thread;

	private boolean running;

	private String name;

	private int priority = 5;

	private ThreadProcessorListener listener;

	/**
	 * Create a new thread processor with given name and callback listener
	 * 
	 * @param name
	 *            name of the thread processor
	 * @param listener
	 *            callback for run action
	 */
	public ThreadProcessor(final String name,
			final ThreadProcessorListener listener) {
		setName(name);
		setListener(listener);
	}

	/**
	 * @return thread processor name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return thread processor thread's priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return true is thread is running, otherwise false
	 */
	public final boolean isRunning() {
		return running;
	}

	private final void run() {
		while (running) {

			try {

				getListener().threadProcessorAction(this);

			} catch (final InterruptedException e) {

				UnitManager.Logging.logWarning("Thread Processor '" + getName()
						+ "' was Interrupted.  Continueing.");

			}
		}
	}

	/**
	 * Sets the priority of the working thread
	 * 
	 * @param priority
	 *            priority of the working thread
	 */
	public void setPriority(final int priority) {

		synchronized (this) {

			if (getThread() != null) {
				thread.setPriority(priority);
			}

			this.priority = priority;
		}
	}

	/**
	 * Starts the thread processor.
	 * 
	 * @throws ThreadProcessorException
	 *             if the thread processor is already running
	 */
	public final void start() throws ThreadProcessorException {

		synchronized (this) {

			if (isRunning()) {
				throw new ThreadProcessorException("Thread Processor '"
						+ toString() + "' already started.");
			}

			try {

				setThread(UnitManager.Threading.createThread(this,
						new Runnable() {
							public void run() {
								ThreadProcessor.this.run();
							}
						}, "Thread Processor '" + getName() + "'"));
				setRunning(true);
				getThread().setPriority(getPriority());
				getThread().start();

			} catch (final CoreThreadException e) {

				throw new ThreadProcessorException(
						"Could not create worker thread.", e);

			} catch (final Exception e) {

				throw new ThreadProcessorException(
						"Exception occured while creating thread in Thread Processor '"
								+ toString() + "'.", e);

			}

		}
	}

	/**
	 * Stops the thead processor
	 * 
	 * @throws ThreadProcessorException
	 */
	public void stop() throws ThreadProcessorException {

		synchronized (this) {

			if (isRunning()) {

				setRunning(false);

				try {

					getThread().join(JOIN_TIMEOUT);

				} catch (final InterruptedException e) {

					try {

						getThread().interrupt();
						getThread().join(JOIN_TIMEOUT);

					} catch (final InterruptedException e1) {

						throw new ThreadProcessorException(
								"Interrupted Exception while joining Thread Processor '"
										+ getName() + "' thread.", e1);
					}

				} catch (final Exception e) {

					throw new ThreadProcessorException(
							"Unknown exception in Thread Processor '"
									+ toString() + "'", e);

				}
			}
		}
	}

	protected final Thread getThread() {
		return thread;
	}

	protected final void setThread(final Thread thread) {
		this.thread = thread;
	}

	protected final ThreadProcessorListener getListener() {
		return listener;
	}

	protected final void setRunning(final boolean running) {
		this.running = running;
	}

	protected final void setName(final String name) {
		this.name = name;
	}

	protected final void setListener(final ThreadProcessorListener listener) {
		this.listener = listener;
	}

	@Override
	public String toString() {
		return getName();
	}
}