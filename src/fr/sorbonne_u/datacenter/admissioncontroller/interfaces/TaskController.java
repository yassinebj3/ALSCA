package fr.sorbonne_u.datacenter.admissioncontroller.interfaces;

import java.io.Serializable;



public interface TaskController extends Serializable {

	public Integer getCoresnum();

	public Integer getVMnum();
}