package software.baig.oss.kositspring.core;

import java.net.URL;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.xml.ProcessorProvider;


@Configuration
public class KositConfiguration {


    @Bean
    public Check initializeKoSITValidator() throws URISyntaxException {
        URL scenarios = Thread.currentThread()
                .getContextClassLoader()
                .getResource("kosit/scenarios.xml");

        if (scenarios == null) {
            throw new IllegalStateException("KoSIT scenarios.xml not found");
        }

        // jesus christ, you desperately need better naming...
        de.kosit.validationtool.api.Configuration configuration = de.kosit.validationtool.api.Configuration
                .load(scenarios.toURI())
                .build(ProcessorProvider.getProcessor());

        return new DefaultCheck(configuration);
    }

}
