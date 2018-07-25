/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel.beamsearch;

import com.achteck.misc.log.Logger;
import com.achteck.misc.types.ConfMat;
import de.uros.citlab.languagemodel.beamsearch.type.CTCBeamSearchResult;
import de.uros.citlab.languagemodel.beamsearch.type.DictTree;
import de.uros.citlab.languagemodel.beamsearch.type.ILMCostMapper;
import de.uros.citlab.languagemodel.beamsearch.type.PrefixSet;
import de.uros.citlab.languagemodel.beamsearch.type.Token;
import de.uros.citlab.languagemodel.beamsearch.type.TreeKnot;
import de.uros.citlab.languagemodel.beamsearch.type.TreeKnotSpace;
import de.uros.citlab.languagemodel.lmtypes.ILM;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author tobias
 */
public class CTCBeamSearchTreeset {

    private static final Logger LOG = Logger.getLogger(CTCBeamSearchTreeset.class.getName());
    private final DictTree tree;
    private double[][] costs;
    private final ILM lm;
    private final int numberOfToks2Keep;
    private Integer idxNac;
    private Integer idxSpace;
    private TreeKnot knotSpace;
    private final int numberOfToks2Expand;
    private final ILMCostMapper lmCostMapper;
    private final int histSize;
    private final boolean withBoundaryCosts;
    private final boolean appendSpace;
    private boolean calcOnlyAccepting;
    private final double nacOffset;

    /**
     *
     * @param lm
     * @param withBoundaryCosts
     * @param numberOfToks2Keep
     * @param lmCostMapper
     * @param appendSpace
     */
    public CTCBeamSearchTreeset(ILM lm, DictTree tree,
            boolean withBoundaryCosts,
            int numberOfToks2Keep,
            int numberOfToks2Expand,
            ILMCostMapper lmCostMapper,
            boolean appendSpace,
            double nacOffset) {
        this.numberOfToks2Keep = numberOfToks2Keep;
        this.lm = lm;
        histSize = lm.getHistSize();
        this.tree = tree;
        this.withBoundaryCosts = withBoundaryCosts;
        this.numberOfToks2Expand = numberOfToks2Expand;
        this.lmCostMapper = lmCostMapper;
        this.appendSpace = appendSpace;
        if (appendSpace) {
            this.knotSpace = new TreeKnotSpace();
        }
        this.nacOffset = nacOffset;
    }

    public void setConfMat(ConfMat cm) {
        tree.setCharMap(cm.getCharMap());
        if (appendSpace) {
            knotSpace.setNewIndices(cm.getCharMap());
        }
        idxNac = cm.getCharMap().getKey(ConfMat.NaC);
        idxSpace = cm.getCharMap().getKey(' ');
        costs = neg(cm.getDoubleMat());
    }

    public CTCBeamSearchResult search() {
        LinkedList<String> hist = new LinkedList<>();
        if (withBoundaryCosts && histSize > 0) {
            hist.add(lm.getBoSSymb());
        }
        PrefixSet allToks = new PrefixSet(numberOfToks2Keep, numberOfToks2Expand);
        allToks.put(tree.getRoot(), "", hist, null, 0, 0, -1);
//        int calculated = 0;
//        int total = 0;
        for (int pos = 0; pos < costs.length; pos++) {
            double[] cost = costs[pos];
            Collection<Token> mostLikelyToks = allToks.getMostLikelyToks();
            allToks.clear();

            calcOnlyAccepting = pos == costs.length - 1;
            for (Token mostLikelyTok : mostLikelyToks) {
                if (mostLikelyTok.getCosts() < allToks.getCostUpperBound()) {
//              Nur die Pfade weiterlaufen, die auch noch gewinnen können.      
                    expand(mostLikelyTok, cost, allToks, pos);
//                    calculated++;
                }
//                total++;

            }
        }
//        System.out.println("calc ratio: " + ((double) calculated / total));
        Collection<Token> toks = withBoundaryCosts && histSize > 0 ? addBos(allToks.values()) : allToks.values();

        Token min = getMinAccepting(toks);

        if (min == null) {
            min = toks.iterator().next();
        }

        return new CTCBeamSearchResult(min);
    }

    /**
     * ToDo: Noch keine LM costs bei erstem Wort.
     *
     * @param preTok
     * @param cost
     * @param allToks
     */
    private void expand(Token preTok, double[] cost, PrefixSet allToks, int pos) {

        /**
         * continue (keine LM-costs!)
         */
        String preText = preTok.getText();
//        Token newTok;
        double costChar = (preTok.getWordKnot().getIdx() < 0) ? Double.MAX_VALUE
                : preTok.getCostsChar() + cost[preTok.getWordKnot().getIdx()];
        double costNac = Math.min(preTok.getCostsNac(), preTok.getCostsChar()) + cost[idxNac] + nacOffset;
//        String key = preTok.isFinalLetter() ? preText : preText + ConfMat.NaC;
        allToks.put(preTok.getWordKnot(), preTok.getText(), preTok.getHist(), preTok, costChar, costNac, pos);

        /**
         * append
         */
        LinkedList<TreeKnot> nextWordKnotes;
        costNac = Double.MAX_VALUE;

        if (preTok.isFinalLetter() || preTok.getWordKnot() instanceof TreeKnotSpace) {
            /**
             * neue Wörter
             */
            addNewWords(preText, preTok, allToks, cost, pos);
        } else {
            /**
             * kein neues Wort -> keine neue Hist!
             */
            nextWordKnotes = preTok.getNextWordKnotes();
            for (TreeKnot nextWordKnot : nextWordKnotes) {
                if (calcOnlyAccepting && !nextWordKnot.isFinalLetter()) {
                    continue;
                }
                String newText = preText + nextWordKnot.getChar();
                Integer idx = nextWordKnot.getIdx();
                if (idx == null) {
                    continue;
                }
                costChar = Objects.equals(preTok.getWordKnot().getChar(), nextWordKnot.getChar()) ? preTok.getCostsNac() + cost[idx] : preTok.getCosts() + cost[idx];
                createAndAddNewToken(newText, costChar, costNac, nextWordKnot, preTok, allToks, preTok.getHist(), pos);
            }

        }

        /**
         * Space anhängen
         */
        if (appendSpace && (preTok.isFinalLetter() || preTok.getWordKnot() == tree.getRoot())) {
            costChar = preTok.getCosts() + cost[idxSpace];
            createAndAddNewToken(preText + " ", costChar, costNac, knotSpace, preTok, allToks, preTok.getHist(), pos);
        }

    }

    private void addNewWords(String preText, Token preTok, PrefixSet allToks, double[] cost, int pos) {
        /**
         * neues Wort -> LM-costs!
         */
        LinkedList<TreeKnot> nextWordKnotes = tree.getNewWordKnotes();
        double costNac = Double.MAX_VALUE;
        for (TreeKnot nextWordKnot : nextWordKnotes) {
            if (calcOnlyAccepting && !nextWordKnot.isFinalLetter()) {
                continue;
            }
            String newText = preText + nextWordKnot.getChar();
            Integer idx = nextWordKnot.getIdx();
            if (idx == null) {
                continue;
            }
            LinkedList<String> newHist = new LinkedList<>();
            if (histSize > 0) {
                Iterator<String> iterator = preTok.getHist().iterator();
                if (preTok.getHist().size() == histSize) {
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    newHist.add(iterator.next());
                }
                String lastWord = preTok.getLastWord();
                if (!lastWord.isEmpty()) {
                    newHist.add(lastWord);
                }
            }
            double costChar = Objects.equals(preTok.getWordKnot().getChar(), nextWordKnot.getChar()) ? preTok.getCostsNac() + cost[idx] : preTok.getCosts() + cost[idx];
            createAndAddNewToken(newText, costChar, costNac, nextWordKnot, preTok, allToks, newHist, pos);
        }
    }

    private void createAndAddNewToken(
            String newText,
            double costChar,
            double costNac,
            TreeKnot nextWordKnot,
            Token preTok,
            PrefixSet allToks,
            List<String> hist,
            int pos) {
        costChar += lmCostMapper.getLMCosts(preTok, hist, nextWordKnot);
        allToks.put(nextWordKnot, newText, hist, preTok, costChar, costNac, pos);
    }

    private double[][] neg(double[][] doubleMat) {
        double[][] ret = new double[doubleMat.length][doubleMat[0].length];
        for (int i = 0; i < ret.length; i++) {
            double[] cpy = ret[i];
            double[] orig = doubleMat[i];
            for (int j = 0; j < orig.length; j++) {
                cpy[j] = -orig[j];
            }
        }
        return ret;
    }

    private Collection<Token> addBos(Collection<Token> values) {
        LinkedList<Token> ret = new LinkedList<>();
        double min = Double.MAX_VALUE;
        for (Token value : values) {
            if (min > value.getCosts() && value.isAccepting()) {
                LinkedList<String> phrase = new LinkedList<>(value.getHist());
                phrase.removeFirst();
                phrase.add(value.getLastWord());
                phrase.add(lm.getEoSSymb());
                double costs = -lm.getLogProb(phrase) + value.getCosts();
                if (min > costs) {
                    ret.clear();
                    Token tmp = new Token(value.getWordKnot(), value.getText(), value.getHist(), value, costs, costs, value.getPos());
                    ret.add(tmp);
                    min = costs;
                }
            }
        }
        return ret;
    }

    private Token getMinAccepting(Collection<Token> toks) {
        Token bestTok = null;
        double minCosts = Double.MAX_VALUE;
        for (Token tok : toks) {
            if (tok.isAccepting() && tok.getCosts() < minCosts) {
                bestTok = tok;
                minCosts = tok.getCosts();
            }
        }
        return bestTok;
    }

    private static class ComparatorToken implements Comparator<Token> {

        public ComparatorToken() {
        }

        @Override
        public int compare(Token o1, Token o2) {
            if (o1.isFinalLetter() != o2.isFinalLetter()) {
                if (o1.isFinalLetter()) {
                    return -1;
                }
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

}
