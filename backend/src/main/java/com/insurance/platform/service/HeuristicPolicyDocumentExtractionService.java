package com.insurance.platform.service;

import com.insurance.platform.dto.document.ExtractionPreviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristic extraction: reads PDF text via PDFBox when available (loaded reflectively
 * so the dependency is optional) and applies regexes for policy number, dates and amounts.
 */
@Service
public class HeuristicPolicyDocumentExtractionService implements PolicyDocumentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(HeuristicPolicyDocumentExtractionService.class);

    private static final Pattern POLICY_NUMBER =
            Pattern.compile("(?i)policy\\s*(?:no|number|#)?\\s*[:\\-]?\\s*([A-Z0-9][A-Z0-9\\-/]{4,})");
    private static final Pattern DATE =
            Pattern.compile("(\\d{4}-\\d{2}-\\d{2})|(\\d{2}/\\d{2}/\\d{4})|(\\d{2}-\\d{2}-\\d{4})");
    private static final Pattern AMOUNT =
            Pattern.compile("(?i)(?:premium|coverage|amount|sum insured)\\s*[:\\-]?\\s*(?:INR|Rs\\.?|\\$)?\\s*([0-9][0-9,]*\\.?[0-9]*)");
    private static final Pattern INSURER =
            Pattern.compile("(?i)([A-Z][A-Za-z& ]{2,40}(?:Insurance|General Insurance|Life Insurance))");

    @Override
    public ExtractionPreviewResponse extract(MultipartFile file) {
        try {
            byte[] content = file != null ? file.getBytes() : new byte[0];
            String filename = file != null ? file.getOriginalFilename() : "upload";
            return extractFromBytes(content, filename);
        } catch (Exception ex) {
            log.warn("Extraction failed: {}", ex.getMessage());
            return empty();
        }
    }

    @Override
    public ExtractionPreviewResponse extractFromBytes(byte[] content, String filename) {
        String text = toText(content, filename);
        ExtractionPreviewResponse preview = new ExtractionPreviewResponse();
        preview.setPolicyNumber(firstGroup(POLICY_NUMBER, text));
        Matcher dates = DATE.matcher(text);
        if (dates.find()) {
            preview.setStartDate(dates.group());
            if (dates.find()) {
                preview.setExpiryDate(dates.group());
            }
        }
        Matcher amounts = AMOUNT.matcher(text);
        if (amounts.find()) {
            preview.setPremiumAmount(amounts.group(1));
            if (amounts.find()) {
                preview.setCoverageAmount(amounts.group(1));
            }
        }
        preview.setInsurerName(firstGroup(INSURER, text));
        preview.setRawTextExcerpt(text.length() > 2000 ? text.substring(0, 2000) : text);
        return preview;
    }

    private String toText(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            return "";
        }
        boolean isPdf = filename != null && filename.toLowerCase().endsWith(".pdf")
                || content.length > 4 && content[0] == '%' && content[1] == 'P'
                && content[2] == 'D' && content[3] == 'F';
        if (isPdf) {
            String pdfText = extractPdfText(content);
            if (!pdfText.isBlank()) {
                return pdfText;
            }
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * Extracts PDF text via PDFBox using reflection so the library remains optional
     * at compile time and runtime. Returns blank when PDFBox is absent.
     */
    private String extractPdfText(byte[] content) {
        try {
            Class<?> loader = Class.forName("org.apache.pdfbox.Loader");
            Method load = loader.getMethod("loadPDF", byte[].class);
            Object document = load.invoke(null, (Object) content);
            try {
                Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
                Object stripper = stripperClass.getDeclaredConstructor().newInstance();
                Method getText = stripperClass.getMethod("getText",
                        Class.forName("org.apache.pdfbox.pdmodel.PDDocument"));
                return String.valueOf(getText.invoke(stripper, document));
            } finally {
                document.getClass().getMethod("close").invoke(document);
            }
        } catch (ClassNotFoundException ex) {
            log.debug("PDFBox not on classpath; falling back to raw bytes");
            return "";
        } catch (Exception ex) {
            log.warn("PDF text extraction failed: {}", ex.getMessage());
            return "";
        }
    }

    private String firstGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return matcher.group(i).trim();
                }
            }
            return matcher.group().trim();
        }
        return null;
    }

    private ExtractionPreviewResponse empty() {
        return new ExtractionPreviewResponse();
    }
}
