/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author tobias
 */
public class HistList {

    private final ArrayList<String> list;
    private final String BoS;
    private final String EoS;
    private final String Unk;
    private final int n;

    public HistList(int n, String BoS, String EoS, String Unk) {
        list = new ArrayList<>(n - 1);
        this.n = n;
        this.Unk = Unk;
        this.BoS = BoS;
        this.EoS = EoS;
        for (int i = 0; i < n - 1; i++) {
            list.add(BoS);
        }

    }

    public void updatePhrase(List<String> phrase, boolean withTgt) {
        int idx = 0;
        if (withTgt) {
            if (phrase.size() < n) {
                while (idx < n - phrase.size()) {
                    list.set(idx++, BoS);
                }
            }
            Iterator<String> iterator = phrase.iterator();
            for (int i = idx; i < n - 1; i++) {
                list.set(i, iterator.next());
            }
        } else {
            if (phrase.size() + 1 < n) {
                while (idx < n - 1 - phrase.size()) {
                    list.set(idx++, BoS);
                }
            }
            Iterator<String> iterator = phrase.iterator();
            for (int i = idx; i < n - 1; i++) {
                list.set(i, iterator.next());
            }
        }
    }

    public boolean sameHist(List<String> phrase, boolean withTgt) {
        int startIdx = 0;
        while (list.get(startIdx).equals(BoS) && ++startIdx < n-1) {
//            startIdx++;
        }

        if (withTgt) {
            if (phrase.size() != n - startIdx) {
                return false;
            }
        } else if (phrase.size() != n - startIdx - 1) {
            return false;
        }
        Iterator<String> iter = phrase.iterator();
        for (int i = startIdx; i < n - 1; i++) {
            if (!list.get(i).equals(iter.next())) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<String> getList() {
        return list;
    }

}
