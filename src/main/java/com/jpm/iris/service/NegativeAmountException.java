package com.jpm.iris.service;

public class NegativeAmountException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private double amount;

	public NegativeAmountException(double amount) {
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

}
