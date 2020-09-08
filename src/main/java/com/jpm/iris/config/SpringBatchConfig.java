package com.jpm.iris.config;

import java.text.ParseException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.jpm.iris.model.Transaction;
import com.jpm.iris.service.CustomItemProcessor;
import com.jpm.iris.service.CustomItemWriter;
import com.jpm.iris.service.CustomSkipPolicy;
import com.jpm.iris.service.MissingUsernameException;
import com.jpm.iris.service.NegativeAmountException;
import com.jpm.iris.service.RecordFieldSetMapper;
import com.jpm.iris.service.SkippingItemProcessor;

public class SpringBatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Value("file:D:\\SPRINGBATCH\\iris-batch\\src\\main\\resources\\input\\record.csv")
	private Resource inputCsv;

	@Value("file:")
	private Resource invalidInputCsv;

	@Value("file:xml/output.xml")
	private Resource outputXml;

	public ItemReader<Transaction> itemReader(Resource inputData) throws UnexpectedInputException, ParseException {
		FlatFileItemReader<Transaction> reader = new FlatFileItemReader();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		String[] tokens = { "username", "userid", "transactiondate", "amount" };
		tokenizer.setNames(tokens);
		reader.setResource(inputData);
		DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper();
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new RecordFieldSetMapper());
		reader.setLinesToSkip(1);
		reader.setLineMapper(lineMapper);
		return reader;
	}

	@Bean
	public ItemProcessor<Transaction, Transaction> itemProcessor() {
		return new CustomItemProcessor();
	}

	@Bean
	public ItemProcessor<Transaction, Transaction> skippingItemProcessor() {
		return new SkippingItemProcessor();
	}

	@Bean
	public ItemWriter<Transaction> itemWriter(){		 
		return new CustomItemWriter();
	}

	@Bean
	public Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(Transaction.class);
		return marshaller;
	}

	@Bean
	protected Step step1(@Qualifier("itemProcessor") ItemProcessor<Transaction, Transaction> processor,
			ItemWriter<Transaction> writer) throws ParseException {
		return stepBuilderFactory.get("step1").<Transaction, Transaction>chunk(10).reader(itemReader(inputCsv))
				.processor(processor).writer(writer).build();
	}

	@Bean(name = "firstBatchJob")
	public Job job(@Qualifier("step1") Step step1) {
		return jobBuilderFactory.get("firstBatchJob").start(step1).build();
	}

	@Bean
	public Step skippingStep(@Qualifier("skippingItemProcessor") ItemProcessor<Transaction, Transaction> processor,
			ItemWriter<Transaction> writer) throws ParseException {
		return stepBuilderFactory.get("skippingStep").<Transaction, Transaction>chunk(10)
				.reader(itemReader(invalidInputCsv)).processor(processor).writer(writer).faultTolerant().skipLimit(2)
				.skip(MissingUsernameException.class).skip(NegativeAmountException.class).build();
	}

	@Bean(name = "skippingBatchJob")
	public Job skippingJob(@Qualifier("skippingStep") Step skippingStep) {
		return jobBuilderFactory.get("skippingBatchJob").start(skippingStep).build();
	}

	@Bean
	public Step skipPolicyStep(@Qualifier("skippingItemProcessor") ItemProcessor<Transaction, Transaction> processor,
			ItemWriter<Transaction> writer) throws ParseException {
		return stepBuilderFactory.get("skipPolicyStep").<Transaction, Transaction>chunk(10)
				.reader(itemReader(invalidInputCsv)).processor(processor).writer(writer).faultTolerant()
				.skipPolicy(new CustomSkipPolicy()).build();
	}

	@Bean(name = "skipPolicyBatchJob")
	public Job skipPolicyBatchJob(@Qualifier("skipPolicyStep") Step skipPolicyStep) {
		return jobBuilderFactory.get("skipPolicyBatchJob").start(skipPolicyStep).build();
	}

}
