/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.lmtypes;

import com.achteck.misc.types.IParamTreeOrganizer;
import java.util.List;

/**
 *
 * Since 20.07.2016
 *
 * @author Tobias (tobias.strauss@uni-rostock.de)
 */
public interface ILM extends IParamTreeOrganizer{


    public double getLogProb(List<String> phrase);
    
    public double rateSentence(List<String> sentence,boolean withStartAndEnd);

    public List<String> getWords();

    public int getHistSize();

    public String getEoSSymb();

    public String getBoSSymb();

    public String getUnkSymb();

}
