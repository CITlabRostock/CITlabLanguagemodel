/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import de.planet.citech.types.IDecodingType;
import de.planet.citech.types.IDecodingType.Label;
import de.planet.citech.types.ISortingFunction;
import de.planet.decoding.types.MatchWord;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class CTCBeamSearchResult implements IDecodingType {

    private final String text;
    private final double costs;
    private int T;
    List<MatchWord> tokens;

    public CTCBeamSearchResult(Token tok) {
        costs = tok.getCosts();
        text = tok.getText();
        tokens = new LinkedList<>();
        fillWordList(tokens, tok);

    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getCollapsedBP() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public int getEnd() {
        return T;
    }

    @Override
    public double getCostAbs() {
        return costs;
    }

    @Override
    public double getSortingCosts() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSortingCosts(ISortingFunction isf, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getCostAbsBestPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Label getLabel() {
        return Label.UNDEFINED;
    }

    public Collection<? extends IDecodingType> getTokens() {
        return tokens;
    }

    private void fillWordList(List<MatchWord> words, Token tok) {
        Token pre;
        T = tok.getPos()+1;
        Token tmp = tok;
        String lastWord = tok.getWordKnot() instanceof TreeKnotSpace ? " " : tok.getLastWord();
        double cost = tok.getCosts();
        int lastEndPos = T;
        int pos = T;
        while ((pre = tok.getPreviousToken()) != null && pos > 0) {
            if (pre.getWordKnot() != tok.getWordKnot() && (pre.isFinalLetter() || pre.getWordKnot() instanceof TreeKnotSpace)) {
                MatchWord matchWord = new MatchWord();
                matchWord.set(lastWord, pos, lastEndPos, null, cost - pre.getCosts(), 0, Label.UNDEFINED);
                words.add(matchWord);

                lastEndPos = pos;
                cost = pre.getCosts();
                lastWord = pre.getWordKnot() instanceof TreeKnotSpace ? " " : pre.getLastWord();

            }
            tok = pre;
            pos--;
        }
        MatchWord matchWord = new MatchWord();
        matchWord.set(lastWord, pos, lastEndPos, null, cost, 0, Label.UNDEFINED);
        words.add(matchWord);
        Collections.reverse(words);
    }

    @Override
    public String toString() {
        return "CTCBeamSearchResult{" + "text=" + text + ", costs=" + costs + ", [0," + T + "]}";
    }

}
