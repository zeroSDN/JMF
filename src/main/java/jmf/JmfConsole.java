package jmf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import jmf.data.ModuleUniqueId;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;

/**
 * Console utility to display a console for controlling an instance
 * Created on 9/28/15.
 *
 * @author Jonas Grunert
 */
public class JmfConsole {

	IFrameworkController frameworkController;

	public JmfConsole(IFrameworkController frameworkController) {
		this.frameworkController = frameworkController;
	}

	/**
	 * Starts a JMF console to control the given frameworkController
	 */
	public void startConsole() {

		System.out.println("---------- ZMF Console ----------");
		printHelpText();

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (!frameworkController.isStopped()) {
			try {
				// Wait with timeout for console input
				while (!reader.ready() && !frameworkController.isStopped()) {
					Thread.sleep(1000);
				}
				// Exit if stopped
				if (frameworkController.isStopped()) {
					System.out.println("Instance stopped - exiting");
					break;
				}
				line = reader.readLine();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			// Split commands
			String[] lineCommandSplit = line.split(" ");
			if (lineCommandSplit.length == 0) {
				System.err.println("Nothing entered");
				continue;
			}

			if (line.equals("en") || line.equals("enable")) {
				System.out.println("Requesting module enable");
				frameworkController.requestEnableModule();
			} else if (line.equals("dis") || line.equals("disable")) {
				System.out.println("Requesting module disable");
				frameworkController.requestDisableModule();
			} else if (line.equals("stop")) {
				System.out.println("Requesting instance stop");
				frameworkController.requestStopInstance();
				frameworkController.joinExecution();
				return;
			} else if (lineCommandSplit[0].equals("ren") || lineCommandSplit[0].equals("renable")) {
				ModuleUniqueId moduleIdR;
				if ((moduleIdR = tryStringToModuleId(lineCommandSplit[1])) == null) {
					continue;
				}
				// Remote enable
				boolean result = frameworkController.requestEnableRemoteInstance(moduleIdR, 1000);
				if (result) {
					System.out.println("Remote Enable Success");
				} else {
					System.err.println("Remote Enable Failed");
				}
			} else if (lineCommandSplit[0].equals("rdis") || lineCommandSplit[0].equals("rdisable")) {
				ModuleUniqueId moduleIdR;
				if ((moduleIdR = tryStringToModuleId(lineCommandSplit[1])) == null) {
					continue;
				}
				// Remote disable
				boolean result = frameworkController.requestDisableRemoteInstance(moduleIdR, 1000);
				if (result) {
					System.out.println("Remote Disable Success");
				} else {
					System.err.println("Remote Disable Failed");
				}
			} else if (lineCommandSplit[0].equals("rstop")) {
				ModuleUniqueId moduleIdR;
				if ((moduleIdR = tryStringToModuleId(lineCommandSplit[1])) == null) {
					continue;
				}
				// Remote stop
				boolean result = frameworkController.requestStopRemoteInstance(moduleIdR, 1000);
				if (result) {
					System.out.println("Remote Stop Success");
				} else {
					System.err.println("Remote Stop Failed");
				}
			} else {
				System.out.println("Unknown command");
				printHelpText();
			}
		}
	}

	private void printHelpText() {

		System.out.println("Commands");
		System.out.println("\"en\" or \"enable\" to enable this module");
		System.out.println("\"dis\" or \"disable\" to disable this module");
		System.out.println("\"stop\" to stop this instance");
		System.out.println("\"ren [type]:[instance]\" to remote enable an existing module with ID [type]:[instance]");
		System.out.println("\"rdis [type]:[instance]\" to remote disable an existing module with ID [type]:[instance]");
		System.out.println("\"rstop [type]:[instance]\" to remote stop an existing instance with ID [type]:[instance]");
	}

	private ModuleUniqueId tryStringToModuleId(String str) {

		try {
			String[] moduleIdPSplit = str.split(":");
			if (moduleIdPSplit.length != 2) {
				System.err.println("Invalid type identifier. Use format [type]:[version]");
				return null;
			}
			return new ModuleUniqueId(UnsignedInteger.valueOf(moduleIdPSplit[0]), UnsignedLong.valueOf(moduleIdPSplit[1]));
		} catch (Exception exc) {
			System.err.println("Invalid module identifier");
			return null;
		}
	}

	/**
	 * Test Main method
	 *
	 * @param args
	 * 		Execution arguments
	 */
	public static void main(String[] args) {
		AbstractModule testModule = new AbstractModule(new ModuleUniqueId(UnsignedInteger.valueOf(123), UnsignedLong.valueOf(456)), UnsignedInteger.valueOf(1), "TestModule", new ArrayList<>()) {
			@Override
			public boolean enable() {
				System.out.println("Enable");
				return true;
			}

			@Override
			public void disable() {
				System.out.println("Disable");
			}
		};

		IFrameworkController fc = Launcher.createInstance(testModule, true, true, false, false, false, Optional.<String>empty());
		JmfConsole console = new JmfConsole(fc);
		console.startConsole();
	}
}
