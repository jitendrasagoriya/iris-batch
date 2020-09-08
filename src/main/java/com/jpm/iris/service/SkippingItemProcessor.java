package com.jpm.iris.service;

import org.springframework.batch.item.ItemProcessor;

import com.jpm.iris.model.Transaction;

public class SkippingItemProcessor implements ItemProcessor<Transaction, Transaction> {

	public Transaction process(Transaction transaction) throws Exception {

		System.out.println("SkippingItemProcessor: " + transaction);

		if (transaction.getUsername() == null || transaction.getUsername().isEmpty()) {
			throw new MissingUsernameException();
		}

		double txAmount = transaction.getAmount();
		if (txAmount < 0) {
			throw new NegativeAmountException(txAmount);
		}

		return transaction;
	}

}
