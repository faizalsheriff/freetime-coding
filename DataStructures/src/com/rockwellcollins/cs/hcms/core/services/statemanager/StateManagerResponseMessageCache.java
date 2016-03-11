package com.rockwellcollins.cs.hcms.core.services.statemanager;

import java.net.InetAddress;

import com.rockwellcollins.cs.hcms.core.collections.Cache;
import com.rockwellcollins.cs.hcms.core.collections.CacheItem;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateManagerMessage;
import com.rockwellcollins.cs.hcms.core.services.statemanager.messages.StateChangeResponseMessage;


public class StateManagerResponseMessageCache extends Cache<StateManagerMessage> {

	public StateManagerResponseMessageCache(final int size) {
		super(size);
	}

	@Override
	public boolean itemEqual(final StateManagerMessage item1,  // response
			final StateManagerMessage item2) { // request

		boolean result = false;
		if (item1 instanceof StateChangeResponseMessage) {
			result = item2.getId() == ((StateChangeResponseMessage)item1).getRequestId()
					&& ((StateChangeResponseMessage)item1).getRequestIp().equals(
							item2.getSourceInetAddress());
		}
		return result;
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
