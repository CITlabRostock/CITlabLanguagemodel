/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.decoder;

import com.achteck.misc.types.ConfMat;
import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import de.planet.citech.types.IDecodingType;
import de.planet.citech.types.ISortingFunction;
import de.planet.imaging.types.IWDImage;
import de.planet.itrtech.types.IDictOccurrence;
import de.planet.itrtech.writingdecoder.IWritingDecoder;
import de.planet.langmod.types.ILangMod;
import de.uros.citlab.languagemodel.beamsearch.CTCBeamSearchTreeset;
import de.uros.citlab.languagemodel.beamsearch.type.DictTree;
import de.uros.citlab.languagemodel.beamsearch.type.LMCostMapperChar;
import de.uros.citlab.languagemodel.lmtypes.ILM;
import de.uros.citlab.languagemodel.lmtypes.LMNetworkTF;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tobias, Gundram
 */
public class LanguageDecoderLM extends ParamTreeOrganizer implements ILanguageDecoder, ILangMod {

    @ParamAnnotation
    double lmAlpha = 1;
    @ParamAnnotation
    double lmBeta = -1.5;
    @ParamAnnotation
    int lmN = 200;
    @ParamAnnotation
    private int lmM = 10;
    @ParamAnnotation
    private double nacOffset = -Math.log(0.4);
    @ParamAnnotation(member = "lm")
    String lmclass;
    protected ILM lm;

    private CTCBeamSearchTreeset searcher;
    private ISortingFunction isf;
    private ConfMat cm;

    public LanguageDecoderLM() {
        this(LMNetworkTF.class.getCanonicalName());
    }

    public LanguageDecoderLM(ILM lm) {
        this.lm = lm;
        this.lmclass = lm.getClass().getCanonicalName();
        addReflection(this, LanguageDecoderLM.class);
    }

    public LanguageDecoderLM(String lmclass) {
//        this();
        this.lmclass = lmclass;
        addReflection(this, LanguageDecoderLM.class);
    }

    @Override
    public void init() {
        super.init();
//        lm = initLM();
        initSearcher();
    }

    private void initSearcher() {
        DictTree tree = new DictTree(lm.getWords());
        searcher = new CTCBeamSearchTreeset(lm, tree, false, lmN, lmM, new LMCostMapperChar(lm, lmAlpha, lmBeta), false, nacOffset);
    }

    @Override
    public void setConfMat(ConfMat cm) {
        if (searcher == null) {
            initSearcher();
        }
        searcher.setConfMat(cm);
    }

    @Override
    public IDecodingType decode() {
        return (IDecodingType) searcher.search();
    }

    @Override
    public List<String> getParamValues() {
        List<String> l = new LinkedList<>();
        l.add("lmAlpha;" + lmAlpha);
        l.add("lmBeta;" + lmBeta);
        l.add("lmN;" + lmN);
        l.add("lmM;" + lmM);
        return l;
    }

    @Override
    public void setDict(String string, Collection<String> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDict(String string, IDictOccurrence ido) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeDict(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInput(IWDImage iwdi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
    }

    @Override
    public void setConfMats(List<ConfMat> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSortingFunction(ISortingFunction isf) {
        this.isf = isf;
    }

    @Override
    public ISortingFunction getSortingFunction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ILangModResult getResult() {
        final IDecodingType s = decode();
        return new ILangModResult() {
            @Override
            public String getText() {
                return s.getText();
            }

            @Override
            public double getCostAbs() {
                return s.getCostAbs();
            }

            @Override
            public double getScore() {
                return isf.getSortingCost(s);
            }

            @Override
            public String getBestPath() {
                return cm.getBestPath();
            }

            @Override
            public List<? extends ILangModGroup> getGroups() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public List<? extends ILangModGroup> getGroups(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    @Override
    public List<ILangModResult> getResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IWritingDecoder getWritingDecoder() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setWritingDecoder(IWritingDecoder iwd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
