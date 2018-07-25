/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.util;

import de.uros.citlab.languagemodel.lmtypes.ILM;
import eu.transkribus.interfaces.ITokenizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class Perplexity {

    public static double calcPerplexity(ILM lm, List<List<String>> texts) {
        double sum = 0.0;
        int count = 0;
        for (List<String> text : texts) {
            sum -= lm.rateSentence(text, true);
            count += text.size() + 1;
        }
        return Math.pow(2, sum / count / Math.log(2));
    }

    public static double calcPerplexity(ILM lm, String[] path2Files, ITokenizer tok) throws IOException {
        LinkedList<List<String>> allLines = new LinkedList<List<String>>();
        for (String path2File : path2Files) {
            List<String> lines = Files.readAllLines(Paths.get(path2File));
            for (String line : lines) {
                List<String> tokenize = tok.tokenize(line);
                allLines.add(tokenize);
            }
        }
        return calcPerplexity(lm, allLines);
    }
}
