package software.baig.oss.kositspring.core;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kosit.validationtool.impl.DefaultResult;
import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;

import software.baig.oss.kositspring.api.models.responses.ValidationResponse;


@Service
public class XRechnungValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(XRechnungValidationService.class);

    private final Check validator;
    private final XRechnungPDFService pdfService;


    @Autowired
    public XRechnungValidationService(Check validator, XRechnungPDFService pdfService) {
        this.validator = validator;
        this.pdfService = pdfService;
    }

    public ValidationResponse validate(byte[] xml, String filename) {
        Input input = InputFactory.read(xml, filename);
        Result result = validator.checkInput(input);

        String pdfBase64 = null;
        String recommendation = result.getAcceptRecommendation().toString();
        if ("ACCEPTABLE".equals(recommendation)) {
            try {
                pdfBase64 = new String(Base64.getEncoder().encode(pdfService.generate(xml)), StandardCharsets.UTF_8);
            } catch (Exception exception) {
                LOG.warn("PDF visualization failed", exception);
            }
        }

        return new ValidationResponse(
                result.isProcessingSuccessful(),
                result.getAcceptRecommendation().toString(),
                result.getProcessingErrors(),
                ((DefaultResult) result).extractHtmlAsString().stream().findFirst().orElse(null),
                pdfBase64
        );
    }

}
