package batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

/**
 * spring batch 配置
 * @author
 */
@Configuration
@EnableBatchProcessing
//@ImportResource({"classpath:/org/springframework/batch/admin/web/resources/webapp-config.xml"
//        ,"classpath:/org/springframework/batch/admin/web/resources/servlet-config.xml"})
public class BlackListBatchConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(BlackListBatchConfiguration.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CustFlatFileItemReaderConfiguration custFlatFileItemReaderConfiguration;

    /**
     * 处理过程
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<BlackList, BlackList> processor() {

        return new ItemProcessor<BlackList, BlackList>() {
            @Override
            public BlackList process(BlackList blackList) throws Exception {
                BlackList blackListTransform = new BlackList();
                blackListTransform.setFlag(1);
                blackListTransform.setFirstname(blackList.getFirstname().toUpperCase());
                blackListTransform.setLastname(blackList.getLastname().toUpperCase());
                return  blackListTransform;
            }
        };
    }

    /**
     * 写出内容
     * @return
     */
    @Bean
    @StepScope
    public JpaItemWriter<BlackList> writer() {
        JpaItemWriter<BlackList> jpaItemWriter = new JpaItemWriter();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    /**
     * 构建job
     * @param jobs
     * @return
     */
    @Bean
    public Job importFileJob(JobBuilderFactory jobs, Step step1,JobRepository jobRepository) {
        return jobs.get("importFileJob")
                .incrementer(new RunIdIncrementer())
                .repository(jobRepository)
                .flow(step1)
                .end()
                .build();
    }

    /**
     * 声明step
     * @param stepBuilderFactory
     * @param reader
     * @param writer
     * @param processor
     * @return
     */
    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory,
                      ItemWriter<BlackList> writer, ItemProcessor<BlackList, BlackList> processor,PlatformTransactionManager transactionManager) {
        logger.error("step1");
        return stepBuilderFactory.get("step1")
                .<BlackList, BlackList> chunk(500)
                .reader(custFlatFileItemReaderConfiguration.setStrName("abc").read())
                .processor(processor)
                .writer(writer)
                .faultTolerant()
//                .retry(Exception.class)   // 重试
                .noRetry(ParseException.class)
//                .retryLimit(1)           //每条记录重试一次
//                .listener(new RetryFailuireItemListener())
                .skip(Exception.class)
                .skipLimit(500)         //一共允许跳过200次异常
                .taskExecutor(new SimpleAsyncTaskExecutor()) //设置并发方式执行
                .throttleLimit(10)        //并发任务数为 10,默认为4
//                .transactionManager(transactionManager)
                .build();
    }

}