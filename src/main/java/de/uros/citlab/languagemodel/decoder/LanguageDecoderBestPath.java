/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.decoder;

import com.achteck.misc.types.ConfMat;
import com.achteck.misc.types.ParamTreeOrganizer;
import de.planet.imaging.types.IWDImage;
import de.planet.itrtech.types.IDictOccurrence;
import de.planet.itrtech.writingdecoder.IWritingDecoder;
import de.planet.citech.types.IDecodingType;
import de.planet.langmod.types.ILangMod;
import de.planet.citech.types.ISortingFunction;
import de.planet.decoding.types.MatchPosAndWord;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
public class LanguageDecoderBestPath extends ParamTreeOrganizer implements ILanguageDecoder, ILangMod{

    private ConfMat cm;
    private ISortingFunction sf;

    @Override
    public void setConfMat(ConfMat cm) {
        this.cm=cm;
    }

    @Override
    public IDecodingType decode() {
        MatchPosAndWord mw = new MatchPosAndWord(0, 0, cm.toString(), cm.toString(), 0, cm.getLength(), IDecodingType.Label.UNDEFINED, null);
        return mw;
    }

    @Override
    public List<String> getParamValues() {
        return new LinkedList<>();
    }

    @Override
    public void setDict(String string, Collection<String> clctn) {
    }

    @Override
    public void setDict(String string, IDictOccurrence ido) {
    }

    @Override
    public void removeDict(String string) {
    }

    @Override
    public void setInput(IWDImage iwdi) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConfMats(List<ConfMat> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param isf
     */
    @Override
    public void setSortingFunction(ISortingFunction isf) {
        this.sf=isf;
    }

    @Override
    public ISortingFunction getSortingFunction() {
        return sf;
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
                return sf.getSortingCost(s);
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
