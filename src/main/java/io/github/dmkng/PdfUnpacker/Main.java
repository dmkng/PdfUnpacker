package io.github.dmkng.PdfUnpacker;

import com.itextpdf.text.pdf.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) {
		if(args.length != 1 && args.length != 2) {
			System.err.println("Usage: java -jar PdfUnpacker.jar file.pdf [password]");
		} else {
			try {
				PdfReader reader = new PdfReader(args[0], args.length == 2 ? args[1].getBytes() : null);
				PdfDictionary root = reader.getCatalog();
				PdfDictionary names = root.getAsDict(PdfName.NAMES);
				if(names != null) {
					PdfDictionary embeddedFilesDict = names.getAsDict(PdfName.EMBEDDEDFILES);
					if(embeddedFilesDict != null) {
						PdfArray embeddedFiles = embeddedFilesDict.getAsArray(PdfName.NAMES);
						if(embeddedFiles != null) {
							int len = embeddedFiles.size();
							for(int i = 0; i < len; i += 2) {
								PdfString pdfName = embeddedFiles.getAsString(i);
								if(pdfName != null) {
									PdfDictionary fileSpec = embeddedFiles.getAsDict(i + 1);
									if(fileSpec != null) {
										PdfDictionary streams = fileSpec.getAsDict(PdfName.EF);
										if(streams != null) {
											PRStream stream = (PRStream)streams.getAsStream(streams.contains(PdfName.UF) ? PdfName.UF : PdfName.F);
											if(stream != null) {
												String name = pdfName.toUnicodeString();
												System.out.printf("Extracting '%s' ...\n", name);
												Files.write(Paths.get(name), PdfReader.getStreamBytes(stream));
											}
										}
									}
								}
							}
							System.out.println("Done.");
							return;
						}
					}
				}
				System.out.println("Nothing to extract.");
			} catch(IOException | RuntimeException e) {
				System.err.printf("Error: %s\n", e.getLocalizedMessage());
			}
		}
	}
}
