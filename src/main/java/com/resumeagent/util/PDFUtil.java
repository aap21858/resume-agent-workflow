package com.resumeagent.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PDFUtil {
    
    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 11;
    private static final float LEADING = 14;
    
    /**
     * Extract text content from a PDF file
     */
    public String extractText(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * Extract text content from a PDF input stream
     */
    public String extractText(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * Generate a PDF document from text content
     */
    public void generatePDF(String content, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Split content into lines that fit the page
            List<String> lines = splitTextIntoLines(content);
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
                
                float yPosition = page.getMediaBox().getHeight() - MARGIN;
                
                for (String line : lines) {
                    // Check if we need a new page
                    if (yPosition < MARGIN) {
                        contentStream.endText();
                        contentStream.close();
                        
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        
                        PDPageContentStream newContentStream = new PDPageContentStream(document, page);
                        newContentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
                        newContentStream.beginText();
                        newContentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                        
                        newContentStream.showText(line);
                        newContentStream.newLineAtOffset(0, -LEADING);
                        yPosition -= LEADING;
                        continue;
                    }
                    
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -LEADING);
                    yPosition -= LEADING;
                }
                
                contentStream.endText();
            }
            
            document.save(outputFile);
        }
    }
    
    private List<String> splitTextIntoLines(String text) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }
            
            // Simple word wrapping - approximately 80 characters per line
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            
            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > 80) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }
            
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        
        return lines;
    }
}
