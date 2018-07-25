/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class Token implements Comparable<Token> {

    private double costsAccNac;
    private Token preNac;
    private double costsAccChar;
    private Token preChar;
    private List<String> hist;
    private String text;
    private TreeKnot wordKnot;
    private final int pos;

    public Token(TreeKnot nextWordKnot, String text, List<String> hist, Token preTok, double costsChar, double costsNac, int pos) {
        this.costsAccChar = costsChar;
        this.costsAccNac = costsNac;
        this.preChar = preTok;
        this.preNac = preTok;
        wordKnot = nextWordKnot;
        this.text = text;
        this.hist = hist;
        this.pos = pos;
    }

    @Override
    public int compareTo(Token o) {
        if (this == o) {
            return 0;
        }
        if (getCosts() != o.getCosts()) {
            return Double.compare(getCosts(), o.getCosts());
        }

        int compare = Boolean.compare(isAccepting(), o.isAccepting());
        if (compare != 0) {
            return compare;
        }
        compare = Boolean.compare(isFinalLetter(), o.isFinalLetter());
        if (compare != 0) {
            return compare;
        }
        return text.compareTo(o.text);
    }

    public boolean isFinalLetter() {
        return wordKnot.isFinalLetter();
    }

    public boolean isAccepting() {
        return wordKnot.isAccepting();
    }

    public LinkedList<TreeKnot> getNextWordKnotes() {
        return wordKnot.getSuccessors();
    }

    public void update(Token preTok, double costsChar, double costsNac) {
        if (costsChar < this.costsAccChar) {
            this.costsAccChar = costsChar;
            preChar = preTok;
        } else if (costsNac < this.costsAccNac) {
            this.costsAccNac = costsChar;
            preNac = preTok;
        }
    }

    public double getCostsNac() {
        return costsAccNac;
    }

    public double getCostsChar() {
        return costsAccChar;
    }

    public String getText() {
        return text;
    }

    public List<String> getHist() {
        return hist;
    }

    public TreeKnot getWordKnot() {
        return wordKnot;
    }

    public double getCosts() {
        if (costsAccChar < costsAccNac) {
            return costsAccChar;
        }
        return costsAccNac;
    }

    public Token getPreviousToken() {
        if (preChar == null && preNac == null) {
            return null;
        }
        return costsAccChar < costsAccNac ? preChar : preNac;
    }

    @Override
    public String toString() {
        return "Token{" + "text=" + text + ", costs=" + String.format("%1.3e", getCosts()) + "}";
    }

    public String getLastWord() {
        if (wordKnot instanceof TreeKnotSpace) {
            return preChar.getLastWord();
        }
        return wordKnot.getWordPrefix();
    }

    public int getPos() {
        return pos;
    }

}
