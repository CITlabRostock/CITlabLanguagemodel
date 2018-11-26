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
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tobias
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TrainLMTest {
    static String lmPath = "konz_c.arpa";
    static String lmPath2 = "konz_c2.arpa";
    static String textdir = "src/test/res/konz_c";
    public static int order = 5;
    public TrainLMTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            new File(lmPath).delete();
        } catch (Throwable ex) {
        }
        try {
            new File(lmPath2).delete();
        } catch (Throwable ex) {
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(lmPath);
    }


    /**
     * Test of main method, of class TrainLM.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String arg = "-tmp temp.txt "
                + "-txtFolder " + textdir + " "
                + "-spaceSubs @ "
                + "-lmFile " + lmPath + " "
                + "-n " + order;
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

    @Test
    public void testMethod() throws IOException {
        Collection<File> filelist = FileUtils.listFiles(new File(textdir), new String[]{"txt"}, true);
        List<String> res = new LinkedList<>();
        for (File f : filelist) {
            res.add(FileUtils.readFileToString(f));
        }
        TrainLM.train(res, order, new File(lmPath2));
//        TrainLM.train(Arrays.asList("ich bin so klug",
//                "ich bin so schlau",
//                "ich bin der Anton aus Tirol",
//                "ich habe tolle Waden",
//                "ich bin so schön",
//                "ich bin so toll"), 5, new File("out.txt"));
    }
    @Test
    public void testZSameResult() throws IOException {
        List<String> strings1 = FileUtils.readLines(new File(lmPath));
        List<String> strings2 = FileUtils.readLines(new File(lmPath2));
        Assert.assertEquals("files differ", strings1.size(), strings2.size());
        for (int i = 0; i < strings1.size(); i++) {
            Assert.assertEquals("files differ in line " + i, strings1.get(i), strings2.get(i));
        }
    }

}
