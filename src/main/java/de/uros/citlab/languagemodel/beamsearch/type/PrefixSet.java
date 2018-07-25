/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import com.achteck.misc.types.ConfMat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author tobias
 */
public class PrefixSet implements SortedPrefixSet {

    private final TreeSet<Token> set;
    private final HashMap<String, Token> map;
    private final int numberOfToks2Keep;
    private final int numberOfToks2Expand;
    private final HashMap<Token, TreeSet<Token>> map2;

    public PrefixSet(int numberOfToks2Keep, int numberOfToks2Expand) {
        set = new TreeSet<>();
        map = new HashMap<String, Token>();
        map2 = new HashMap<Token, TreeSet<Token>>();
        this.numberOfToks2Keep = numberOfToks2Keep;
        this.numberOfToks2Expand = numberOfToks2Expand;
    }

    @Override
    public boolean put(TreeKnot wordKnot, String text, List<String> hist, Token preTok, double costChar, double costNac, int pos) {
        String key = getKey(wordKnot, text);
        Token tok = map.get(key);
        if (doExpand(preTok, costChar, costNac)) {
            if (tok == null) {
                tok = new Token(wordKnot, text, hist, preTok, costChar, costNac, pos);
                map.put(key, tok);
                set.add(tok);
                if (set.size() > numberOfToks2Keep) {
                    Token pollLast = set.pollLast();
                    key = getKey(pollLast.getWordKnot(), pollLast.getText());
                    map2.get(preTok).add(tok);
                    map.remove(key);
                }
            } else if (costChar < tok.getCosts() || costNac < tok.getCosts()) {
                key = tok.isFinalLetter() ? tok.getText() + "E" : tok.getText();
                map.remove(key);
                tok.update(preTok, costChar, costNac);
                map2.get(preTok).add(tok);
                set.add(tok);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        set.clear();
        map.clear();
        map2.clear();
    }

    @Override
    public Collection<Token> getMostLikelyToks() {
        return new ArrayList<>(set);
    }

    @Override
    public Collection<Token> values() {
        return new ArrayList<>(set);
    }

    @Override
    public double getCostUpperBound() {
        if (set.size() < numberOfToks2Keep) {
            return Double.MAX_VALUE;
        }
        return set.last().getCosts();
    }

    private boolean doExpand(Token tk, double costChar, double costNac) {
        TreeSet<Token> get = map2.get(tk);
        if (get == null) {
            map2.put(tk, new TreeSet<Token>());
            return true;
        }
        if (get.size() < numberOfToks2Expand) {
            return true;
        }
        double costs = get.last().getCosts();
        if (costs > costChar || costs > costNac) {
            Token pollLast = get.pollLast();
            map.remove(pollLast);
            set.remove(pollLast);
            return true;
        }
        return false;
    }

    private String getKey(TreeKnot wordKnot, String text) {
        return wordKnot.isFinalLetter() ? text + ConfMat.NaC : text;
    }

}
