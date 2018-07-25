/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import com.achteck.misc.log.Logger;
import com.achteck.misc.types.CharMap;
import java.util.LinkedList;

/**
 *
 * @author tobias
 */
public class TreeKnot implements Comparable<TreeKnot> {

    private static final Logger LOG = Logger.getLogger(TreeKnot.class.getName());
    private String pref;
    private TreeKnot predecessor;
    private final LinkedList<TreeKnot> successors;
    private final boolean isAccepting;
    private final LinkedList<String> wordsWithThisPrefix;
    protected Character c;
    private Integer idx = -1;

    public TreeKnot() {
        pref = null;
        pref = "";
        successors = new LinkedList<>();
        isAccepting = false;
        wordsWithThisPrefix = new LinkedList<>();
        c = null;
    }

    public TreeKnot(char aChar, TreeKnot pred, boolean isAccepting) {
        pref = pred.getWordPrefix() + aChar;
        c = aChar;
        predecessor = pred;
        successors = new LinkedList<>();
        this.isAccepting = isAccepting;
        wordsWithThisPrefix = new LinkedList<>();
        if (isAccepting) {
            wordsWithThisPrefix.add(pref);
            predecessor.addNewWord(pref);
        }
    }

    public String getWordPrefix() {
        return pref;
    }

    public void addSuccessor(TreeKnot t) {
        successors.add(t);
    }

    public TreeKnot getPredecessor() {
        return predecessor;
    }

    public LinkedList<TreeKnot> getSuccessors() {
        return successors;
    }

    private void addNewWord(String word) {
        wordsWithThisPrefix.add(word);
        if (predecessor != null) {
            predecessor.addNewWord(word);
        }
    }

    public boolean isFinalLetter() {
        return isAccepting;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public LinkedList<String> getWordsWithThisPrefix() {
        return wordsWithThisPrefix;
    }

    public Character getChar() {
        return c;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setNewIndices(CharMap<Integer> charMap) {
        if (c != null) {
            idx = charMap.getKey(c);
            if (idx == null) {
                LOG.log(Logger.WARN, "CharMap does not contain letter: " + c);
            }
        }
        for (TreeKnot successor : successors) {
            successor.setNewIndices(charMap);
        }
    }

    @Override
    public int compareTo(TreeKnot o) {
        if (c == null) {
            return 1;
        }
        if (o.c == null) {
            return -1;
        }
        return Character.compare(c, o.c);
    }

    @Override
    public String toString() {
        return pref;
    }

}
