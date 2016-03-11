package com.rockwellcollins.cs.hcms.core.services.handlers;

public interface HandlerListener {
	void handlerPropertyChanged(final Handler handler,
			final PropertyChangedArgs args);

	void handlerPropertyMapChanged(final Handler handler,
			final PropertyMapChangedArgs args);

	void handlerPropertyChangedTimeout(final Handler handler,
			final PropertyChangeTimeoutArgs args);
}
