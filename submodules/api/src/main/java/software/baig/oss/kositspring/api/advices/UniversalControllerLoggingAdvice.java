package software.baig.oss.kositspring.api.advices;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import software.baig.oss.kositspring.api.models.responses.UniversalErrorResponse;


@RestControllerAdvice
public class UniversalControllerLoggingAdvice extends ResponseEntityExceptionHandler {


	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        UniversalErrorResponse response = new UniversalErrorResponse(statusCode, request, ex);

		return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(response);
	}
	
}
