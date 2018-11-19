package fr.sorbonne_u.datacenterclient.tests;

import java.util.ArrayList;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.admissioncontroller.AdmissionController;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.tests.ComputerMonitor;
import fr.sorbonne_u.datacenter.software.applicationvm.application.Application;

/**
 * The class <code>TestAdmissonController</code> deploys a test application for
 * Applications deployment in a single JVM using the AdmissionController for a
 * data center simulation.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * A data center has a set of computers, each with several multi-core
 * processors. AdmissionController is created to Execute request of an
 * Application to decide deployment or decline. Admission Controller use the
 * static informations of Data Center represented by Core for each Computer's
 * Processor To verify resources into the Data Center See the data center
 * simulator documentation for more details about the implementation of this
 * simulation.
 * </p>
 * <p>
 * This test creates two computer component with two processors, each having two
 * cores. It then creates 3 Application to Test Submission. ControllerSubmission
 * component is then created and linked to the applications . The test scenario
 * starts the Admission Request,Accept App1 and App2 Deployment And Create
 * Dynamic Components.
 * </p>
 * <p>
 * The waiting time in the scenario and in the main method must be manually set
 * by the tester.
 * </p>
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>
 * Created on : May 5, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class TestAdmissonController extends AbstractCVM {
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String ComputerServicesInboundPortURI = "cs-ibp";
	public static final String ComputerStaticStateDataInboundPortURI = "css-dip";
	public static final String ComputerDynamicStateDataInboundPortURI = "cds-dip";

	public static final String ComputerServicesInboundPortURI1 = "cs-ibp1";
	public static final String ComputerStaticStateDataInboundPortURI1 = "css-dip1";
	public static final String ComputerDynamicStateDataInboundPortURI1 = "cds-dip1";

	public static final String ApplicationVMManagementInboundPortURI = "avm-ibp";
	public static final String ApplicationVMManagementInboundPortURI1 = "avm1-ibp";
	public static final String ApplicationVMManagementInboundPortURI2 = "avm2-ibp";

	public static final String RequestGeneratorManagementInboundPortURI = "rgmip";
	public static final String RequestGeneratorManagementInboundPortURI1 = "rgmip1";
	public static final String RequestGeneratorManagementInboundPortURI2 = "rgmip2";
	// --------------------------------------------------------------------------------

	/** Computer monitor component. */
	protected ComputerMonitor cm;
	protected ComputerMonitor cm1;
	/** Applications */
	protected Application app1, app2, app3;

	/** cores per Computer */
	protected Hashtable<String, Integer> cores;
	/** Computer URI and ServiceInboundPortURI */

	protected Hashtable<String, String> Computeruriservice;

	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public TestAdmissonController() throws Exception {
		super();
	}

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	@Override
	public void deploy() throws Exception {
		Processor.DEBUG = true;

		// --------------------------------------------------------------------
		// Create and deploy a computer component with its 2 processors and
		// each with 2 cores.
		// --------------------------------------------------------------------
		String computerURI = "computer0";
		int numberOfProcessors = 2;
		int numberOfCores = 2;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>();
		admissibleFrequencies.add(1500); // Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000); // and at 3 GHz
		Map<Integer, Integer> processingPower = new HashMap<Integer, Integer>();
		processingPower.put(1500, 1500000); // 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000); // 3 GHz executes 3 Mips
		Computer c = new Computer(computerURI, admissibleFrequencies, processingPower, 1500, // Test scenario 1,
																								// frequency = 1,5 GHz
				// 3000, // Test scenario 2, frequency = 3 GHz
				1500, // max frequency gap within a processor
				numberOfProcessors, numberOfCores, ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI, ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(c);

		c.toggleLogging();
		c.toggleTracing();

		String computerURI1 = "computer1";
		int numberOfProcessors1 = 2;
		int numberOfCores1 = 2;
		Set<Integer> admissibleFrequencies1 = new HashSet<Integer>();
		admissibleFrequencies1.add(1500); // Cores can run at 1,5 GHz
		admissibleFrequencies1.add(3000); // and at 3 GHz
		Map<Integer, Integer> processingPower1 = new HashMap<Integer, Integer>();
		processingPower1.put(1500, 1500000); // 1,5 GHz executes 1,5 Mips
		processingPower1.put(3000, 3000000); // 3 GHz executes 3 Mips
		Computer c1 = new Computer(computerURI1, admissibleFrequencies1, processingPower1, 1500, // Test scenario 1,
																									// frequency = 1,5
																									// GHz
				// 3000, // Test scenario 2, frequency = 3 GHz
				1500, // max frequency gap within a processor
				numberOfProcessors1, numberOfCores1, ComputerServicesInboundPortURI1,
				ComputerStaticStateDataInboundPortURI1, ComputerDynamicStateDataInboundPortURI1);
		this.addDeployedComponent(c1);

		//c1.toggleLogging();
		//c1.toggleTracing();
		// --------------------------------------------------------------------
		cores = new Hashtable<String, Integer>();
		cores.put(computerURI1, numberOfCores1 * numberOfProcessors1);
		cores.put(computerURI, numberOfCores * numberOfProcessors);
		Computeruriservice = new Hashtable<String, String>();
		Computeruriservice.put(computerURI, ComputerServicesInboundPortURI);
		Computeruriservice.put(computerURI1, ComputerServicesInboundPortURI1);

		// --------------------------------------------------------------------
		// Create the computer monitor component and connect its to ports
		// with the computer component.
		// --------------------------------------------------------------------
		this.cm = new ComputerMonitor(computerURI, true, ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI);
		this.addDeployedComponent(this.cm);
		this.cm1 = new ComputerMonitor(computerURI1, true, ComputerStaticStateDataInboundPortURI1,
				ComputerDynamicStateDataInboundPortURI1);
		this.addDeployedComponent(this.cm1);

		ArrayList<String> inboundporturi = new ArrayList<String>();

		this.app1 = new Application("app1", 2, 2, 5000.0, 60000000000L, RequestGeneratorManagementInboundPortURI,
				"rcdport1");
		this.addDeployedComponent(this.app1);

		inboundporturi.add("rcdport1");
		inboundporturi.add("rcdport2");
		inboundporturi.add("rcdport3");

		this.app2 = new Application("app2", 2, 1, 5000.0, 60000000000L, RequestGeneratorManagementInboundPortURI1,
				"rcdport2");
		this.addDeployedComponent(this.app2);
		this.app3 = new Application("app3", 2, 4, 5000.0, 60000000000L, RequestGeneratorManagementInboundPortURI2,
				"rcdport3");
		this.addDeployedComponent(this.app3);

		ArrayList<String> applicationuri = new ArrayList<String>();

		applicationuri.add("app1");
		applicationuri.add("app2");
		applicationuri.add("app3");

		AdmissionController admission;
		admission = new AdmissionController(this.cores, applicationuri, inboundporturi, Computeruriservice);
		this.addDeployedComponent(admission);

		// --------------------------------------------------------------------
		super.deploy();

	}

	// ------------------------------------------------------------------------
	// Test scenarios and main execution.
	// ------------------------------------------------------------------------

	public void start() throws Exception {
		super.start();

		this.app1.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {
					((Application) this.getOwner()).dynamicDeploy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app1.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {

					((Application) this.getOwner()).launch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app2.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {
					((Application) this.getOwner()).dynamicDeploy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app2.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {

					((Application) this.getOwner()).launch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		this.app3.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {
					((Application) this.getOwner()).dynamicDeploy();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		this.app3.runTask(new AbstractComponent.AbstractTask() {
			public void run() {
				try {

					((Application) this.getOwner()).launch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#execute()
	 */

	/**
	 * execute the test application.
	 * 
	 * @param args
	 *            command line arguments, disregarded here.
	 */
	public static void main(String[] args) {
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestAdmissonController trg = new TestAdmissonController();
			trg.startStandardLifeCycle(100000L);
			// Augment the time if you want to examine the traces after
			// the exeuction of the program.
			Thread.sleep(100000L);
			// Exit from Java (closes all trace windows...).
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
