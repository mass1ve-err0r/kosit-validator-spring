package software.baig.oss.kositspring.core;

import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.xml.ProcessorProvider;


@Configuration
public class KositConfiguration {


    @Bean
    @ConditionalOnProperty(name = "app.local-development", havingValue = "true")
    public Check initializeKoSITValidatorLocal() throws URISyntaxException {
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

    @Bean
    @ConditionalOnMissingBean
    public Check normalKoSITValidator() throws URISyntaxException {
        Path scenarios = Path.of("/opt/einvoicing/config/scenarios.xml");
        de.kosit.validationtool.api.Configuration configuration = de.kosit.validationtool.api.Configuration
                .load(scenarios.toUri())
                .build(ProcessorProvider.getProcessor());

        return new DefaultCheck(configuration);
    }

}
