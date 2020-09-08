package com.jpm.iris.service;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.jpm.iris.model.Transaction;

public class CustomItemWriter implements ItemWriter<Transaction> {

	public void write(List<? extends Transaction> items) throws Exception {
		 items.forEach(i -> {
			 System.out.println("CustomItemWriter :"+i);
		 });
		
	}

}
