package batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustFlatFileItemReaderConfiguration {

    private String strName;

    public CustFlatFileItemReaderConfiguration setStrName(String strName) {
        this.strName = strName;
        return this;
    }

    /**
     * 读取外部文件方法
     * @return
     * @throws IOException
     */
    @Bean
    @StepScope
    public FlatFileItemReader<BlackList> read() {
        FlatFileItemReader<BlackList> reader = new FlatFileItemReader<BlackList>();
        reader.setResource(new ClassPathResource("blacklist.csv"));
        reader.setLineMapper(new DefaultLineMapper<BlackList>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstname", "lastname" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<BlackList>() {{
                setTargetType(BlackList.class);
            }});
        }});
        System.out.println(this.strName);
        return reader;
    }

}
