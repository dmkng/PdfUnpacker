package io.github.dmkng.PdfUnpacker;

import com.itextpdf.text.pdf.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfUnpacker {
	public static void main(final String[] args) {
		if(args.length != 1 && args.length != 2) {
			System.err.println("Usage: java -jar PdfUnpacker.jar <file> [password]");
			return;
		}

		try {
			final PdfReader reader = new PdfReader(args[0], args.length == 2 ? args[1].getBytes() : null);
			final PdfDictionary root = reader.getCatalog();
			final PdfArray embeddedFiles = PdfUnpacker.getEmbeddedFiles(root);
			if(embeddedFiles == null) {
				System.out.println("Nothing to extract.");
				return;
			}

			// Iterating through all embedded file entries
			// 1st entry is the file name, 2nd entry is the file data, so we increment by 2
			final int length = embeddedFiles.size();
			for(int i = 0; i < length; i += 2) {
				final PdfString pdfName = embeddedFiles.getAsString(i);
				if(pdfName == null) {
					continue;
				}
				final PdfDictionary fileSpec = embeddedFiles.getAsDict(i + 1);
				if(fileSpec == null) {
					continue;
				}
				final PdfDictionary streams = fileSpec.getAsDict(PdfName.EF);
				if(streams == null) {
					continue;
				}
				final PRStream stream = (PRStream)streams.getAsStream(streams.contains(PdfName.UF) ? PdfName.UF : PdfName.F);
				if(stream != null) {
					final String name = pdfName.toUnicodeString();
					System.out.printf("Extracting '%s' ...\n", name);
					Files.write(Paths.get(name), PdfReader.getStreamBytes(stream));
				}
			}

			System.out.println("Done.");
		} catch(IOException | RuntimeException e) {
			System.err.printf("Error: %s\n", e.getLocalizedMessage());
		}
	}

	private static PdfArray getEmbeddedFiles(final PdfDictionary root) {
		final PdfDictionary names = root.getAsDict(PdfName.NAMES);
		if(names == null) {
			return null;
		}
		final PdfDictionary embeddedFilesDict = names.getAsDict(PdfName.EMBEDDEDFILES);
		if(embeddedFilesDict == null) {
			return null;
		}
		return embeddedFilesDict.getAsArray(PdfName.NAMES);
	}

}
