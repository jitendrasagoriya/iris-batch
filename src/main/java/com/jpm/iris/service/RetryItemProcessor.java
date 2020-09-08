package com.jpm.iris.service;

import org.springframework.batch.item.ItemProcessor;

import com.jpm.iris.model.Transaction;

public class RetryItemProcessor implements ItemProcessor<Transaction, Transaction> {

	public Transaction process(Transaction item) throws Exception {

		return new Transaction();
	}

}
