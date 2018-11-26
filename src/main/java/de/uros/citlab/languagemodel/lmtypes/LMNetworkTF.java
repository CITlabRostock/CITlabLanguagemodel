package de.uros.citlab.languagemodel.lmtypes;

/*
 *   
 * 
 * 
 *  <p>Copyright: Copyright (c) 2017</p>
 * 
 *  <p>Company: URO - CITlab</p>
 * 
 *  @author tobias.strauss(at)uni-rostock.de
 *  @version 1.0
 * 
 *  Created on 23.03.2017, 11:00 CET
 */
import com.achteck.misc.types.ParamAnnotation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.uros.citlab.languagemodel.beamsearch.type.HistList;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 *
 * @author tobias
 */
public class LMNetworkTF extends LMAbstract implements Serializable, ILM {

    private static final long serialVersionUID = 1L;

    //cache
    private String uniqueID = null;

    protected String netName;
    protected String configName;

    public static Logger LOG = Logger.getLogger(LMNetworkTF.class.getName());

    private transient Session s;
    private byte[] graphDef;
    private byte[] config = null;
    protected BidiMap wordMap;
    protected HistList lastHist;
    protected double[] lastProbs;
    @ParamAnnotation
    protected int n;
    @ParamAnnotation
    private String path2Net;
    @ParamAnnotation
    private String path2Config;
    @ParamAnnotation
    private String path2WordMap;
    @ParamAnnotation
    private double costunknownword = 10;
    private LinkedList<String> unknowntoken;

    public LMNetworkTF() {
        addReflection(this, LMNetworkTF.class);
    }

    public LMNetworkTF(String path2Net, String path2WordMap, String path2Config, int n) {
        if (path2Net == null || path2Net.isEmpty()) {
            throw new RuntimeException("path to net must not be null or empty");
        }
        this.path2Net = path2Net;
        this.path2Config = path2Config;
        this.path2WordMap = path2WordMap;
        this.n = n;
        addReflection(this, LMNetworkTF.class);
    }

    public final void setConfig(File pathToConfig) {
        if (pathToConfig == null) {
            configName = null;
            config = null;
            return;
        }
        config = readAllBytes(pathToConfig.getAbsolutePath());
        configName = pathToConfig.getName();
    }

    @Override
    public void init() {
        super.init();
        LOG = Logger.getLogger(this.getClass());
        LOG.log(Level.INFO, "Loading LM \"" + path2Net + "\" and wordmap \"" + path2WordMap + "\"");
        File fileNet = new File(path2Net);
        if (!fileNet.exists()) {
            throw new RuntimeException("path to net must not be null or empty");
        }
        netName = fileNet.getName();
        graphDef = readAllBytes(path2Net);
        createUniqueID();
        if (path2WordMap == null || path2WordMap.isEmpty() || !new File(path2WordMap).exists()) {
            throw new RuntimeException("path to net must not be null, empty or does not exist");
        }
        setConfig(path2Config != null ? new File(path2Config) : null);
        wordMap = loadWordMap((path2WordMap));

        Graph g = new Graph();
        g.importGraphDef(graphDef);
        if (config == null) {
            s = new Session(g);
        } else {
            s = new Session(g, config);
        }
        lastHist = new HistList(n, BoS, EoS, UNKNOWN);
        lastProbs = calcProbs(lastHist.getList());
        unknowntoken = new LinkedList<String>();
    }

    private byte[] readAllBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Failed to read [" + path + "] ", e);
            throw new RuntimeException("Failed to read [" + path + "] ", e);
        }
    }

    private void createUniqueID() {
        if (uniqueID == null) {
            uniqueID = getName() + "_" + Arrays.hashCode(graphDef);
        }

    }

    public String getName() {
        return netName;
    }

    public String getUniqueId() {
        return uniqueID;
    }

    public void setUniqueId(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public static BidiMap loadWordMap(String path) {
        BidiMap ret = new DualHashBidiMap();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            StringBuilder text = new StringBuilder();
            for (String line : lines) {
                text.append(line).append("\n");
            }

            Gson g = new Gson();
            Type stringStringMap = new TypeToken<Map<String, Integer>>() {
            }.getType();
            Map<String, Integer> map = g.fromJson(text.toString(), stringStringMap);

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                ret.put(entry.getValue(), entry.getKey());
            }
        } catch (IOException ex) {
            throw new RuntimeException("cannot load file " + path + ".", ex);
//
        }
        return ret;
    }

    @Override
    public double getLogProb(List<String> phrase) {
        if (phrase.size() > n) {
            throw new IllegalArgumentException("phrases of size n+1 or more are not yet supported.");
        }
        if (phrase.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate target prob for empty phrase.");
        }
        Integer key = (Integer) wordMap.getKey(phrase.get(phrase.size() - 1));
        if (key == null) {
            key = (Integer) wordMap.getKey(UNKNOWN);
        }
        if (!lastHist.sameHist(phrase, true)) {
            lastProbs = updateProbs(phrase);
        }
        if (lastProbs == null) {

            return Double.NEGATIVE_INFINITY;
        }
        return lastProbs[key];
    }

    /**
     * Vorsicht! Diese Methode ist nicht Laufzeit-optimiert!
     *
     * @param phrase
     * @return
     */
    public String predictNextWord(List<String> phrase) {
        if (phrase.size() != n - 1) {
            throw new IllegalArgumentException("history must be of size n-1.");
        }
        if (!lastHist.sameHist(phrase, false)) {
            lastHist.updatePhrase(phrase, false);
            lastProbs = calcProbs(lastHist.getList());
        }
        int id = maxIndex(lastProbs);
        return (String) wordMap.get(id);
    }

    @Override
    public List<String> getWords() {
        return new LinkedList<>(wordMap.values());
    }

    @Override
    public int getHistSize() {
        return n - 1;
    }

    protected boolean sameHist(List<String> phrase, List<String> lastHist) {
        if (lastHist == null || lastHist.isEmpty()) {
            return false;
        }
        if (phrase.size() - 1 != lastHist.size()) {
            return false;
        }
        for (int i = 0; i < lastHist.size(); i++) {
            if (!lastHist.get(i).equals(phrase.get(i))) {
                return false;
            }
        }
        return true;
    }

    private double[] calcProbs(List<String> lastHist) {
        int[] idcs = getIndices(lastHist);
        if (idcs == null) {
            return null;
        }
        double[] probs = runNetworkTensorflow(idcs);
        if (probs == null) {
            throw new RuntimeException("result of TF network is null");
        }
        return probs;
    }

    private double[] runNetworkTensorflow(int[] input_array) {
        int nminus1 = n - 1;
        long[] longarr = new long[input_array.length];
        for (int i = 0; i < input_array.length; i++) {
            longarr[i] = input_array[i];
        }

        Tensor word_idcs = Tensor.create(new long[]{1, nminus1}, LongBuffer.wrap(longarr));
        Session.Runner feed = s.runner().feed("feeds/input", word_idcs);

        Session.Runner fetch = feed.fetch("val/model/output");
        List<Tensor<?>> run = fetch.run();
        Tensor result = run.get(0);
        FloatBuffer buffer = FloatBuffer.allocate((int) result.shape()[1]);
        result.writeTo(buffer);
        float[] res = buffer.array();
        double[] ret = new double[res.length];
        double sum = 0.0;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = res[i];
            sum += Math.exp(ret[i]);
        }
        sum = Math.log(sum);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ret[i] - sum;

        }
        word_idcs.close();
        result.close();
        return ret;
    }

    private int[] getIndices(List<String> lastHist) {
        int[] ret = new int[lastHist.size()];
        int i = 0;
        for (String string : lastHist) {
            Integer key = (Integer) wordMap.getKey(string);
            if (key == null) {
                if (!unknowntoken.contains(string)) {
                    unknowntoken.add(string);
                    LOG.log(Priority.ERROR, "Index of \"" + string + "\" not found");
                }
                key = (Integer) wordMap.getKey(UNKNOWN);
            }
            ret[i++] = key;
        }
        return ret;
    }

    private int maxIndex(double[] lastProbs) {
        int maxIdx = 0;
        for (int i = 1; i < lastProbs.length; i++) {
            if (lastProbs[i] > lastProbs[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private double[] updateProbs(List<String> phrase) {
        lastHist.updatePhrase(phrase, true);
        return calcProbs(lastHist.getList());
    }
}
