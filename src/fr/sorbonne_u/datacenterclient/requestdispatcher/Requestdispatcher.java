package fr.sorbonne_u.datacenterclient.requestdispatcher;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;

import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.sorbonne_u.datacenterclient.requestgenerator.RequestGenerator;

/**
 * The class <code>Requestdispatcher</code> is a component that distributes
 * Request between the ApplicationVM using round Robin.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
 * The component receives requests from the generator through its
 * RequestSubmissionInboundPort port and in turn transmits the queries to the
 * ApplicationVM through the RequestSubmissionOutboundPort port connected with
 * the VM in question. At the end, it receives the notifications through its
 * RequestNotificationInboundPort from a VM and passes the notification to the
 * Generator through the RequestNotificationOutboundPort.
 * 
 * <p>
 * <strong>Invariant</strong>
 * </p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>
 * Created on :Nov, 2018
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @author Salammbo
 */

public class Requestdispatcher extends AbstractComponent
		implements RequestNotificationHandlerI, RequestSubmissionHandlerI {
	public static int DEBUG_LEVEL = 2;

	// -------------------------------------------------------------------------
	// Constants and instance variables
	// -------------------------------------------------------------------------

	/** the URI of the component. */
	protected final String rgURI;

	/** Counter Round-Robin */
	protected int counter = 0;

	/**
	 * Inbound port offering the request submission service of the
	 * RequestDispatcher.
	 */
	protected RequestSubmissionInboundPort requestSubmissionInboundPort;

	/** Outbound port used by the RequestDispatcher to notify tasks' termination. */
	protected RequestNotificationOutboundPort requestNotificationOutboundPort;

	/**
	 * RequestSubmissionOutboundPort ports used by the RequestDispatcher to Dispatch
	 * request to the VM
	 */
	protected ArrayList<RequestSubmissionOutboundPort> requestDispatcherSubmissionlist;

	/**
	 * RequestNotificationInboundPort ports used by the RequestDispatcher to Receive
	 * notification From the VM
	 */
	protected ArrayList<RequestNotificationInboundPort> requestDispatcherNotificationlist;

	/**
	 * URI of ports RequestSubmissionInboundPort used by the RequestDispatcher to
	 * Connect with the VM
	 */
	protected ArrayList<String> requestDispatcherInboundPortURI;

	/** URI of Computer RequestNotificationInboundPortURI */

	protected String requestNotificationInboundPortURI;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a RequestDispatcher component.
	 * 
	 * @param rgURI
	 *            RequestDispatcher URI
	 * @param requestSubmissionInboundPortURI
	 *            RequestSubmissionInboundPort URI
	 * @param requestDispatcherInboundPortURI
	 *            URI list of VM's RequestSubmissionInboundPort
	 * @param requestDispatchernotificationListt
	 *            URI List of RequestDispatcher RequestNotificationInboundPort
	 * @param requestDispatcherNotificationInboundPortURI
	 *            URI of RequestGenerator RequestNotificationInboundPort
	 * @param requestDispatcherNotificationOutboundPortURI
	 *            URI of RequestNotificationOutboundPort
	 * @throws Exception
	 */

	public Requestdispatcher(String rgURI, String requestSubmissionInboundPortURI,
			ArrayList<String> requestDispatcherInboundPortURI, ArrayList<String> requestDispatchernotificationListt,
			String requestDispatcherNotificationInboundPortURI, String requestDispatcherNotificationOutboundPortURI)
			throws Exception {
		super(rgURI, 1, 1);

		// preconditions check

		assert requestSubmissionInboundPortURI != null;
		assert requestDispatchernotificationListt != null;
		assert requestDispatcherInboundPortURI != null;
		assert requestDispatcherNotificationInboundPortURI != null;
		assert requestDispatcherNotificationOutboundPortURI != null;

		// initialization
		this.rgURI = rgURI;
		this.requestNotificationInboundPortURI = requestDispatcherNotificationInboundPortURI;

		requestDispatcherSubmissionlist = new ArrayList<RequestSubmissionOutboundPort>();
		this.addRequiredInterface(RequestSubmissionI.class);

		for (int i = 0; i < requestDispatcherInboundPortURI.size(); i++) {

			requestDispatcherSubmissionlist.add(new RequestSubmissionOutboundPort(this));
			this.addPort(requestDispatcherSubmissionlist.get(i));
			requestDispatcherSubmissionlist.get(i).publishPort();
		}

		this.addOfferedInterface(RequestNotificationI.class);

		this.requestDispatcherInboundPortURI = requestDispatcherInboundPortURI;

		requestDispatcherNotificationlist = new ArrayList<RequestNotificationInboundPort>();

		for (int i = 0; i < requestDispatchernotificationListt.size(); i++) {

			requestDispatcherNotificationlist
					.add(new RequestNotificationInboundPort(requestDispatchernotificationListt.get(i), this));
			this.addPort(requestDispatcherNotificationlist.get(i));
			requestDispatcherNotificationlist.get(i).publishPort();
		}

		this.requestSubmissionInboundPort = new RequestSubmissionInboundPort(requestSubmissionInboundPortURI, this);

		this.addPort(this.requestSubmissionInboundPort);
		this.requestSubmissionInboundPort.publishPort();

		this.addRequiredInterface(RequestNotificationI.class);

		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(
				requestDispatcherNotificationOutboundPortURI, this);
		this.addPort(this.requestNotificationOutboundPort);
		this.requestNotificationOutboundPort.publishPort();

	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void start() throws ComponentStartException {
		this.toggleTracing();
		this.toggleLogging();

		super.start();

		try {
			for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {
				this.doPortConnection(requestDispatcherSubmissionlist.get(i).getPortURI(),
						requestDispatcherInboundPortURI.get(i), RequestSubmissionConnector.class.getCanonicalName());
			}

		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void finalise() throws Exception {
		for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {
			this.doPortDisconnection(requestDispatcherSubmissionlist.get(i).getPortURI());

		}

		

		super.finalise();
	}

	@Override
	public void shutdown() throws ComponentShutdownException {

		try {
			for (int i = 0; i < requestDispatcherSubmissionlist.size(); i++) {

				requestDispatcherSubmissionlist.get(i).unpublishPort();
			}

			for (int i = 0; i < requestDispatcherNotificationlist.size(); i++) {

				requestDispatcherNotificationlist.get(i).unpublishPort();
			}

			this.requestNotificationOutboundPort.unpublishPort();
			this.requestSubmissionInboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------

	/**
	 * process an end of execution notification for a request r previously submitted
	 * and send it to RequestGenerator.
	 * 
	 * <p>
	 * <strong>Contract</strong>
	 * </p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r
	 *            request that just terminated.
	 * @throws Exception
	 *             <i>todo.</i>
	 */

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		assert r != null;

		if (RequestGenerator.DEBUG_LEVEL == 2) {
			this.logMessage("Request Dispatcher " + this.rgURI + " is notified that request " + r.getRequestURI()
					+ " has ended.then dispatch the notification to the Request Generator ");

			this.requestNotificationOutboundPort.notifyRequestTermination(r);
		}
	}

	// -----------------------------------------------------------

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

	}
	// ----------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(final RequestI r) throws Exception {

		if (ApplicationVM.DEBUG) {
			this.logMessage("** Dispatcher >> accept Request *** ");
		}
		this.logMessage(this.rgURI + "*** Accept Request *** " + r.getRequestURI());

		requestDispatcherSubmissionlist.get(counter).submitRequestAndNotify(r);
		if (counter == requestDispatcherSubmissionlist.size() - 1) {
			counter = 0;
		} else {
			counter++;
		}

	}

}