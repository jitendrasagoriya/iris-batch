package com.jpm.iris.service;

import org.springframework.batch.item.ItemProcessor;

import com.jpm.iris.model.Transaction;

public class CustomItemProcessor implements ItemProcessor<Transaction, Transaction> {

	public Transaction process(Transaction item) throws Exception {
		System.out.println("CustomItemProcessor :" + item);
		return item;
	}

}
