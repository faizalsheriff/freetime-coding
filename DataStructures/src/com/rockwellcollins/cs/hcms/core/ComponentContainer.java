/*
 *
 * Copyright 2007 Rockwell Collins, Inc. All Rights Reserved
 * NOTICE: The contents of this medium are proprietary to Rockwell
 * Collins, Inc. and shall not be disclosed, disseminated, copied,
 * or used except for purposes expressly authorized in written by
 * Rockwell Collins, Inc.
 *
 */
package com.rockwellcollins.cs.hcms.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.adapter.Adapter;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;

/**
 * The ComponentContainer is an advanced Collection Object for Components. The
 * Component container has specialized methods for retrieving specialized
 * Components contained within the collection.
 * 
 * @author Gary Townsend
 * @see Component
 */
public class ComponentContainer extends ArrayList<Component> {

	/**
	 * Sorts components by Setup Priority.
	 */
	private static final Comparator<Component> sorter = new Comparator<Component>() {
		public final int compare(final Component o1, final Component o2) {
			return o1.getSetupPriority() - o2.getSetupPriority();
		}
	};

	/**
	 * Serial Version UID should be Incremented with EVERY change to
	 * ComponentContainer. It is essential for Serialization checking.
	 */
	private static final long serialVersionUID = 7659970589989742350L;

	private final HashMap<String, Service> serviceMap = new HashMap<String, Service>();

	private final HashMap<String, Component> componentMap = new HashMap<String, Component>();

	private final HashMap<String, Adapter> adapterMap = new HashMap<String, Adapter>();

	private final HashMap<String, Handler> handlerMap = new HashMap<String, Handler>();

	@Override
	public final boolean add(final Component component) {

		synchronized (this) {

			addMap(component);

			return super.add(component);
		}
	}

	@Override
	public final void add(final int index, final Component component) {

		synchronized (this) {

			addMap(component);

			super.add(index, component);
		}
	}

	@Override
	public final boolean addAll(final Collection<? extends Component> components) {

		synchronized (this) {

			for (final Component component : components) {

				addMap(component);
			}

			return super.addAll(components);
		}
	}

	@Override
	public final boolean addAll(final int index,
			final Collection<? extends Component> components) {

		synchronized (this) {

			for (final Component component : components) {

				addMap(component);
			}

			return super.addAll(index, components);
		}
	}

	/**
	 * Gets a Component from the Container with given name
	 * 
	 * @param componentName
	 *            Name of the Component to get
	 * @return The Component in the container with given name, otherwise null
	 */
	public final Component get(final String componentName) {

		synchronized (this) {

			return componentMap.get(componentName);
		}
	}

	/**
	 * Gets an Adapter from the Container with given name
	 * 
	 * @param name
	 *            Name of the Adapter to get
	 * @return The Adapter in the container with given name, otherwise null
	 */
	public final Adapter getAdapter(final String name) {

		synchronized (this) {

			return adapterMap.get(name);
		}
	}

	/**
	 * Gets all of the Adapters in the Container
	 * 
	 * @return An array of all Adapters in the Container
	 */
	public final Adapter[] getAdapters() {

		Adapter[] adapters = {};

		synchronized (this) {

			adapters = adapterMap.values().toArray(adapters);
		}

		return adapters;
	}

	/**
	 * Gets a Set of Components who derive their class from given clazz
	 * 
	 * @param <T>
	 *            Type of class
	 * @param clazz
	 *            The class to compare all Components in the container against
	 * @return A Set of Components who derive from given clazz
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Component> Set<T> getByClass(final Class<T> clazz) {

		final Set<T> set = new LinkedHashSet<T>();

		synchronized (this) {

			for (final Component comp : this) {

				if (clazz.isInstance(comp)) {

					set.add((T) comp);
				}
			}
		}

		return Collections.unmodifiableSet(set);
	}

	/**
	 * Get a component by given name
	 * 
	 * @param name
	 *            Name of component to return
	 * @return The Component found in the collection with given name, otherwise
	 *         null
	 */
	public final Component getComponent(final String name) {

		synchronized (this) {

			return componentMap.get(name);
		}
	}

	/**
	 * Get the first Component of class type clazz. This method is best used if
	 * you are certain there is only one class of given type clazz.
	 * 
	 * @param <T>
	 *            The type of clazz
	 * @param clazz
	 *            The class in which the component derives
	 * @return A component that derives from given clazz, otherwise null
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Component> T getFirstByClass(final Class<T> clazz) {

		T result = null;

		synchronized (this) {

			for (final Component comp : this) {
				if (clazz.isInstance(comp)) {
					result = (T) comp;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Get a handler from the container with given name
	 * 
	 * @param name
	 *            Name of the handler
	 * @return A Handler in the container with given name, otherwise null
	 */
	public final Handler getHandler(final String name) {

		synchronized (this) {

			return handlerMap.get(name);
		}
	}

	/**
	 * Gets an array of Handlers from the container
	 * 
	 * @return an array of Handlers
	 */
	public final Handler[] getHandlers() {

		Handler[] handlers = {};

		synchronized (this) {

			handlers = handlerMap.values().toArray(handlers);

		}

		return handlers;
	}

	/**
	 * Get a Service in the container by given name
	 * 
	 * @param name
	 *            Name of the Service to get
	 * @return a service with given name, otherwise null
	 */
	public final Service getService(final String name) {

		synchronized (this) {

			return serviceMap.get(name);
		}
	}

	/**
	 * Gets an array of all services in container
	 * 
	 * @return array of all services in the container
	 */
	public final Service[] getServices() {

		Service[] services = {};

		synchronized (this) {

			services = serviceMap.values().toArray(services);
		}

		return services;
	}

	/**
	 * Rehashes all of the HashTable lookups. There is a look up table for
	 * adapters, services and handlers. These look ups are synchronized by
	 * properly calling add and remove. This method will rebuild the HashTables
	 * in case they become out of sync.
	 */
	public final void rehash() {

		synchronized (this) {

			clearMaps();

			for (final Component component : this) {

				addMap(component);
			}
		}
	}

	@Override
	public final Component remove(final int index) {

		Component component;

		synchronized (this) {
			component = super.remove(index);
			removeMap(component);
		}

		return component;
	}

	@Override
	public final boolean remove(final Object component) {

		synchronized (this) {

			if (component instanceof Component) {

				removeMap((Component) component);

			}

			return super.remove(component);
		}
	}

	@Override
	public final boolean removeAll(final Collection<?> components) {

		synchronized (this) {

			for (final Object component : components) {

				if (component instanceof Component) {

					removeMap((Component) component);
				}
			}

			return super.removeAll(components);
		}
	}

	@Override
	public final boolean retainAll(final Collection<?> components) {

		synchronized (this) {

			for (final Object component : components) {

				if (component instanceof Component) {

					removeMap((Component) component);
				}
			}

			return super.retainAll(components);
		}
	}

	private final void addMap(final Component component) {

		final String name = component.getName();

		synchronized (this) {

			if (component instanceof Service) {
				serviceMap.put(name, (Service) component);
			}

			if (component instanceof Handler) {
				handlerMap.put(name, (Handler) component);
			}

			if (component instanceof Adapter) {
				adapterMap.put(name, (Adapter) component);
			}

			componentMap.put(name, component);
		}
	}

	public final void sort() {
		Collections.sort(this, sorter);
	}

	private final void clearMaps() {

		synchronized (this) {

			serviceMap.clear();
			componentMap.clear();
			adapterMap.clear();
			handlerMap.clear();
		}
	}

	private final void removeMap(final Component component) {

		synchronized (this) {
			
			String n = component.getName();

			serviceMap.remove(n);
			componentMap.remove(n);
			handlerMap.remove(n);
			adapterMap.remove(n);
		}
	}

	@Override
	protected final void removeRange(final int start, final int end) {

		synchronized (this) {

			for (int i = start; i < end; i++) {
				removeMap(this.get(i));
			}

			super.removeRange(start, end);
		}
	}
}
