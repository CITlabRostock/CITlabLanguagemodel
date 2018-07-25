/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.lmtypes;

import com.achteck.misc.types.ParamAnnotation;
import de.planet.itrtech.types.IDictOccurrence;
import de.planet.util.types.DictOccurrence;
import java.util.List;

/**
 *
 * @author tobias
 */
public class LMDictUni extends LMAbstract implements ILM {

    @ParamAnnotation(member = "dict")
    String dclass = DictOccurrence.class.getCanonicalName();
    IDictOccurrence dict;

    public LMDictUni() {
        addReflection(this, LMDictUni.class);
        EoS = "";
        BoS = "";
        UNKNOWN = "";
    }

    @Override
    public double getLogProb(List<String> phrase) {
        String word = phrase.get(phrase.size() - 1);
        return -dict.getCost(word);
    }

    @Override
    public List<String> getWords() {
        return dict.getDict();
    }

    @Override
    public int getHistSize() {
        return 0;
    }

}
