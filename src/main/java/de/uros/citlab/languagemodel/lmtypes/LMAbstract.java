/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.lmtypes;

import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public abstract class LMAbstract extends ParamTreeOrganizer implements ILM {

    /**
     * End of Sentence Symbol
     */
    @ParamAnnotation
    public String EoS = "</s>";
    /**
     * Begin of Sentence Symbol
     */
    @ParamAnnotation
    public String BoS = "<s>";

    @ParamAnnotation
    public String UNKNOWN = "<unk>";

    public LMAbstract() {
        addReflection(this, LMAbstract.class);
    }

    @Override
    public String getEoSSymb() {
        return EoS;
    }

    @Override
    public String getBoSSymb() {
        return BoS;
    }

    @Override
    public String getUnkSymb() {
        return UNKNOWN;
    }

    @Override
    public double rateSentence(List<String> sentence, boolean withStartAndEnd) {
        int nMinus1 = getHistSize();
        if (withStartAndEnd == false) {
            throw new IllegalArgumentException("without startAndEnd not yet supported. ");
        }
        LinkedList<String> tmp = new LinkedList<String>();
        for (int i = 0; i < nMinus1; i++) {
            tmp.add(BoS);
        }
        double sum = 0.0;
        for (String string : sentence) {
            if (tmp.size() > nMinus1) {
                tmp.removeFirst();
            }
            tmp.add(string);
            sum += getLogProb(tmp);
        }
        if (tmp.size() > nMinus1) {
            tmp.removeFirst();
        }
        tmp.add(EoS);
        sum += getLogProb(tmp);

        return sum;
    }
}
