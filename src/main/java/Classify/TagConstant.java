package Classify;

/**
 * Created by mhjang on 6/3/14.
 */
public class TagConstant {

    static int BEGINTABLE = 1;
    static int INTTABLE = 2;
    static int ENDTABLE = 3;

    static int BEGINCODE = 4;
    static int INTCODE = 5;
    static int ENDCODE = 6;

    static int BEGINEQU = 7;
    static int INTEQU = 8;
    static int ENDEQU = 9;

    static int BEGINMISC = 10;
    static int INTMISC = 11;
    static int ENDMISC = 12;
    static int TEXT = 0;

    static String tableTag = "<TABLE>";
    static String codeTag = "<CODE>";
    static String equTag = "<EQUATION>";
    static String miscTag = "<MISCELLANEOUS>";

    static String tableCloseTag = "</TABLE>";
    static String codeCloseTag = "</CODE>";
    static String equCloseTag = "</EQUATION>";
    static String miscCloseTag = "</MISCELLANEOUS>";


}
