package fr.onagui.alignment.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.NoMappingPossible;
import fr.onagui.alignment.OntoContainer;

public class CSVImpl implements IOAlignment {
	
	private static final String TITLES = "Fragment 1;"+
			"Fragment 2;"+
			"Alignement;"+
			"Preflabels 1;"+
			"Preflabels 2;"+
			"Altlabels 1;"+
			"Altlabels 2;"+
			"Method;"+
			"Validity;"+
			"Date";
	
	private static final DateTimeFormatter TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
	
	@Override
	public AlignmentFormat getFormat() {
		return AlignmentFormat.CSV;
	}

	@Override
	public <ONTORES1, ONTORES2> Alignment<ONTORES1, ONTORES2> loadAlignment(
			OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2,
			File file) throws IOException {

		System.out.println("Chargement à partir du fichier.");

		// Un reader sur le fichier CSV
		BufferedReader reader = Files.newBufferedReader(Paths.get(file.toURI()), ONAGUI_CHARSET);

		// Un peu de trifouillage ontologie
		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(
				onto1, onto2);

		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.equals(TITLES)) continue;
			String[] scan = line.split(";");
			if (scan.length == 0)
				continue; // Ligne vide, pourquoi pas...
			
			ONTORES1 res1 = null;
			try {
				res1 = onto1.getConceptFromURI(URI.create(scan[0]));
			} catch(Exception e) {}
			ONTORES2 res2 = null;
			try {
				res2 = onto2.getConceptFromURI(URI.create(scan[1]));;
			} catch(Exception e) {}

			// La case 3, type d'alignement
			if(scan[2].equals(CANNOT_BE_ALIGNED)) {
				if(res1 != null && res2 == null) {
					alignment.addImpossibleMappingFrom1(new NoMappingPossible<ONTORES1>(res1));
					continue;
				}
				else if(res1 == null && res2 != null) {
					alignment.addImpossibleMappingFrom2(new NoMappingPossible<ONTORES2>(res2));
					continue;
				}
			}
			if(res1 == null || res2 == null) {
				System.err.println("Invalid line: " + line);
				continue;
			}

			// La case 3, type d'alignement
			// by default, the undefined type
			MAPPING_TYPE readedType = (scan.length>=3)?extractAlignmentType(scan[2]):MAPPING_TYPE.UNDEFINED;
			// Skip labels (4 cases)
			String method = (scan.length>=8)?scan[7]:Mapping.UNKNOW_METHOD;
			double score = (scan.length>=9)?Double.valueOf(scan[8]):1.0;
			VALIDITY validity = (scan.length>=10)?VALIDITY.valueOf(scan[9]):VALIDITY.VALID;
			DateTime date = (scan.length>=11)?TIME_FORMATTER.parseDateTime(scan[10]):DateTime.now();

			Mapping<ONTORES1, ONTORES2> map = new Mapping<ONTORES1, ONTORES2>(
					res1, res2, score, readedType, method, validity, date);
			alignment.addMap(map);
		}
		reader.close();
		return alignment;
	}
	
	private static MAPPING_TYPE extractAlignmentType(String alignMethod) {
		try {
			return MAPPING_TYPE.getTypeFromString(alignMethod);
		} catch (Exception e) {}
		return MAPPING_TYPE.EQUIV;
	}

	@Override
	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException {

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(pathToSave), ONAGUI_CHARSET);
		OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		OntoContainer<ONTORES2> onto2 = alignment.getOnto2();
		StringBuilder buf = null;

		writer.write(TITLES);
		writer.newLine();
		
		for (Mapping<ONTORES1, ONTORES2> map : alignment.getMapping()) {
			if(validityWanted != null && !map.getValidity().equals(validityWanted))
				continue;
			buf = new StringBuilder();
			final ONTORES1 firstConcept = map.getFirstConcept();
			final ONTORES2 secondConcept = map.getSecondConcept();
			// First concept fragment
			buf.append(onto1.getURI(firstConcept).toString());
			buf.append(';');
			// Second concept fragment
			buf.append(onto2.getURI(secondConcept).toString());
			buf.append(';');
			// Alignement symbol
			buf.append(map.getType().name());
			buf.append(';');
			// Preflabels of concept 1
			buf.append(CSVImpl.join(onto1.getPrefLabels(firstConcept), ","));
			buf.append(";");
			// Preflabels of concept 2
			buf.append(CSVImpl.join(onto2.getPrefLabels(secondConcept), ","));
			buf.append(";");
			// Altlabels of concept 1
			buf.append(CSVImpl.join(onto1.getAltLabels(firstConcept), ","));
			buf.append(";");
			// Altlabels of concept 2
			buf.append(CSVImpl.join(onto2.getAltLabels(secondConcept), ","));
			buf.append(";");
			// Method
			buf.append(map.getMethod());
			buf.append(";");
			// Score
			buf.append(map.getScore());
			buf.append(";");
			// Validity
			buf.append(map.getValidity());
			buf.append(";");
			// Date
			buf.append(TIME_FORMATTER.print(map.getCreationDate()));
			// Writing this line
			writer.write(buf.toString());
			writer.newLine();
		}
		for(NoMappingPossible<ONTORES1> nomap :	alignment.getImpossibleMapping1().values()) {
			buf = new StringBuilder();
			buf.append(onto1.getURI(nomap.getConcept()).toString());
			buf.append(";;");
			buf.append(CANNOT_BE_ALIGNED);
			writer.write(buf.toString());
			writer.newLine();
		}
		for(NoMappingPossible<ONTORES2> nomap :	alignment.getImpossibleMapping2().values()) {
			buf = new StringBuilder();
			buf.append(";");
			buf.append(onto2.getURI(nomap.getConcept()).toString());
			buf.append(";");
			buf.append(CANNOT_BE_ALIGNED);
			writer.write(buf.toString());
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

	public static String join(Iterable<? extends Object> pColl, String separator) {
		Iterator<? extends Object> oIter;
		if (pColl == null || (!(oIter = pColl.iterator()).hasNext()))
			return "";
		StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
		while (oIter.hasNext())
			oBuilder.append(separator).append(oIter.next());
		return oBuilder.toString();
	}

}
