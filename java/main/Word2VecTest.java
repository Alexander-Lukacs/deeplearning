package main;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import src.com.nytlabs.corpus.*;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public class Word2VecTest {

    private NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();

    private static Logger log = LoggerFactory.getLogger(Word2VecTest.class);

    private String word = "the";

    public static void main(String args[]) {
        new Word2VecTest().start();
    }

    private void start() {
        try {
            for (int jahr = 1987; jahr <= 1987; jahr++)
                dateiSchreiben(dateiEinlesen(jahr), jahr);

            word2Vec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dateiSchreiben(File[] allFiles, int jahr) {
        FileWriter writer;
        File zielDatei = new File("C:/Users/Besitzer/IdeaProjects/deeplearning1/data/ziel_" + jahr + ".txt");
        StringBuilder stringBuilder = new StringBuilder();
        NYTCorpusDocument document;

        if (allFiles != null) {
            System.out.println("Textdokument erstellt");
            try {
                writer = new FileWriter(zielDatei);
                for (int i = 0; i < allFiles.length; i++) {

                    document = parser.parseNYTCorpusDocumentFromFile(allFiles[i], false);

                    stringBuilder.append("Title: " + document.getHeadline() + "\n");
                    stringBuilder.append(document.getBody());
                    String string = stringBuilder.toString();

                    // Pipeline konfigurieren
                    Properties properties = new Properties();
                    properties.put("annotators", "tokenize, ssplit");
                    StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

                    // Zu annotierender Text
                    Annotation annotation = new Annotation(string);
                    pipeline.annotate(annotation);

                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        writer.write(sentence.toString());
                    }
                }
                writer.close();
                System.out.println("In Textdokument geschrieben");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File[] dateiEinlesen(int jahr) {
        File file;
        File[] allFiles = null;

        System.out.println("Start Datei einlesen");
        for (int monat = 1; monat <= 12; monat++) {
            for (int tag = 1; tag <= 31; tag++) {
                if (monat <= 9) {
                    if (tag <= 9) {
                        file = new File("data/" + jahr + "/0" + monat + "/0" + tag);
                    } else {
                        file = new File("data/" + jahr + "/0" + monat + "/" + tag);
                    }
                } else if (tag <= 9) {
                    file = new File("data/" + jahr + "/" + monat + "/0" + tag);
                } else {
                    file = new File("data/" + jahr + "/" + monat + "/" + tag);
                }
                allFiles = file.listFiles();
            }
        }
        return allFiles;
    }

    private void word2Vec() throws Exception {//NYTCorpusDocument corpusDocument) {
        // Gets Path to Text file
        String filePath = new File("data/ziel_1987.txt").getAbsolutePath();
        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        //SentenceIterator iter = new BasicLineIterator(filePath);

        //InputStream inputStream = new ByteArrayInputStream(corpusDocument.getBody().getBytes());

        SentenceIterator iter = new BasicLineIterator(filePath);

        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");
        Collection<String> lst = vec.wordsNearestSum(word, 10);
        log.info("10 Words closest to '" + word + "': {}", lst);

        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());
    }
}
