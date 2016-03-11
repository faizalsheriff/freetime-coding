package com.rockwellcollins.cs.hcms.core.profiling.tests;

import com.rockwellcollins.cs.hcms.core.ComponentInitializeArgs;
import com.rockwellcollins.cs.hcms.core.ComponentInitializeException;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileResult;
import com.rockwellcollins.cs.hcms.core.profiling.ProfileTest;

public class ExampleTest extends ProfileTest {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void onInitialize(Object source, ComponentInitializeArgs args)
			throws ComponentInitializeException {
		// TODO Auto-generated method stub
		super.onInitialize(source, args);
		setName("Example Test");
	}

	@Override
	protected void onRun(ProfileResult result) {
		super.onRun(result);

		/** On Run with Result Goes Here **/
		System.out.println("Did Test!");

		result.setGrade(99.9);
		result.setNotes("TESTING RIGHT NOW!");
	}
}
