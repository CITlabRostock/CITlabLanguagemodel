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
public class LMBerkleyChar extends LMAbstract implements ILM {

    private ContextEncodedProbBackoffLm<String> lm;
    private LinkedList<String> words;
    @ParamAnnotation
    private String path2File;
    @ParamAnnotation
    private String spaceSubs;
    private char spaceSubsChar;
    LinkedList<String> myPhrase = new LinkedList<String>();

    public LMBerkleyChar() {
        addReflection(this, LMBerkleyChar.class);
    }

    /**
     *
     * @param path2File
     * @param spaceSubs
     */
    public LMBerkleyChar(String path2File, String spaceSubs) {
        this.path2File = path2File;
        this.spaceSubs = spaceSubs;
        addReflection(this, LMBerkleyChar.class);
    }

    @Override
    public void init() {
        super.init();
        lm = LmReaders.readContextEncodedLmFromArpa(path2File);
        words = new LinkedList<>();
        if (spaceSubs.isEmpty() || spaceSubs.length() > 1) {
            throw new IllegalArgumentException("parameter spaceSubs not correctly set");
        }
        spaceSubsChar = spaceSubs.charAt(0);
        WordIndexer<String> wordIndexer = lm.getWordIndexer();
        for (int i = 0; i < wordIndexer.numWords(); i++) {
            words.add(wordIndexer.getWord(i).replace(spaceSubsChar, ' '));
        }
    }

    @Override
    public double getLogProb(List<String> phrase) {
        myPhrase.clear();
        for (String string : phrase) {
            myPhrase.add(string.replace(' ', spaceSubsChar));
        }
        return lm.getLogProb(myPhrase);
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
        myPhrase.clear();
        for (String string : sentence) {
            myPhrase.add(string.replace(' ', spaceSubsChar));
        }
        return lm.scoreSentence(myPhrase);
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
