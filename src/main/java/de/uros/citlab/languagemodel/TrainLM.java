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
import edu.berkeley.nlp.lm.io.MakeKneserNeyArpaFromText;
import de.uros.citlab.tokenizer.TokenizerCategorizer;
import de.uros.citlab.tokenizer.categorizer.CategorizerCharacterDft;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;

/**
 *
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
        String tokenizedtxts = tokenizeTexts(tmp, txtFolder);
        trainLM(tokenizedtxts, lmFile);

        Files.delete(Paths.get(tmp));

    }

    private String tokenizeTexts(String tmp, String txts) throws IOException {
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
            LinkedList<String> tokanizedText = new LinkedList<String>();
            for (String line : lines) {
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
                    tokanizedText.add(sb.substring(0, sb.length() - 1));
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
