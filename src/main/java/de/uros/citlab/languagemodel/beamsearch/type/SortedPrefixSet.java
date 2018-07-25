/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author tobias
 */
public interface SortedPrefixSet {

    void clear();

    double getCostUpperBound();

    Collection<Token> getMostLikelyToks();

    boolean put(TreeKnot wordKnot, String text, List<String> hist, Token preTok, double costChar, double costNac, int pos);

    Collection<Token> values();
    
}
