package org.deeplearning4j.examples.nlp.word2vec;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.com.nytlabs.corpus.NYTCorpusDocument;
import src.com.nytlabs.corpus.NYTCorpusDocumentParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by agibsonccc on 10/9/14.
 * <p>
 * Neural net that processes text into wordvectors. See below url for an in-depth explanation.
 * https://deeplearning4j.org/word2vec.html
 */
public class Word2VecRawTextExample {

    private static Logger log = LoggerFactory.getLogger(Word2VecRawTextExample.class);

    private NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();

    private String word = "the";

    private void start() {
        int i = 0;
        File file;
        while (i<=231) {
            try {
            if (i <=9) {
                file = new File("data/000000" + i + ".xml");
            }
            else if (i <=99) {
                file = new File("data/00000" + i + ".xml");
            }else {
                    file = new File("data/00000" + i + ".xml");
                }
                System.out.println(file.getAbsolutePath());
                word2Vec(parser.parseNYTCorpusDocumentFromFile(file, false));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            i = i + 1;
        }

    }


    private void word2Vec(NYTCorpusDocument corpusDocument){
        // Gets Path to Text file
        //String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();
        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        //SentenceIterator iter = new BasicLineIterator(filePath);

        InputStream inputStream = new ByteArrayInputStream(corpusDocument.getBody().getBytes());

        SentenceIterator iter = new BasicLineIterator(inputStream);

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

    public static void main(String[] args) {
        new Word2VecRawTextExample().start();
    }
}
