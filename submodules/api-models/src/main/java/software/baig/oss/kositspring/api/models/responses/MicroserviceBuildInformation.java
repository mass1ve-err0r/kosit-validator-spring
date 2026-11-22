package software.baig.oss.kositspring.api.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;


public record MicroserviceBuildInformation(
		String version,

		@JsonProperty("hash")
		String buildHash
) { }
