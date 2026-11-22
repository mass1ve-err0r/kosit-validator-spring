# XRechnung Validator
> Java 25 • Spring 7 • Vue 3

A lightweight, self-hosted XRechnung validation service built with Java, Spring Boot, Vue 3 and shadcn-vue.

Uses the official [KoSIT Validator](https://github.com/itplr-kosit/validator) and a custom fork of [XRechnung Visualization](https://github.com/itplr-kosit/xrechnung-visualization), namely:
- validator-configuration-xrechnung: https://github.com/mass1ve-err0r/kosit-validator-configuration-xrechnung

## Features

- Validation of XRechnung documents (UBL and CII)
- Detailed HTML validation reports
- PDF visualization for accepted invoices
- Web interface with HTML/PDF preview and downloads
- Public REST API


## Build & Run
Requires Java and Node.js.

```bash
./gradlew :application:bootRun
```

Build the executable JAR:

```bash
./gradlew :application:bootJar
```

The web interface is available at `http://localhost:8090/ui/`.

## REST API

**POST** `/api/validation`

Submit the invoice XML as the raw request body:

```bash
curl -X POST http://localhost:8090/api/validate \
    -H "Content-Type: application/xml" \
    --data-binary @invoice.xml
```

The JSON response contains the validation recommendation, processing errors, an HTML report and, for successfully accepted invoices, a Base64-encoded PDF.

## Acknowledgements
Built using the open-source KoSIT XML Validator and XRechnung resources. This project is not an official service of KoSIT.
