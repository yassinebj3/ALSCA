package fr.sorbonne_u.datacenter.admissioncontroller.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestSubmissionControllerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestcontrolleurI;

/**
 * The class <code>RequestSubmissionControllerConnector</code> implements a
 * connector for ports exchanging through the interface
 * <code>RequestSubmissionControllerI</code>.
 *
 * <p>
 * <strong>Description</strong>
 * </p>
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
 * Created on : April 9, 2015
 * </p>
 * 
 * @author <a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */

public class RequestSubmissionControllerConnector extends AbstractConnector implements RequestSubmissionControllerI {
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequest(RequestcontrolleurI r) throws Exception {
		((RequestSubmissionControllerI) this.offering).submitRequest(r);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI#submitRequest(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void submitRequestAndNotify(RequestcontrolleurI r) throws Exception {

		((RequestSubmissionControllerI) this.offering).submitRequestAndNotify(r);
	}
}
