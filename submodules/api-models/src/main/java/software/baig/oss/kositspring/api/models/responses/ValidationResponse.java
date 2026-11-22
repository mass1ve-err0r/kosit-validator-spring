package software.baig.oss.kositspring.api.models.responses;

import java.util.List;


public record ValidationResponse(
            boolean processingSuccessful,
            String recommendation,
            List<String> processingErrors,
            String html,
            String pdfBase64
) { }
