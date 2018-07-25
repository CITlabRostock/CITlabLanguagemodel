/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import de.uros.citlab.languagemodel.lmtypes.ILM;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class LMCostMapperChar implements ILMCostMapper {

    private final ILM lm;
    private final int numOfWords;
    private final double beta;
    private final double alpha;

    public LMCostMapperChar(ILM lm, double alpha, double beta) {
        this.lm = lm;
        numOfWords = lm.getWords().size();
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double getLMCosts(Token preTok, List<String> hist, TreeKnot nextWordKnot) {
//        if (preTok.getWordKnot() == nextWordKnot) {
//            return 0;
//        }
        LinkedList<String> tmp = new LinkedList<String>(hist);
        tmp.add(String.valueOf(nextWordKnot.c));
        return beta - alpha * lm.getLogProb(tmp);
    }

}
