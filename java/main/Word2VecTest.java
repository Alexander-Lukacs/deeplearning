package main;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import src.com.nytlabs.corpus.*;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Word2VecTest {

    private NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();

    private static Logger log = LoggerFactory.getLogger(Word2VecTest.class);

    private CreateDB createDB = new CreateDB();

    public static void main(String args[]) {
        new Word2VecTest().start();
    }

    private String word = "train";

    private void start() {

        for (int jahr = 1987; jahr <= 2007; jahr++) {
            try {
                //schreiben(jahr);
                word2Vec(jahr);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void word2Vec(int jahr) throws Exception {

        // Gets Path to Text file
        String filePath = new File("data/ziel_" + jahr + ".txt").getAbsolutePath();
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

        System.out.println("Word2Vec starten");
        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(10)
                .iterations(1)
                .layerSize(100)
                //.layerSize(50)
                .seed(42)
                .windowSize(5)
                .useHierarchicSoftmax(true)
                .allowParallelTokenization(true)
                .workers(4)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");

        vec.fit();

        log.info("Writing word vectors to text file....");

        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");

        Collection<String> lst = vec.wordsNearestSum(word, 10);
        System.out.println("10 Words closest to '" + word + "': { " + lst + " }");

        System.out.println("Werte in Datenbank speichern:");

        VocabCache<VocabWord> v = vec.getVocab();
        System.out.println(v.vocabWords().size());
        createDB.createConnection();

        for (VocabWord w : v.vocabWords()) {
            System.out.println(w.getWord() + " Jahr:" + jahr);
            double[] r = vec.getWordVector(w.getWord());
            for (int i = 0; i < r.length; i++) {
                System.out.println("Dimension: " + i + " und Wert " + r[i]);
                createDB.insert(jahr, w.getWord(), i, r[i]);
            }
        }
        createDB.closeConnection();

        log.info("10 Words closest to '" + word + "': {}", lst);

        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());
    }


    private static void traverse(File root, List<File> files) {
        if (root.isFile()) {
            if (root.getAbsolutePath().endsWith(".xml")) {
                files.add(root);
            }
        } else {
            for (File file : root.listFiles()) {
                traverse(file, files);
            }
        }
    }


    public void schreiben(int jahr) throws Exception {

        Properties properties = new Properties();
        properties.put("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        String inputPath = "data/" + jahr;
        //String inputPath = "/home/alukacs/deeplearning2/data/"+jahr;
        String outputPath = "data/ziel_" + jahr + ".txt";
        //String outputPath = "/home/alukacs/deeplearning2/data/ziel_" + jahr + ".txt";

        LinkedList<File> files = new LinkedList<File>();
        traverse(new File(inputPath), files);

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));

        int cnt = 0;

        for (File file : files) {
            NYTCorpusDocument doc = parser.parseNYTCorpusDocumentFromFile(file, false);
            String content = doc.getHeadline() + " " + doc.getBody();


            // Zu annotierender Text
            Annotation annotation = new Annotation(content);
            pipeline.annotate(annotation);

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    bw.write(token.get(CoreAnnotations.TextAnnotation.class).toLowerCase() + " ");
                }
                bw.write("\n");
            }

            if (++cnt % 1000 == 0) System.out.println(cnt);
        }

        bw.close();
    }
}
