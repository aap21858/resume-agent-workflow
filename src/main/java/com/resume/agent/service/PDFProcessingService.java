package com.resume.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for PDF processing using Apache PDFBox.
 */
@Service
@Slf4j
public class PDFProcessingService {
    
    private static final int MAX_CHARS_PER_LINE = 90;
    private static final int MAX_LINES_PER_PAGE = 50;
    private static final float FONT_SIZE = 11;
    private static final float LEADING = 14;
    
    /**
     * Extract text content from a PDF file.
     */
    public String extractTextFromPDF(String pdfPath) throws IOException {
        log.debug("Extracting text from PDF: {}", pdfPath);
        
        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.debug("Extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", pdfPath, e);
            throw new IOException("Failed to extract text from PDF: " + pdfPath, e);
        }
    }
    
    /**
     * Generate a PDF file from text content.
     */
    public void generatePDF(String content, String outputPath) throws IOException {
        log.debug("Generating PDF at: {}", outputPath);
        
        try (PDDocument document = new PDDocument()) {
            List<String> lines = splitTextIntoLines(content);
            int totalPages = (int) Math.ceil((double) lines.size() / MAX_LINES_PER_PAGE);
            
            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                
                int startLine = pageNum * MAX_LINES_PER_PAGE;
                int endLine = Math.min(startLine + MAX_LINES_PER_PAGE, lines.size());
                List<String> pageLines = lines.subList(startLine, endLine);
                
                writeTextToPage(document, page, pageLines);
            }
            
            document.save(outputPath);
            log.info("Generated PDF with {} pages at: {}", totalPages, outputPath);
        } catch (IOException e) {
            log.error("Failed to generate PDF: {}", outputPath, e);
            throw new IOException("Failed to generate PDF: " + outputPath, e);
        }
    }
    
    /**
     * Split text into lines that fit within the page width.
     */
    private List<String> splitTextIntoLines(String content) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = content.split("\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }
            
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > MAX_CHARS_PER_LINE) {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                }
                
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
            
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        
        return lines;
    }
    
    /**
     * Write text lines to a PDF page.
     */
    private void writeTextToPage(PDDocument document, PDPage page, List<String> lines) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, page.getMediaBox().getHeight() - 50);
            
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -LEADING);
            }
            
            contentStream.endText();
        }
    }
    
    /**
     * Validate if a file is a valid PDF.
     */
    public boolean isValidPDF(String pdfPath) {
        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {
            return document.getNumberOfPages() > 0;
        } catch (IOException e) {
            log.warn("Invalid PDF file: {}", pdfPath);
            return false;
        }
    }
}
