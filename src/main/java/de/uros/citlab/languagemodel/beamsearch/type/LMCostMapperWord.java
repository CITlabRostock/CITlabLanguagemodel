/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import com.achteck.misc.types.Pair;
import de.uros.citlab.languagemodel.lmtypes.ILM;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class LMCostMapperWord implements ILMCostMapper {

    private final ILM lm;
    private final HashMap<Pair<List<String>, TreeKnot>, Double> map;
    private final int numOfWords;
    private double beta;
    private double alpha;
    private final TreeKnot root;

    public LMCostMapperWord(ILM lm, double alpha, double beta, TreeKnot root) {
        this.lm = lm;
        numOfWords = lm.getWords().size();
        map = new HashMap<>();
        this.alpha = alpha;
        this.beta = beta;
        this.root = root;
    }

    @Override
    public double getLMCosts(Token preTok, List<String> hist, TreeKnot nextWordKnot) {
        if ( //                preTok.getWordKnot() == nextWordKnot || 
                nextWordKnot.c == ' ') {
            return 0;
        }

        double nom = getSumOfPrefProbs(hist, nextWordKnot);
        double denom;
        if (preTok.isFinalLetter() || (preTok.getWordKnot() instanceof TreeKnotSpace) || (preTok.getWordKnot().getIdx() < 0)) {
            denom = -beta;
        } else {
            denom = getSumOfPrefProbs(preTok.getHist(), preTok.getWordKnot());
        }

        return nom - denom;
    }

    private double getSumOfPrefProbs(List<String> hist, TreeKnot wordKnot) {
        Pair<List<String>, TreeKnot> key = new Pair<>(hist, wordKnot);
        Double get = map.get(key);
        if (get == null) {
//            wordKnot.getWordPrefix();
            LinkedList<String> tmp = new LinkedList<>(hist);
            double sum = 0;
            for (String word : wordKnot.getWordsWithThisPrefix()) {
                tmp.add(word);
                sum += Math.exp(lm.getLogProb(tmp));
                tmp.pollLast();
            }
            get = alpha * (-Math.log(sum));
            map.put(key, get);
        }
        return get;
    }

}
