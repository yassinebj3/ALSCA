package fr.sorbonne_u.datacenter.interfaces;

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

/**
 * The interface <code>PushModeControllingI</code> defines the
 * services to be implemented to control the data push services.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>Created on : September 30, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface			PushModeControllingI
{
	/**
	 * start the pushing of data and force the pushing to be done each
	 * <code>interval</code> period of time. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	interval &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param interval		delay between pushes (in milliseconds).
	 * @throws Exception		<i>todo.</i>
	 */
	public void			startUnlimitedPushing(final int interval)
	throws Exception ;

	/**
	 * start <code>n</code> pushing of data and force the pushing to be done
	 * each <code>interval</code> period of time. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	interval &gt; 0 and n &gt; 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param interval		delay between pushes (in milliseconds).
	 * @param n				total number of pushes to be done, unless stopped.
	 * @throws Exception		<i>todo.</i>
	 */
	public void			startLimitedPushing(final int interval, final int n)
	throws Exception ;

	/**
	 * stop the pushing of data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception		<i>todo.</i>
	 */
	public void			stopPushing() throws Exception ;
}
