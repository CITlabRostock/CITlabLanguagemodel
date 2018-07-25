/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.decoder;

import com.achteck.misc.types.ConfMat;
import com.achteck.misc.types.ParamAnnotation;
import de.planet.itrtech.types.IDictOccurrence;
import de.planet.citech.types.IDecodingType;
import de.planet.citech.types.ISortingFunction;
import de.planet.decoding.types.MatchWord;
import de.planet.langmod.LangMod;
import de.planet.langmod.LangModFullText;
import de.planet.util.types.DictOccurrence;

import java.util.LinkedList;
import java.util.List;

/**
 * @author tobias
 */
public class LanguageDecoderLangMod extends LangModFullText implements ILanguageDecoder {

    @ParamAnnotation(member = "dTmp")
    String d = DictOccurrence.class.getCanonicalName();
    IDictOccurrence dTmp;

    private ISortingFunction isf;
    private ConfMat cm;

    public LanguageDecoderLangMod() {
        addReflection(this, LanguageDecoderLangMod.class);
    }

    @Override
    public void init() {
        super.init();
        reDictName = "";
        setDict("dict", dTmp);
    }

    //    @Override
//    public void setConfMat(ConfMat cm) {
//        
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    public IDecodingType decode() {
        LangMod.ILangModResult result = getResult();
        MatchWord ret = new MatchWord();
        ret.set(result.getText(),
                0, result.getBestPath().length(),
                result.getBestPath(),
                result.getCostAbs(),
                0,
                IDecodingType.Label.UNDEFINED);
        return ret;
    }

    @Override
    public List<String> getParamValues() {
        LinkedList<String> list = new LinkedList<>();
        list.add("thres;" + splitThres);
        list.add("doSplit;" + doSplit);
        list.add("doMerge;" + doMerge);
        list.add("dict;" + getParamSet().getParam("decclass/d/dict").getString());
        return list;

    }


}
