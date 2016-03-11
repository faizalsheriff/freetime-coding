package com.rockwellcollins.cs.hcms.core;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import com.rockwellcollins.cs.hcms.core.UnitManager.Logging;
import com.rockwellcollins.cs.hcms.core.services.MulticastService;
import com.rockwellcollins.cs.hcms.core.services.Service;
import com.rockwellcollins.cs.hcms.core.services.adapter.Adapter;
import com.rockwellcollins.cs.hcms.core.services.handlers.Handler;
import com.rockwellcollins.cs.hcms.core.services.logging.LoggingService;
import com.rockwellcollins.cs.hcms.core.services.statemanager.StateManager;
import com.rockwellcollins.cs.hcms.core.services.statemanager.database.StateDatabase;

public class LcpLoader extends DefaultUnitManagerLoader {

	private File lcpFile;
	private boolean verbose = true;

	@Override
	protected void onComplete() throws CoreException {

		if (verbose) {

			StringBuilder sb = new StringBuilder();

			sb.append("###( UnitManager ENVIRONMENT )###");

			for (Component c : UnitManager.ObjectModel.getComponents()) {
				sb.append("\n\n---| " + c.getName() + " |---\n");
				sb.append("\n  Class: ");
				sb.append(c.getClass());
				sb.append("\n  Setup Priority: ");
				sb.append(c.getSetupPriority());

				if (c instanceof Service) {
					Service s = (Service) c;
					sb.append("\n  -- Service");
					sb.append("\n     Thread Priority: ");
					sb.append(s.getThreadPriority());
					sb.append("\n     Startup Type: ");
					sb.append(s.getStartupType());
				}

				if (c instanceof MulticastService) {
					MulticastService m = (MulticastService) c;
					sb.append("\n  -- Multicast Service");
					sb.append("\n     Port: ");
					sb.append(m.getPort());
				}

				if (c instanceof Adapter) {
					sb.append("\n  -- Adapter");
				}

				if (c instanceof Handler) {
					Handler h = (Handler) c;
					sb.append("\n  -- Handler");
					sb.append("\n     Listen Mode: ");
					sb.append(h.getListenMode());
				}

				if (c instanceof LoggingService) {
					LoggingService ls = (LoggingService) c;
					sb.append("\n  -- LoggingService");
					sb.append("\n     Multicast Group: ");
					sb.append(ls.getMulticastGroup().getHostAddress());
					sb.append("\n     Logging: ");
					sb.append(ls.isLogging());
					sb.append("\n     File Name: ");
					sb.append(ls.getFileName());
					sb.append("\n     File Extension: ");
					sb.append(ls.getFileExtension());
					sb.append("\n     Exception Pattern: ");
					sb.append(ls.getExceptionPattern());
					sb.append("\n     Message Pattern: ");
					sb.append(ls.getMessagePattern());
					sb.append("\n     Type Pattern: ");
					sb.append(ls.getTypePattern());
					sb.append("\n     Source Pattern: ");
					sb.append(ls.getSourcePattern());
				}

				if (c instanceof StateManager) {
					StateManager sm = (StateManager) c;
					StateDatabase db = sm.getStateDatabase();
					sb.append("\n  -- StateManager");
					sb.append("\n     Multicast: ");
					sb.append(sm.getMulticastGroup().getHostAddress());
					sb.append("\n     Mode: ");
					sb.append(sm.getMode());
					sb.append("\n     Database Block Count: ");
					sb.append(db.getBlockCount());
					sb.append("\n     Database Block Size: ");
					sb.append(db.getBlockSize());
					sb.append("\n     Database Size: ");
					sb.append(db.getSize());
					sb.append("\n     Database Signature: ");
					sb.append(Integer.toHexString(sm.getDatabaseSignature())
							.toUpperCase());
				}

				if (c instanceof Unit) {
					Unit u = (Unit) c;
					sb.append("\n  -- Unit");
					sb.append("\n     IP Address: ");
					sb.append(u.getIpAddress());
					sb.append("\n     Unit Group: ");
					sb.append(u.getUnitGroup());
					sb.append("\n     Unit Number: ");
					sb.append(u.getUnitNumber());
					sb.append("\n     Bulk Directory: ");
					sb.append(u.getBulkDirectory());
					sb.append("\n     Cache Directory: ");
					sb.append(u.getCacheDirectory());
					sb.append("\n     Log Directory: ");
					sb.append(u.getLogDirectory());
					sb.append("\n     Store Directory: ");
					sb.append(u.getStoreDirectory());
					sb.append("\n     Temp Directory: ");
					sb.append(u.getTempDirectory());
					sb.append("\n     Working Directory: ");
					sb.append(u.getWorkingDirectory());
					sb.append("\n     System Data Directory: ");
					sb.append(u.getSystemDataDirectory());
					sb.append("\n     Unit Data Directory: ");
					sb.append(u.getUnitDataDirectory());

				}
			}

			sb.append("\n\n###( Runtime Environment )###\n");
			sb.append("\n     Default Character Set: ");
			sb.append(Charset.defaultCharset().displayName());

			sb.append("\n\n###( Java System Properties )###\n");
			
			Properties props = System.getProperties();

			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				sb.append("\n     ");
				sb.append(entry.getKey());
				sb.append(" == ");
				sb.append(entry.getValue());
			}

			sb.append("\n");

			Logging.logStdOut(sb.toString());

			Logging.logStdOut("###( UnitManager COMPLETING )###");
		}

		super.onComplete();
	}

	@Override
	protected void onInitialize(final Map<String, String> p)
			throws CoreException {

		super.onInitialize(p);

		String name;
		String value;

		if (verbose) {
			Logging.logStdOut("###( UnitManager INITIALIZE )###");
		}

		for (Map.Entry<String, String> entry : p.entrySet()) {

			name = entry.getKey();
			value = entry.getValue();

			if ("core".equals(name) || "--core".equals(name)
					|| "-c".equals(name)) {
				Logging.setCore(true);
				Logging.logStdOut("  Logging Core");
			} else if ("stdout".equals(name) || "--stdout".equals(name)) {
				Logging.logStdOut("   Logging StdOut");
				Logging.setStdOut(true);
			} else if ("info".equals(name) || "--info".equals(name)
					|| "-i".equals(name)) {
				Logging.setInfo(true);
				Logging.logStdOut("  Logging Info");
			} else if ("debug".equals(name) || "--debug".equals(name)
					|| "-d".equals(name)) {
				Logging.setDebug(true);
				Logging.logStdOut("  Logging Debug");
			} else if ("profile".equals(name) || "--profile".equals(name)
					|| "-p".equals(name)) {
				Logging.setProfile(true);
				Logging.logStdOut("  Logging Profile");
			} else if ("lcp".equals(name) || "--lcp".equals(name)
					|| "-l".equals(name)) {
				lcpFile = new File(value);
				LCPPathName = value;
				Logging.logStdOut("  LCP = " + lcpFile);
			} else if ("verbose".equals(name) || "--verbose".equals(name)
					|| "-v".equals(name)) {
				verbose = Boolean.parseBoolean(value);
			} else if ("help".equals(name) || "--help".equals(name)
					|| "-h".equals(name)) {
				Logging.logStdOut("  UnitManager [name=vale|arg]...");
				Logging.logStdOut("    lcp=file       : lcpConfig file");
				Logging.logStdOut("    info           : log information");
				Logging.logStdOut("    debug          : log debug");
				Logging.logStdOut("    profile        : log profiling");
				Logging.logStdOut("    core           : log core");
				Logging
						.logStdOut("    verbose=true   : verbose mode, true is default");
			}
		}

		if (lcpFile == null || !lcpFile.exists()) {
			throw new CoreException("LcpLoader could not find LCP '" + lcpFile
					+ "'");
		}
	}

	@Override
	protected void onLoad() throws CoreException {

		if (verbose) {
			Logging.logStdOut("###( UnitManager LOAD )###");
		}

		super.onLoad();

		final LcpConfig lcpConfig = new LcpConfig();
		lcpConfig.setLcpConfigFile(lcpFile);
		lcpConfig.setComponent(UnitManager.ObjectModel.components);
		lcpConfig.parse();
	}

	@Override
	protected void onSetup() throws CoreException {

		if (verbose) {
			Logging.logStdOut("###( UnitManager SETUP )###");
		}

		super.onSetup();
	}

	@Override
	protected void onSetup(Component component) {
		if (verbose) {
			Logging.logStdOut("---| Setup " + component.getName() + " |---");
		}
		super.onSetup(component);
	}

	protected final void setLcpFile(File lcpFile) {
		this.lcpFile = lcpFile;
	}

	protected final File getLcpFile() {
		return lcpFile;
	}
}
