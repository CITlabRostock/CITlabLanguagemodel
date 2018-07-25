/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.decoder;

import com.achteck.misc.types.ConfMat;
import de.planet.citech.types.IDecodingType;
import java.util.List;

/**
 *
 * @author tobias
 */
public interface ILanguageDecoder {

    public void setConfMat(ConfMat cm);

    public IDecodingType decode();
    
    public List<String> getParamValues();
}
