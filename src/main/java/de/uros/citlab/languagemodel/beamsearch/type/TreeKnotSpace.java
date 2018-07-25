/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch.type;

/**
 *
 * @author tobias
 */
public class TreeKnotSpace extends TreeKnot {

    public TreeKnotSpace() {
        super();
        c = ' ';
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isAccepting() {
        return true;
    }

}
