package fr.sorbonne_u.datacenter.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.sorbonne_u.components.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.sorbonne_u.components.reflection.connectors.ReflectionConnector;
import fr.sorbonne_u.components.reflection.ports.ReflectionOutboundPort;
import fr.sorbonne_u.datacenter.admissioncontroller.connectors.RequestNotificationControllerConnector;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestContlleurHandlerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestcontrolleurI;
import fr.sorbonne_u.datacenter.admissioncontroller.ports.InboundAdmissionport;
import fr.sorbonne_u.datacenter.admissioncontroller.ports.OutboundApplicationport;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.connectors.RequestNotificationConnector;
import fr.sorbonne_u.datacenterclient.requestdispatcher.Requestdispatcher;
import fr.sorbonne_u.datacenterclient.tests.Integrator;

/**
 * The class <code>AdmissionController</code> deploys an AdmissionController for
 * an application. This component decide to accept application deployment or
 * decline.
 * 
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * 
 * <p>
 * This Component is used to verify Resources in the Data center and allocate
 * Core for an Application. AdmissionController receive an Application request
 * indicating the number of ApplicationVM and the number of necessary core to
 * launch it. AdmissionController search into the cores structure for a number
 * of cores into a Computer to run the Application.it return the URI of Computer
 * to allocate cores from the indicated one then deploy dynamically
 * ApplicationVMs and RequestDispatcher and interconnect them. After this step ,
 * AdmissionController send a notify to the application and indicate the
 * inboundDispatcherSubmission to connect the RequestGenerator with the
 * requestDispatcher
 * 
 * </p>
 *
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
 * Created on : Nov 10, 2018
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class AdmissionController extends AbstractComponent implements RequestContlleurHandlerI {

	/** URI of this AdmissionController */
	protected String AdmissionControllerURI;

	/** Cores per Computer */
	protected static Hashtable<String, Integer> cores = new Hashtable<String, Integer>();
	/** DynamicComponentCreationOutboundPort Port */
	protected DynamicComponentCreationOutboundPort portdynamicComponentCreation;

	/** URI Used for Reflection port. */
	protected String admissionControllerUri = "";

	/** URI of Computerserviceinboundport */
	protected String computerserviceportinboud;

	/** ArrayList to Save Allocated Core */
	protected ArrayList<AllocatedCore[]> alloc;

	/** URI of Computer and ComputerServicesInboundPort */
	protected Hashtable<String, String> Computeruriservice;

	/** List of this InboundAdmissionport of AdmissionController */
	protected ArrayList<InboundAdmissionport> portadmission;

	/** URI of Application. */
	protected String applicationuri;

	/**
	 * URI of InboundApplication and AdmissionController OutboundApplicationPort to
	 * connect with Application
	 */

	protected Hashtable<String, OutboundApplicationport> portoutboundapplication;

	/** Object used to synchronize Application Admission Request */
	protected static Object key = new Object();

	/**
	 * 
	 * The constructor takes as parameter HashMap of cores containing the Computer
	 * URI and the number of cores, an arrayList of the URI of
	 * OutboundApplicationport, a portInbound controller URI arrayList and a HashMap
	 * containing the Computers URI's with its ComputerServicesInboundPort .
	 * 
	 * @param cores
	 *            HashMap containing the URI of Computer as a key and number of Core
	 *            as a value
	 * @param portoutboundapplication
	 *            URI of OutboundApplicationport
	 * @param portInboundController
	 *            URI of InboundAdmissionport
	 * @param computerserviceportinboud
	 *            URI of ComputerServicesInboundPort
	 * @throws Exception
	 */

	public AdmissionController(Hashtable<String, Integer> cores, ArrayList<String> portoutboundapplication,
			ArrayList<String> portInboundController, Hashtable<String, String> computerserviceportinboud)
			throws Exception {
		super(1, 1);

		this.Computeruriservice = computerserviceportinboud;
		this.addOfferedInterface(RequestSubmissionControllerI.class);
		this.portadmission = new ArrayList<InboundAdmissionport>();

		for (int i = 0; i < portInboundController.size(); i++) {
			InboundAdmissionport iba;
			iba = new InboundAdmissionport(portInboundController.get(i), this);
			addPort(iba);
			iba.publishPort();
			this.portadmission.add(iba);
		}
		this.addOfferedInterface(RequestNotificationControllerI.class);
		this.cores = cores;
		this.portoutboundapplication = new Hashtable<String, OutboundApplicationport>();
		for (int i = 0; i < portoutboundapplication.size(); i++) {
			this.portoutboundapplication.put(portoutboundapplication.get(i) + "portout",
					new OutboundApplicationport(portoutboundapplication.get(i) + "portout", this));
			this.addPort(this.portoutboundapplication.get(portoutboundapplication.get(i) + "portout"));
			this.portoutboundapplication.get(portoutboundapplication.get(i) + "portout").publishPort();
		}
	}

	/**
	 * 
	 * the numberOfcoresApp method takes in parameter a VM HashMap with a URI and
	 * number of desired cores, the method traverses the data structure to know the
	 * total number of cores per application
	 * 
	 * @param VMs
	 * @return Number of core
	 */
	protected int numberOfcoresApp(HashMap<String, Integer> VMs) {

		int somme = 0;

		for (Entry<String, Integer> entry : VMs.entrySet()) {

			somme = somme + entry.getValue();
		}

		return somme;

	}

	public void start() throws ComponentStartException {

		try {
			this.portdynamicComponentCreation = new DynamicComponentCreationOutboundPort(this);
			this.addPort(this.portdynamicComponentCreation);
			this.portdynamicComponentCreation.localPublishPort();
			this.portdynamicComponentCreation.doConnection(
					this.admissionControllerUri + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
		this.toggleTracing();
		this.toggleLogging();

	}

	@Override
	public void finalise() throws Exception {

		try {
			this.doPortDisconnection(portdynamicComponentCreation.getPortURI());
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		// Disconnect ports to the request emitter and to the processors owning
		// the allocated cores.
		try {
			for (int i = 0; i < portadmission.size(); i++) {
				portadmission.get(i).unpublishPort();
			}

			for (Map.Entry mapentry : portoutboundapplication.entrySet()) {
				((OutboundApplicationport) mapentry.getValue()).unpublishPort();
			}

			portdynamicComponentCreation.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException("processor services outbound port disconnection" + " error", e);
		}

		super.shutdown();
	}

	/**
	 * The acceptRequestSubmissionAndnotify method receives a request from the
	 * Application to check the resources needed for the deployment.
	 * AcceptRequestSubmissionAndNotify recovers the number of Core, the number of
	 * ApplicationVM, the Application URI, the InboundApplication port URI, and the
	 * RequestGenerator RequestNotificationInboundPort port URI of the request.This
	 * method looks for resources by calling the SearchRessouces method and passing
	 * it as a parameter the number of Core and the number of ApplicationVM. If
	 * there is a Computer available, the method searchRessources returns the URI of
	 * the Computer which will be used for the reservation of resources.
	 * Getressource method which takes in parameter the Computer URI and the number
	 * of core necessary for the execution of the application . the uri of the
	 * Computer is used to connect to the computer and get tables of cores which
	 * will be passed to the ApplicationVM, at the end the AdmissionController call
	 * the DynamicDeploy method for Dynamic Component creation.
	 */

	public void acceptRequestSubmissionAndNotify(final RequestcontrolleurI r) throws Exception {

		synchronized (key) {
			String computerURIi = null;
			ComputerServicesOutboundPort csop;
			csop = new ComputerServicesOutboundPort(this);
			this.addPort(csop);
			csop.publishPort();
			int numbercores = r.getCoresnum();
			int numbervm = r.getVMnum();
			this.applicationuri = r.getapplicationuri();
			String inboundapplicationURI = r.getinboundappuri();
			String notificationinboundDispatcherportURI = r.getnotificationinboundportURI();
			this.logMessage("*** Looking for ressources for Application " + this.applicationuri + "*** \n");
			this.logMessage("** Cores number " + r.getCoresnum() + "\n");
			this.logMessage("** nombreVM " + r.getVMnum() + "\n");
			computerURIi = SearchRessouces(numbercores, numbervm);
			if (computerURIi != null) {
				this.logMessage("--- Ressources found in Computer " + computerURIi);
				int NumberofperApplication = numbervm * numbercores;
				Getressource(computerURIi, NumberofperApplication);
				String csipuri = getComputerServicesInboundPortURI(computerURIi);
				computerserviceportinboud = csipuri;
				this.logMessage("---------------------------------------------------\n");
				try {
					this.doPortConnection(csop.getPortURI(), csipuri,
							ComputerServicesConnector.class.getCanonicalName());
				} catch (Exception e) {
					System.out.println(e + "aa");
				}

				alloc = new ArrayList<AllocatedCore[]>();

				for (int i = 0; i < r.getVMnum(); i++) {
					alloc.add(csop.allocateCores(numbercores));
				}

				dynamicDeploy(numbercores, numbervm, notificationinboundDispatcherportURI, inboundapplicationURI);

			} else {
				this.logMessage("**** Cannot find resources ****");
			}
		}
	}

	/**
	 * The SearchResources method takes into account a number of cores and a number
	 * of vm and allows to check in the data structure cores if there is an
	 * available number of cores it returns the computer uri otherwise return null
	 * 
	 * @param numbercores
	 * @param numbervm
	 * @return
	 */
	protected String SearchRessouces(int numbercores, int numbervm) {

		int somme = numbercores * numbervm;

		for (Entry<String, Integer> entry : cores.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			if (value >= somme) {
				return key;
			}
		}
		return null;
	}

	protected synchronized void Getressource(String OrdinteurURI, int nbrecoeur) {

		int valeur = cores.get(OrdinteurURI).intValue();
		valeur = valeur - nbrecoeur;
		cores.put(OrdinteurURI, valeur);
	}

	/**
	 * The getComputerServiceInboundPortURI method takes the computer's URI and
	 * returns the URI of its portinboundservice
	 * 
	 * @param OrdinateurURI
	 * @return
	 */

	protected String getComputerServicesInboundPortURI(String OrdinateurURI) {

		return Computeruriservice.get(OrdinateurURI);

	}

	@Override
	public void acceptRequestSubmission(RequestcontrolleurI r) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptRequestTerminationNotification(RequestcontrolleurI r) {
		// TODO Auto-generated method stub

	}

	/**
	 * generate methods generate Request Generator URI
	 */
	public String generateRDURI() {
		return this.applicationuri + "RD";
	}

	/**
	 * generate methods generate VM URI
	 */
	public String generateVMURI(int i) {
		return this.applicationuri + "VM" + i;

	}

	/**
	 * generate methods generate Integrator URI
	 */
	public String generateintegrator() {
		return this.applicationuri + "integrator";
	}

	/**
	 * generate methods generate VMapplicationVMManagement URI
	 */
	public String GenerateVMapplicationVMManagementipuri(int i) {
		return this.generateVMURI(i) + "vmmiuri";
	}

	/**
	 * generate methods generate URI
	 */
	public String GenerateVMapplicationsubinboundporturi(int i) {
		return this.generateVMURI(i) + "vmibp";
	}

	/**
	 * generate methods generate URI
	 */
	public String GenerateVMapplicationnotificationinboundporturi(int i) {
		return this.generateVMURI(i) + "vmnibp";
	}

	/**
	 * generate methods generate VMapplicationnotificationoutbound URI
	 */
	public String GenerateVMapplicationnotificationoutboundporturi(int i) {

		return this.generateVMURI(i) + "vmnobp";
	}

	/**
	 * generate methods generate RequestDispatcher URI
	 */
	public String GenerateRequestDispatcherURI() {
		return this.generateRDURI();
	}

	/**
	 * generate methods generate DispatcherSubmissionInboundPort URI
	 */
	public String GenerateDispatcherSubmissionInboundPortURI() {
		return this.applicationuri + "rdsinpuri";
	}

	/**
	 * generate methods generate DispatcherNotificationOutboundPort URI
	 */
	public String GeneraterequestDispatcherNotificationOutboundPortURI() {
		return this.applicationuri + "rnsinpuri";
	}

	/**
	 * The dynamicDeploy method dynamically generates virtual machines and request
	 * dispatcher. At the beginning it generates the uri of the ports necessary for
	 * the interconnection then it creates the vm and the request dispatcher and
	 * interconnects the components thanks to the port of reflection. At the end the
	 * dynamicDeploy creates an integrator that allows resources to be allocated to
	 * the VMs and sends a notification to the application through the
	 * outboundapplication port containing the RequestDispatcherSubmissionPortURI
	 * uri to link it with the request generator.
	 * 
	 * @param numbercores
	 * @param numberVM
	 * @param notificationinboundportURI
	 * @param inboundport
	 * @throws Exception
	 */
	public void dynamicDeploy(int numbercores, int numberVM, String notificationinboundportURI, String inboundport)
			throws Exception {
		ArrayList<String> RequestDispatcherSubmissionURIlist = new ArrayList<String>();
		ArrayList<String> RequestDispatcherNotificationURIlist = new ArrayList<String>();
		HashMap<String, Integer> ApplicationVMManagementInboundPortURIList = new HashMap<String, Integer>();
		for (int i = 0; i < numberVM; i++) {

			String applicationVMURI = generateVMURI(i);
			String portManagement = GenerateVMapplicationVMManagementipuri(i);
			String portsubinbound = GenerateVMapplicationsubinboundporturi(i);
			String portnotificationinbound = GenerateVMapplicationnotificationinboundporturi(i);
			String portnotificationoutbound = GenerateVMapplicationnotificationoutboundporturi(i);

			try {
				this.doPortConnection(this.applicationuri + "portout", inboundport,
						RequestNotificationControllerConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				this.portdynamicComponentCreation.createComponent(ApplicationVM.class.getCanonicalName(),
						new Object[] { applicationVMURI, portManagement, portsubinbound, portnotificationinbound,
								portnotificationoutbound, });
			} catch (Exception e) {
				e.printStackTrace();
			}

			RequestDispatcherSubmissionURIlist.add(portsubinbound);
			RequestDispatcherNotificationURIlist.add(portnotificationinbound);
			ApplicationVMManagementInboundPortURIList.put(portManagement, numbercores);

		}

		String RequestDispatcherURI = GenerateRequestDispatcherURI();
		String RequestDispatcherSubmissionInboundPortURI = GenerateDispatcherSubmissionInboundPortURI();
		String requestDispatcherNotificationOutboundPortURI = GeneraterequestDispatcherNotificationOutboundPortURI();

		try {
			this.portdynamicComponentCreation.createComponent(Requestdispatcher.class.getCanonicalName(),
					new Object[] { RequestDispatcherURI, RequestDispatcherSubmissionInboundPortURI,
							RequestDispatcherSubmissionURIlist, RequestDispatcherNotificationURIlist,
							notificationinboundportURI, // URI GEN
							requestDispatcherNotificationOutboundPortURI });
		} catch (Exception e) {
			e.printStackTrace();
		}

		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();

		for (int i = 0; i < numberVM; i++) {
			String applicationVMURI = generateVMURI(i);
			try {
				rop.doConnection(applicationVMURI, ReflectionConnector.class.getCanonicalName());
			} catch (Exception e) {
				e.printStackTrace();

			}
			String portnotificationinbound = GenerateVMapplicationnotificationinboundporturi(i);
			String portnotificationoutbound = GenerateVMapplicationnotificationoutboundporturi(i);

			try {
				rop.doPortConnection(portnotificationoutbound, portnotificationinbound,
						RequestNotificationConnector.class.getCanonicalName());
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			rop.doConnection(RequestDispatcherURI, ReflectionConnector.class.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();

		}

		try {
			rop.doPortConnection(requestDispatcherNotificationOutboundPortURI, notificationinboundportURI,
					RequestNotificationConnector.class.getCanonicalName());
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		String integratoruri = generateintegrator();

		try {
			this.portdynamicComponentCreation.createComponent(Integrator.class.getCanonicalName(),
					new Object[] { integratoruri, ApplicationVMManagementInboundPortURIList, this.alloc });
		} catch (Exception e) {
			e.printStackTrace();
		}

		ControllerTask r = new ControllerTask(RequestDispatcherSubmissionInboundPortURI);

		this.portoutboundapplication.get(this.applicationuri + "portout").notifyRequestTermination(r);

	}

}
