package software.baig.oss.kositspring.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xmlgraphics.util.MimeConstants;


@Service
public class XRechnungPDFService {

    private static final Logger LOG = LoggerFactory.getLogger(XRechnungPDFService.class);

    private static final String ROOT = "kosit-visualization/";
    private static final String BASE_URI = "classpath:/" + ROOT;

    private static final String UBL_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String UBL_CREDITNOTE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
    private static final String CII = "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100";

    private final Templates invoiceTemplate;
    private final Templates creditNoteTemplate;
    private final Templates ciiTemplate;
    private final Templates pdfTemplate;
    private final FopFactory fopFactory;


    public XRechnungPDFService() throws TransformerConfigurationException {
        TransformerFactoryImpl factory = new TransformerFactoryImpl();
        factory.setURIResolver(this::resolve);

        invoiceTemplate = load(factory, "ubl-invoice-xr.xsl");
        creditNoteTemplate = load(factory, "ubl-creditnote-xr.xsl");
        ciiTemplate = load(factory, "cii-xr.xsl");
        pdfTemplate = load(factory, "xr-pdf.xsl");

        fopFactory = FopFactory.newInstance(URI.create("file:/"));
    }

    public byte[] generate(byte[] xml) throws TransformerException, XMLStreamException, FOPException {
        Templates inputTemplate = selectTemplate(xml);

        Transformer toXr = inputTemplate.newTransformer();
        toXr.setURIResolver(this::resolve);

        ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
        toXr.transform(
            new StreamSource(new ByteArrayInputStream(xml)),
            new StreamResult(intermediate)
        );

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdf);

        Transformer toFo = pdfTemplate.newTransformer();
        toFo.setURIResolver(this::resolve);
        toFo.setParameter("foengine", "fop");

        toFo.transform(
            new StreamSource(new ByteArrayInputStream(intermediate.toByteArray())),
            new SAXResult(fop.getDefaultHandler())
        );

        return pdf.toByteArray();
    }

    private Templates selectTemplate(byte[] xml) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));

        try {
            reader.nextTag();
            QName root = reader.getName();

            if (root.equals(new QName(UBL_INVOICE, "Invoice"))) {
                return invoiceTemplate;
            }

            if (root.equals(new QName(UBL_CREDITNOTE, "CreditNote"))) {
                return creditNoteTemplate;
            }

            if (root.equals(new QName(CII, "CrossIndustryInvoice"))) {
                return ciiTemplate;
            }

            throw new IllegalArgumentException("Unsupported invoice document: " + root);
        } finally {
            reader.close();
        }
    }

    private Templates load(TransformerFactoryImpl factory, String filename) throws TransformerConfigurationException {
        String resource = ROOT + filename;

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing visualization stylesheet: " + resource);
            }

            return factory.newTemplates(new StreamSource(input, BASE_URI + filename));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load visualization stylesheet: " + resource, exception);
        }
    }

    private Source resolve(String href, String base) throws TransformerException {
        URI resolved = URI.create(base == null ? BASE_URI : base).resolve(href).normalize();

        if (!resolved.toString().startsWith(BASE_URI)) {
            throw new TransformerException("External stylesheet resource not allowed: " + resolved);
        }

        String resource = resolved.getPath().substring(1);
        InputStream input = getClass().getClassLoader().getResourceAsStream(resource);

        if (input == null) {
            throw new TransformerException("Missing visualization resource: " + resource);
        }

        return new StreamSource(input, resolved.toString());
    }

}
