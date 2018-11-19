package fr.sorbonne_u.datacenter.admissioncontroller.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestNotificationControllerI;
import fr.sorbonne_u.datacenter.admissioncontroller.interfaces.RequestcontrolleurI;

/**
 * The class <code>OutboundApplicationport</code> implements the outbound port
 * requiring the interface <code>RequestNotificationControllerI</code>.
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

public class OutboundApplicationport extends AbstractOutboundPort implements RequestNotificationControllerI {
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public OutboundApplicationport(ComponentI owner) throws Exception {
		super(RequestNotificationControllerI.class, owner);
	}

	public OutboundApplicationport(String uri, ComponentI owner) throws Exception {
		super(uri, RequestNotificationControllerI.class, owner);

		assert uri != null;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI#notifyRequestTermination(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void notifyRequestTermination(RequestcontrolleurI r) throws Exception {
		((RequestNotificationControllerI) this.connector).notifyRequestTermination(r);
	}
}
