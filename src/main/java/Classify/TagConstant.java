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

    static String tableIntTag = "<TABLE-I>";
    static String codeIntTag = "<CODE-I>";
    static String equIntTag = "<EQUATION-I>";
    static String miscIntTag = "<MISCELLANEOUS-I>";

    /**
     * for printing out test output
     * @param tagIdx
     * @return
     */
    static String getTagLabel(int tagIdx) {
        if(tagIdx == BEGINTABLE) return tableTag;
        if(tagIdx == INTTABLE) return tableIntTag;
        if(tagIdx == ENDTABLE) return tableCloseTag;
        if(tagIdx == BEGINCODE) return codeTag;
        if(tagIdx == INTCODE) return codeIntTag;
        if(tagIdx == ENDCODE) return codeCloseTag;
        if(tagIdx == BEGINEQU) return equTag;
        if(tagIdx == INTEQU) return equIntTag;
        if(tagIdx == ENDEQU) return equCloseTag;
        if(tagIdx == BEGINMISC) return miscTag;
        if(tagIdx == INTMISC) return miscIntTag;
        if(tagIdx == ENDMISC) return miscCloseTag;
        else return "TEXT";

    }


}
