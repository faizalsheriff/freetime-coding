package com.rockwellcollins.cs.hcms.core.services.statemanager.database;

import java.io.IOException;
import java.io.Serializable;
import com.rockwellcollins.cs.hcms.core.UnitManager;

public class StateData implements Serializable {
	private static final long serialVersionUID = 1L;
	protected int size = 0;
	protected String[] names;
	protected String[] defaultValues;
	protected String[] types;
	protected String[] rules;
	protected boolean[] persistents;

	public StateData() {
		size = 0;
	}
	
	public StateData(int capacity) {
		size = capacity;
		names = new String[capacity];
		defaultValues = new String[capacity];
		types = new String[capacity];
		rules = new String[capacity];
		persistents = new boolean[capacity];			
	}
	
	////////////////////////////////////////////////////////////////////////////
	// just in case Arrays[] aren't serialized by default
	/*****
	private void writeObject(java.io.ObjectOutputStream out)
     throws IOException {
		out.defaultWriteObject();
		out.writeInt(size);
		out.writeObject(names);
		out.writeObject(defaultValues);
		out.writeObject(types);
		out.writeObject(rules);
		out.writeObject(persistents);
		
		//for (int i=0; i<size; i++) {
		//	out.writeObject(names[i]);
		//	out.writeObject(defaultValues[i]);
		//	out.writeObject(types[i]);
		//	out.writeObject(rules[i]);
		//	out.writeBoolean(persistents[i]);
		//}
		
		out.flush();
	}
	
	private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		int size = (int)in.readInt();
		String[] names = (String[])in.readObject();
		String[] defaultValues = (String[])in.readObject();
		String[] types = (String[])in.readObject();
		String[] rules = (String[])in.readObject();
		boolean[] persistents = (boolean[])in.readObject();
		if (size > 0) {
			for (int i=0; i<names.length; i++) {
				this.names[i] = names[i];
			}
			for (int i=0; i<defaultValues.length; i++) {
				this.defaultValues[i] = defaultValues[i];
			}
			for (int i=0; i<types.length; i++) {
				this.types[i] = types[i];
			}
			for (int i=0; i<rules.length; i++) {
				this.rules[i] = rules[i];
			}
			for (int i=0; i<persistents.length; i++) {
				this.persistents[i] = persistents[i];
			}
		}
		//if (size > 0) {
		//	new StateData(size); //????
		//	for (int i=0; i<size; i++) {
		//		names[i] = (String)in.readObject();
		//		defaultValues[i] = (String)in.readObject();
		//		types[i] = (String)in.readObject();
		//		rules[i] = (String)in.readObject();
		//		persistents[i] = (boolean)in.readBoolean();
		//	}
		//}
	}	
	 ****/

	public boolean validate() {
		boolean result = (size > 0 
				&& names.length == types.length 
				&& types.length == rules.length 
				&& rules.length == defaultValues.length 
				&& defaultValues.length == persistents.length);
		
		if (!result) {
			UnitManager.Logging.logWarning("StateData validation:"
					+ " size: " + size 
					+ " names: " + names.length
					+ " types: " + types.length
					+ " rules: " + rules.length
					+ " defaultValues: " + defaultValues.length
					+ " persistents: " + persistents.length);
		}
		return result;
	}
}
