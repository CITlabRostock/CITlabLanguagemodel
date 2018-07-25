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
public interface ILMCostMapper {

    double getLMCosts(Token preTok, List<String> hist, TreeKnot nextWordKnot);
    
}
