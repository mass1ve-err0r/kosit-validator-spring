package software.baig.oss.kositspring.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.baig.oss.kositspring.api.models.responses.MicroserviceBuildInformation;


@RestController
@RequestMapping("api/system")
public class MicroserviceInformationAPI {

	private static final Logger LOG = LoggerFactory.getLogger(MicroserviceInformationAPI.class);

	private final BuildProperties buildProperties;


	@Autowired
	public MicroserviceInformationAPI(BuildProperties buildProperties) {
		this.buildProperties = buildProperties;
	}

	@GetMapping("health")
	public ResponseEntity<Void> healthcheck() {
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	@GetMapping("build-info")
	public ResponseEntity<MicroserviceBuildInformation> getBuildInformation() {
		MicroserviceBuildInformation buildInfo = new MicroserviceBuildInformation(
				buildProperties.getVersion(),
				buildProperties.get("commit-hash")
		);

		return ResponseEntity.ok(buildInfo);
	}

}
