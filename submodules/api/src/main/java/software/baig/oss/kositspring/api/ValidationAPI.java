package software.baig.oss.kositspring.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.baig.oss.kositspring.api.models.responses.ValidationResponse;
import software.baig.oss.kositspring.core.XRechnungValidationService;


@RestController
@RequestMapping("api/validate")
public class ValidationAPI {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationAPI.class);

    private final XRechnungValidationService service;


    @Autowired
    public ValidationAPI(XRechnungValidationService validationService) {
        this.service = validationService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ValidationResponse validate(@RequestBody byte[] xml, @RequestHeader(name = "X-COMPANY-ID", required = false) String companyId) {
        if (!isNullOrEmpty(companyId)) {
            LOG.info("[METRIC]: Attempting to validate on behalf of: {}", companyId);
        }

        return service.validate(xml, "invoice.xml");
    }

    @PostMapping(
            path = "/asHTML",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> validateHtml(@RequestBody byte[] xml, @RequestHeader(name = "X-COMPANY-ID", required = false) String companyId) {
        if (!isNullOrEmpty(companyId)) {
            LOG.info("[METRIC]: Attempting to validate on behalf of: {}", companyId);
        }

        ValidationResponse result = service.validate(xml, "invoice.xml");

        if (result.html() == null) {
            throw new IllegalArgumentException("No HTML report generated");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("Content-Security-Policy", "sandbox; default-src 'none'; style-src 'unsafe-inline'")
                .body(result.html());
    }


    private static boolean isNullOrEmpty(String s) {
        return (s == null || s.isBlank());
    }

}
