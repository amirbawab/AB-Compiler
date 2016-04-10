package translation.helper;

/**
 * Created by Amir on 4/10/2016.
 */
public class ABArchitectureHelper {

    public enum Size {

        BIT(1),
        BYTE(8),
        WORD(32),
        ADDRESS(32),
        REGISTER(32),
        INTEGER(32),
        FLOAT(64)
        ;

        int bitValue;
        Size(int bitValue) {
            this.bitValue = bitValue;
        }

        public int getSizeInBit() {
            return bitValue;
        }

        public int getSizeInByte() {
            return bitValue / 8;
        }

        public int getSizeInWord() {
            return bitValue / 32;
        }
    }

    public static int convertByteToBit(int byteVal) {
        return byteVal * 8;
    }

    public static int convertBitToByte(int bitVal) {
        return bitVal / 8;
    }

    public static int convertWordToByte(int wordVal) {
        return wordVal * 4;
    }

    public static int convertByteToWord(int byteVal) {
        return byteVal / 4;
    }
}
