package com.jitendra.SpringBatchExample.Config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.jitendra.SpringBatchExample.Model.Product;

@Configuration
public class BatchConfig {

    /**
     * Create the main Job named "job".
     *
     * @param jobRepository repository used to persist job metadata
     * @param listener job lifecycle listener (e.g. on completion)
     * @param steps the step to execute when this job runs
     * @return a configured {@link Job}
     */
    @Bean
    public Job jobBean(JobRepository jobRepository, JobCompletionNotificationImpl listener, Step steps) {
        return new JobBuilder("job", jobRepository)
                .listener(listener)
                .start(steps)
                .build();
    }

    /**
     * Configure the step that performs read->process->write work in chunks.
     *
     * This step is named "jobStep" and uses a chunk size of 10 with the provided
     * transaction manager.
     *
     * @param jobRepository repository used for step/job metadata
     * @param transactionManager manages transactions for each chunk
     * @param itemReader reads {@link Product} instances (CSV source)
     * @param itemProcessor transforms/filters {@link Product} instances
     * @param itemWriter writes processed {@link Product} instances to the sink
     * @return a configured {@link Step}
     */
    @Bean
    public Step steps(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
            ItemReader<Product> itemReader, ItemProcessor<Product, Product> itemProcessor,
            ItemWriter<Product> itemWriter) {
        return new StepBuilder("jobStep", jobRepository)
                .<Product, Product>chunk(10, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();

    }

    /**
     * Create a {@link FlatFileItemReader} that maps CSV rows to {@link Product}.
     *
     * Reads from the classpath resource "data.csv". The reader is configured
     * to skip the first line (header) and map the named fields to the
     * {@link Product} properties.
     *
     * @return a configured {@link FlatFileItemReader}&lt;Product&gt;
     */

    @Bean
    public FlatFileItemReader<Product> itemReader() {
        return new FlatFileItemReaderBuilder<Product>()
                .name("itemReader")
                .resource(new ClassPathResource("data.csv"))
                .delimited()
                .names("productId", "title", "description", "price", "discount")
                .linesToSkip(1)
                .targetType(Product.class)
                .build();

    }

    /**
     * Return the {@link ItemProcessor} that applies business rules to products.
     *
     * The {@code CustomItemProcessor} may modify a product or return {@code null}
     * to filter it out of the stream.
     *
     * @return an {@link ItemProcessor}&lt;Product, Product&gt;
     */

    @Bean
    public ItemProcessor<Product, Product> itemProcessor() {
        return new CustomItemProcessor();
    }

    /**
     * Create a JDBC {@link ItemWriter} to persist {@link Product} instances.
     *
     * Uses the provided {@code DataSource} and the configured INSERT SQL. Calling
     * {@code beanMapped()} maps bean properties to the named parameters in the SQL.
     *
     * @param dataSource JDBC datasource used for writes
     * @return an {@link ItemWriter}&lt;Product&gt; that performs batched inserts
     */

    @Bean
    public ItemWriter<Product> itemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(dataSource)
                .sql("INSERT INTO products (product_id, title, description, price, discount, discounted_price) VALUES (:productId, :title, :description, :price, :discount, :discountedPrice)")
                .beanMapped()
                .build();
    }

}
