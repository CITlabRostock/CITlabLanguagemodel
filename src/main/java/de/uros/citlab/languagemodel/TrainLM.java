/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel;

import com.achteck.misc.exception.InvalidParameterException;
import com.achteck.misc.log.Logger;
import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import de.uros.citlab.tokenizer.TokenizerCategorizer;
import de.uros.citlab.tokenizer.categorizer.CategorizerCharacterDft;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.KneserNeyFileWritingLmReaderCallback;
import edu.berkeley.nlp.lm.io.KneserNeyLmReaderCallback;
import edu.berkeley.nlp.lm.io.MakeKneserNeyArpaFromText;
import edu.berkeley.nlp.lm.io.TextReader;
import eu.transkribus.interfaces.ITokenizer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author tobias
 */
public class TrainLM extends ParamTreeOrganizer {

    @ParamAnnotation
    private String tmp;
    @ParamAnnotation
    private String txtFolder;
    @ParamAnnotation
    private String lmFile;
    @ParamAnnotation
    private String spaceSubs;
    @ParamAnnotation
    private String n = "5"; // seems to be sufficient
    private static final Logger LOG = Logger.getLogger(TrainLM.class.getName());

    public TrainLM() {
        addReflection(this, TrainLM.class);
    }

    @Override
    public void init() {
        super.init();

    }

    private void run() throws IOException {
        String tokenizedtxts = tokenizeTexts(tmp, txtFolder, spaceSubs);
        trainLM(tokenizedtxts, lmFile);

        Files.delete(Paths.get(tmp));

    }

    private static String tokenize(String line, String spaceSubs, ITokenizer tokenizer) {
        if (line.contains("\t")) {
            line = line.replaceAll("\t", "");
            LOG.log(Logger.WARN, "Tabulator is deleted: \"" + line + "\"");
        }
        List<String> tokenize = tokenizer.tokenize(line);
        StringBuilder sb = new StringBuilder();
        for (String token : tokenize) {
            sb.append(token.equals(" ") ? spaceSubs : token).append(" ");
        }
        if (sb.length() > 1) {
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }


    private static String tokenizeTexts(String tmp, String txts, String spaceSubs) throws IOException {
        TokenizerCategorizer tokenizer = new TokenizerCategorizer(new CategorizerCharacterDft());
        File txtdir = new File(txts);
        File tmpFile = new File(tmp);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        tmpFile.createNewFile();
        Collection<File> filelist = FileUtils.listFiles(txtdir, new String[]{"txt"}, true);
        LOG.log(Logger.INFO, "found " + filelist.size() + " txt-files to train character LM");
        for (File txtfile : filelist) {
            List<String> lines = Files.readAllLines(Paths.get(txtfile.getAbsolutePath()));
            LinkedList<String> tokanizedText = new LinkedList<>();
            for (String line : lines) {
                String tokenized = tokenize(line, spaceSubs, tokenizer);
                if (tokenized != null) {
                    tokanizedText.add(tokenized);
                }
            }
            FileUtils.writeLines(tmpFile, tokanizedText, true);
        }
        return tmpFile.getAbsolutePath();
    }

    private void trainLM(String tokenizedtxts, String lmfile1) {
        String arg = n + " " + lmfile1 + " " + tokenizedtxts;
        String[] args = arg.split(" ");
        Locale.setDefault(Locale.US);
        MakeKneserNeyArpaFromText.main(args);
    }

    public static void train(List<String> text, int lmOrder, File arpaOutputFile) {
        train(text, lmOrder, arpaOutputFile, "@");
    }

    public static void train(List<String> text, int lmOrder, File arpaOutputFile, String spaceSubs) {
        TokenizerCategorizer tokenizer = new TokenizerCategorizer(new CategorizerCharacterDft());
        StringWordIndexer wordIndexer = new StringWordIndexer();
        wordIndexer.setStartSymbol("<s>");
        wordIndexer.setEndSymbol("</s>");
        wordIndexer.setUnkSymbol("<unk>");
        List<String> tokenizedList = new LinkedList<>();
        for (String line : text) {
            String tokenize = tokenize(line, spaceSubs, tokenizer);
            if (tokenize != null) {
                tokenizedList.add(tokenize);
            }
        }
        TextReader reader = new TextReader((Iterable<String>) tokenizedList, wordIndexer);
        KneserNeyLmReaderCallback kneserNeyReader = new KneserNeyLmReaderCallback(wordIndexer, lmOrder, new ConfigOptions());
        reader.parse(kneserNeyReader);
        kneserNeyReader.parse(new KneserNeyFileWritingLmReaderCallback(arpaOutputFile, wordIndexer));

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InvalidParameterException {
        if (args.length == 0) {
//            String arg = "-tmp " + HomeDir.getFile("temp.txt").toString() + " "
//                    + "-txtFolder " + HomeDir.getFile("lines/train/").toString() + " "
//                    + "-spaceSubs @ "
//                    + "-lmFile " + HomeDir.getFile("lm/iam_5gram.arpa").toString() + " "
//                    + "-n 5 ";
//            args = arg.split(" ");
//            System.out.println(arg);

        }
        TrainLM instance = new TrainLM();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = instance.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.FORCE); // be strict, don't accept generic parameter
        instance.setParamSet(ps);
        instance.init();
        instance.run();
    }
}
