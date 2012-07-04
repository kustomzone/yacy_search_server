/**
 *
 */
package net.yacy.document.parser.rdfa.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.yacy.document.AbstractParser;
import net.yacy.document.Document;
import net.yacy.document.Parser;
import net.yacy.document.parser.htmlParser;
import net.yacy.document.parser.rdfa.IRDFaTriple;
import net.yacy.kelondro.data.meta.DigestURI;
import net.yacy.kelondro.logging.Log;

/**
 * @author fgandon
 *
 */
public class RDFaParser extends AbstractParser implements Parser {

    private final htmlParser hp;

	public RDFaParser() {
		super("RDFa Parser");
		this.hp = new htmlParser();

		this.SUPPORTED_EXTENSIONS.add("html");
		this.SUPPORTED_EXTENSIONS.add("php");
		this.SUPPORTED_MIME_TYPES.add("text/html");
		this.SUPPORTED_MIME_TYPES.add("text/xhtml+xml");
		this.SUPPORTED_EXTENSIONS.add("html");
		this.SUPPORTED_EXTENSIONS.add("htm");
	}

	@Override
    public Document[] parse(DigestURI url, String mimeType,
			String charset, InputStream source) throws Failure,
			InterruptedException {

		Document[] htmlDocs = parseHtml(url, mimeType, charset, source);

		// TODO: current hardcoded restriction: apply rdfa parser only on selected sources.

		if (url.toString().contains(".yacy") || url.toString().contains("experiments")) {
		// if (true == false) {
			Document rdfaDoc = parseRDFa(url, mimeType, charset, source);
			Document[] retDocs = new Document[htmlDocs.length + 1];
			for (int i = 0; i < htmlDocs.length; i++) {
				retDocs[i] = htmlDocs[i];
			}
			retDocs[retDocs.length - 1] = rdfaDoc;
			return retDocs;
		} else {
			return htmlDocs;
		}

	}

	private Document parseRDFa(DigestURI url, String mimeType,
			String charset, InputStream source) {
		RDFaTripleImpl triple;
		IRDFaTriple[] allTriples = null;
		try {
			triple = new RDFaTripleImpl(new InputStreamReader(source), url
					.toString());
			allTriples = triple.parse();

		} catch (Exception e) {
			Log.logWarning("RDFA PARSER", "Triple extraction failed");
		}

		Document doc = new Document(url, mimeType, charset, null, null, null, "", "",
				"", null, "", 0, 0, null, null, null, null, false);

		try {
			if (allTriples.length > 0)
				doc = convertAllTriplesToDocument(url, mimeType, charset,
						allTriples);

		} catch (Exception e) {
			Log.logWarning("RDFA PARSER",
					"Conversion triple to document failed");
		}
		return doc;

	}

	private Document[] parseHtml(DigestURI url, String mimeType,
			String charset, InputStream source) throws Failure,
			InterruptedException {

		Document[] htmlDocs = null;
		try {
			htmlDocs = this.hp.parse(url, mimeType, charset, source);
			source.reset();

		} catch (IOException e1) {
			Log.logWarning("RDFA PARSER", "Super call failed");
		}
		return htmlDocs;

	}

	private Document convertAllTriplesToDocument(DigestURI url,
			String mimeType, String charset, IRDFaTriple[] allTriples) {

		//Set<String> languages = new HashSet<String>(2);
		Set<String> keywords = new HashSet<String>(allTriples.length);
		//Set<String> sections = new HashSet<String>(5);
		String all = "";

		for (IRDFaTriple irdFaTriple : allTriples) {
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getLanguage());
			// addNotEmptyValuesToSet(keywords,
			// irdFaTriple.getSubjectNodeURI());
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getSubjectURI());
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getPropertyURI());
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getObjectNodeURI());
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getObjectURI());
			// addNotEmptyValuesToSet(keywords, irdFaTriple.getValue());
			addNotEmptyValuesToSet(keywords, irdFaTriple.getPropertyURI() + "Z"
					+ irdFaTriple.getValue());
		}
		for (String string : keywords) {
			string = string.replace(":", "X");
			string = string.replace("_", "Y");
			string = string.replace(" ", "Y");
			string = string.replace(".", "Y");
			string = string.replace(",", "Y");
			all += string + ",";
		}

		Document doc = new Document(url, mimeType, charset, null, null, null, "", "",
				"", null, "", 0, 0, all, null, null, null, false);
		return doc;
	}

	private void addNotEmptyValuesToSet(Set<String> set, String value) {
		if (value != null) {
			set.add(value);
		}
	}

	public static void main(String[] args) {
        URL aURL = null;
        if (args.length < 1) {
            System.out
                    .println("Usage: one and only one argument giving a file path or a URL.");
        } else {
            File aFile = new File(args[0]);
            Reader aReader = null;
            if (aFile.exists()) {
                try {
                    aReader = new FileReader(aFile);
                } catch (FileNotFoundException e) {
                    aReader = null;
                }
            } else {
                try {
                    aURL = new URL(args[0]);
                    aReader = new InputStreamReader(aURL.openStream());
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                    aReader = null;
                }

            }

            if (aReader != null) {
                RDFaParser aParser = new RDFaParser();
                try {
                    aParser.parse(new DigestURI(args[0]),"","",aURL.openStream());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Failure e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                System.out.println("File or URL not recognized.");

        }

    }
}