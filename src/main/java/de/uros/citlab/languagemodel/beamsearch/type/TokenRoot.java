/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

import java.util.List;

/**
 *
 * @author tobias
 */
public class TokenRoot extends Token {

    public TokenRoot(TreeKnot root, List<String> hist) {
        super(root, "", hist, null, 0, 0, -1);
    }

}
