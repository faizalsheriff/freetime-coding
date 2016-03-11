package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.InetAddress;

import com.rockwellcollins.cs.hcms.core.collections.Cache;
import com.rockwellcollins.cs.hcms.core.collections.CacheItem;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;

public class StateManagerRequestMessageCache extends Cache<StateManagerMessage> {

	public StateManagerRequestMessageCache(final int size) {
		super(size);
	}

	@Override
	public boolean itemEqual(final StateManagerMessage item1,
			final StateManagerMessage item2) {

		return item1.getId() == item2.getId()
				&& item1.getSourceInetAddress().equals(
						item2.getSourceInetAddress());
	}

	public void flushIp(final InetAddress ip) {

		synchronized (this) {

			int len = list.size();

			for (int i = 0; i < len; i++) {
				CacheItem<StateManagerMessage> itr = list.get(i);
				if (ip.equals(itr.getItem().getSourceInetAddress())) {
					itr.setDirty(true);
				}
			}
		}
	}
}
