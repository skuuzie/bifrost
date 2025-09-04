package com.bifrost.demo.dto.model;

import com.bifrost.demo.exception.DocuPDFException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

import static com.bifrost.demo.util.CryptoUtil.hashStandardData;
import static com.bifrost.demo.util.CryptoUtil.hexlify;

public class DocuPDF {
    private PDDocument doc;
    private String identifier;
    private String rawText;

    private DocuPDF(PDDocument document) {
        this.doc = document;
    }

    public static DocuPDF load(InputStream input) throws DocuPDFException, IOException {
        try {
            byte[] rawBytes = input.readAllBytes();
            PDDocument doc = Loader.loadPDF(rawBytes);

            if (doc.isEncrypted()) {
                throw new DocuPDFException("Encrypted PDF is not supported.");
            }

            if (doc.getNumberOfPages() > 2) {
                throw new DocuPDFException("Max PDF page is 2.");
            }

            DocuPDF _doc = new DocuPDF(doc);
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(_doc.getDoc());

            if (rawText.length() > 5000) {
                throw new DocuPDFException("PDF text content is too big.");
            }

            _doc.setIdentifier(hexlify(hashStandardData(rawBytes)));
            _doc.setRawText(rawText);

            return _doc;
        } catch (IOException ex) {
            throw ex;
        } catch (DocuPDFException ex) {
            throw new DocuPDFException(ex.toString());
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public PDDocument getDoc() {
        return doc;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
