package com.rockwellcollins.cs.hcms.core.services.handlers;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.collections.QueueListener;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessor;
import com.rockwellcollins.cs.hcms.core.collections.QueueProcessorException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStartException;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopArgs;
import com.rockwellcollins.cs.hcms.core.services.ServiceStopException;

public abstract class EventHandlerTemplate<T> extends Handler implements
		QueueListener<T> {

	private static final long serialVersionUID = 1L;

	transient private QueueProcessor<T> queueProcessor;

	@Override
	protected void onStarted(final Object source, final ServiceStartArgs args)
			throws ServiceStartException {

		super.onStarted(source, args);

		try {

			getQueueProcessor().start();

		} catch (final QueueProcessorException e) {

			throw new ServiceStartException("Queue Processor in Handler '"
					+ toString() + "' failed to start.", e);
		}
	}

	@Override
	protected void onStopped(final Object source, final ServiceStopArgs args)
			throws ServiceStopException {

		try {

			getQueueProcessor().stop();
			getQueueProcessor().clear();

		} catch (final QueueProcessorException e) {

			throw new ServiceStopException("Queue Processor in Handler '"
					+ toString() + "' failed to stop.", e);
		}

		super.onStopped(source, args);
	}

	@Override
	protected void onInitialize(final Object source,
			final ComponentInitializeArgs args)
			throws ComponentInitializeException {

		super.onInitialize(source, args);

		getQueueProcessor().addListener(this);
	}

	protected QueueProcessor<T> getQueueProcessor() {

		if (queueProcessor == null) {
			setQueueProcessor(new QueueProcessor<T>(toString()
					+ " Queue Processor"));
		}

		return queueProcessor;
	}

	private void setQueueProcessor(final QueueProcessor<T> queueProcessor) {
		this.queueProcessor = queueProcessor;
	}
}
