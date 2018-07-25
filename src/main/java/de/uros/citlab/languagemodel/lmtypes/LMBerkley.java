/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.lmtypes;

import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import edu.berkeley.nlp.lm.ContextEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tobias
 */
    public class LMBerkley extends LMAbstract implements ILM {

    private ContextEncodedProbBackoffLm<String> lm;
    private LinkedList<String> words;
    @ParamAnnotation
    private String path2File;

    public LMBerkley() {
        addReflection(this, LMBerkley.class);
    }

    public LMBerkley(String path2File) {
        this.path2File = path2File;
        addReflection(this, LMBerkley.class);
    }

    @Override
    public void init() {
        super.init();
        lm = LmReaders.readContextEncodedLmFromArpa(path2File);
        words = new LinkedList<>();
        WordIndexer<String> wordIndexer = lm.getWordIndexer();
        for (int i = 0; i < wordIndexer.numWords(); i++) {
            words.add(wordIndexer.getWord(i));
        }
    }

    @Override
    public double getLogProb(List<String> phrase) {
//        if (phrase.size() <= getHistSize()) {
//            LinkedList<String> myPhrase = new LinkedList<String>(phrase);
//            while (phrase.size() <= getHistSize()) {
//                myPhrase.addFirst(lm.getWordIndexer().getStartSymbol());
//            }
//            return lm.getLogProb(myPhrase);
//        }
        return lm.getLogProb(phrase);
    }

    @Override
    public List<String> getWords() {
        return words;
    }

    @Override
    public int getHistSize() {

        return lm.getLmOrder() - 1;
    }

    public double scoreSentence(List<String> sentence) {
        return lm.scoreSentence(sentence);
    }

    @Override
    public String getEoSSymb() {
        return lm.getWordIndexer().getEndSymbol();
    }

    @Override
    public String getBoSSymb() {
        return lm.getWordIndexer().getStartSymbol();
    }

    @Override
    public String getUnkSymb() {
        return lm.getWordIndexer().getUnkSymbol();
    }

    

}
