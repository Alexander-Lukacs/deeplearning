package main;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentenceSplittingExample {

    public static void main(String[] args) {

        StringBuilder stringBuilder = new StringBuilder();
        String text = "By the time American negotiators wrapped up high-level talks with a visiting Chinese " +
                "delegation last week, President Trump’s ambitions for a multibillion-dollar trade agreement had, " +
                "for the time being, shriveled into a blandly worded communiqué without any dollar figures. It was not clear that " +
                "the talks set a path to success. Ceaseless infighting and jockeying for influence on the White House’s trade team helped deprive " +
                "Mr. Trump of a quick victory on his most cherished policy agenda, several people involved in the talks said. The deep internal " +
                "divisions carried over into how officials characterized the agreement and muddied the outlook for the next phase of the negotiations between Washington and Beijing.";

        // Pipeline konfigurieren
        Properties properties = new Properties();
        properties.put("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        // Zu annotierender Text
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            /*for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.println(token.get(CoreAnnotations.TextAnnotation.class).toLowerCase());
            }*/
            stringBuilder.append(sentence.toString().toLowerCase()).append("\n");
            System.out.println("<EOS>");
        }
        System.out.println(stringBuilder);

    }
}
