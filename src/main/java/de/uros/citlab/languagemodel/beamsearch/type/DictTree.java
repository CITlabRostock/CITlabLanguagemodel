/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import com.achteck.misc.types.CharMap;
import de.planet.itrtech.types.IDictOccurrence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 *
 * @author tobias
 */
public class DictTree {

    private final TreeKnot root;
    private CharMap<Integer> charMap;

    public DictTree(Collection<String> wordlist) {
        ArrayList<String> list = new ArrayList<String>(wordlist);
        Collections.sort(list);

        root = new TreeKnot();

        TreeKnot[] lasts = new TreeKnot[10];
        String lastWord = "";
        lasts[0] = root;
        for (String word : list) {
            int idx = lastCommonIndex(lastWord, word);
            TreeKnot last = getLast(lasts, idx + 1);
            while (last.isFinalLetter()) {
                last = getLast(lasts, --idx + 1);
            }
            char[] chars = word.toCharArray();
            for (int i = idx + 1; i < chars.length - 1; i++) {
                TreeKnot t = new TreeKnot(chars[i], last, false);
                link(last, t);
                lasts = add(lasts, t, i + 1);
                last = t;
            }
            TreeKnot t = new TreeKnot(chars[chars.length - 1], last, true);
            lasts = add(lasts, t, chars.length);
            link(last, t);
            lastWord = word;
        }

    }

    public DictTree(IDictOccurrence d) {
        this(d.getProbMap().keySet());
    }

    private int lastCommonIndex(String lastWord, String newWord) {
        int i;
        int min = Math.min(lastWord.length(), newWord.length());
        for (i = 0; i < min; i++) {
            if (lastWord.charAt(i) != newWord.charAt(i)) {
                return i - 1;
            }
        }
        return min - 1;
    }

    private void link(TreeKnot last, TreeKnot t) {
        last.addSuccessor(t);
    }

    public TreeKnot getRoot() {
        return root;
    }

    private TreeKnot getLast(TreeKnot[] lasts, int idx) {
        Arrays.fill(lasts, idx + 1, lasts.length, null);
        return lasts[idx];
    }

    private TreeKnot[] add(TreeKnot[] lasts, TreeKnot t, int i) {
        if (lasts.length <= i) {
            TreeKnot[] tmp;
            tmp = new TreeKnot[lasts.length * 2];
            System.arraycopy(lasts, 0, tmp, 0, lasts.length);
            lasts = tmp;
        }
        lasts[i] = t;
        return lasts;
    }

    public void setCharMap(CharMap<Integer> charMap) {
        if (!Objects.equals(this.charMap, charMap)) {
            this.charMap = charMap;
            root.setNewIndices(charMap);
        }
    }

    public LinkedList<TreeKnot> getNewWordKnotes() {
        return root.getSuccessors();
    }

}
