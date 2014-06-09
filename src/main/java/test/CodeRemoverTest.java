package test;

import componentDetection.DetectCodeComponent;
import junit.framework.TestCase;

/**
 * Created by mhjang on 4/6/14.
 */
public class CodeRemoverTest extends TestCase {
    public static void main() {
        CodeRemoverTest crt = new CodeRemoverTest();
        crt.testCodeRemove();
    }
    public void testCodeRemove() {
        // Smoke test with batch search
        String code = "    return -1; \n";
        DetectCodeComponent dcc = new DetectCodeComponent();
      //  boolean judgement = true;
        boolean judgement = dcc.isCodeLine(code.trim());
        boolean expected = true;
        assertEquals(expected, judgement);
    }
}
