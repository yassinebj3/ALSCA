package fr.sorbonne_u.datacenter.admissioncontroller.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestcontrolleurI;

/**
 * The class <code>RequestNotificationControllerConnector</code> implements a
 * connector for ports exchanging through the interface
 * <code>RequestNotificationControllerI</code>.
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
public class RequestNotificationControllerConnector extends AbstractConnector
		implements RequestNotificationControllerI {
	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI#notifyRequestTermination(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void notifyRequestTermination(RequestcontrolleurI r) throws Exception {

		((RequestNotificationControllerI) this.offering).notifyRequestTermination(r);
	}
}
