/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.lmtypes;

import com.achteck.misc.types.ParamAnnotation;

/**
 *
 * @author tobias
 */
public class LMNetworkTFChar extends LMNetworkTF {

    @ParamAnnotation
    private String spaceSubs;

    public LMNetworkTFChar() {
        addReflection(this, LMNetworkTFChar.class);
    }

    public LMNetworkTFChar(String path2Net, String path2WordMap, String path2Config, int n, String spaceSubs) {
        super(path2Net, path2WordMap, path2Config, n);
        this.spaceSubs = spaceSubs;
        addReflection(this, LMNetworkTFChar.class);
    }

    @Override
    public void init() {
        super.init();
        Integer key = (Integer) wordMap.getKey(spaceSubs);
        wordMap.put(key, " ");

    }

}
