package src.com.nytlabs.corpus;

import java.io.File;



public class Start {

    private NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();

    public static void main(String args[] ){
        new Start().start();
    }

    private void start(){
        File file = new File("data/1987/01/01/0000000.xml");
        NYTCorpusDocument document = parser.parseNYTCorpusDocumentFromFile(file,false);

        System.out.println(document.toString());

    }
}
