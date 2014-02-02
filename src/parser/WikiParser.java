package parser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mhjang on 2/2/14.
 */
public class WikiParser {
    public static void main(String[] args) throws TikaException, SAXException, IOException {
        InputStream input = new FileInputStream("./wikitest.html");
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        new HtmlParser().parse(input, handler, metadata, new ParseContext());
        String plainText = handler.toString();
        System.out.println(plainText);
    }


}
