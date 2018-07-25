/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.languagemodel;

import de.uros.citlab.languagemodel.lmtypes.ILM;
import de.uros.citlab.languagemodel.lmtypes.LMBerkleyChar;
import de.uros.citlab.languagemodel.util.Perplexity;
import de.uros.citlab.tokenizer.TokenizerCategorizer;
import de.uros.citlab.tokenizer.categorizer.CategorizerCharacterDft;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author tobias
 */
public class TrainLMTest {
    String lmPath = "konz_c.arpa";

    public TrainLMTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        File f = new File(lmPath);
        f.delete();
    }


    /**
     * Test of main method, of class TrainLM.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String textdir = "src/test/res/konz_c";
        String arg = "-tmp temp.txt "
                + "-txtFolder " + textdir + " "
                + "-spaceSubs @ "
                + "-lmFile " + lmPath + " "
                + "-n 5";
        String[] args = arg.split(" ");
        TrainLM.main(args);

        Collection<File> filelist = FileUtils.listFiles(new File(textdir), new String[]{"txt"}, true);
        String[] paths = new String[filelist.size()];
        int cnt = 0;
        for (File file : filelist) {
            paths[cnt++] = file.getAbsolutePath();
        }

        ILM lm = new LMBerkleyChar(lmPath, "@");
        lm.setParamSet(lm.getDefaultParamSet(null));
        lm.init();
        double ppl = Perplexity.calcPerplexity(lm, paths, new TokenizerCategorizer(new CategorizerCharacterDft()));

        System.out.println("ppl: " + ppl);
        // TODO review the generated test code and remove the default call to fail.

    }

}
