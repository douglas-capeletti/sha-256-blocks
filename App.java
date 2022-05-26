import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

// https://youtu.be/DkJn9gGikCg
public class App {

    public static final int BLOCK_SIZE = 1024;
    public static String REFERENCE_FILE = "FuncoesResumo - SHA1.mp4";
    public static String REFERENCE_RESULT = "302256b74111bcba1c04282a1e31da7e547d4a7098cdaec8330d48bd87569516";

    public static void main(String ...args) throws Exception {
        byte[] data = loadData(args);

        int numberOfBlocks = data.length / BLOCK_SIZE;
        int lastBlockSize = data.length % BLOCK_SIZE;
        if (lastBlockSize > 0) numberOfBlocks += 1;

        byte[][] splitData = generateBlocks(data, numberOfBlocks);
        byte[] encodedHash = encodeBlocks(numberOfBlocks, lastBlockSize, splitData);

        validate(encodedHash);
    }

    private static byte[] encodeBlocks(int numberOfBlocks, int lastBlockSize, byte[][] splitData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        //remove trim zeros from the last block
        byte[] originalBlock = Arrays.copyOfRange(splitData[numberOfBlocks - 1], 0, lastBlockSize);
        byte[] blockHash = digest.digest(originalBlock);

        for (int i = numberOfBlocks - 2; i >= 0; i--) {
            originalBlock = append(splitData[i], blockHash);
            blockHash = digest.digest(originalBlock);
        }
        return blockHash;
    }

    private static byte[][] generateBlocks(byte[] data, int numberOfBlocks) {
        byte[][] blocks = new byte[numberOfBlocks][BLOCK_SIZE];
        for (int i = 0; i < numberOfBlocks; i++) {
            int startPosition = i * BLOCK_SIZE;
            for (int j = startPosition; j < startPosition + BLOCK_SIZE; j++) {
                if (j == data.length) break;
                blocks[i][j - startPosition] = data[j];
            }
        }
        return blocks;
    }

    private static void validate(byte[] hash) {
        String result = bytesToHex(hash);
        System.out.print(result);
        System.out.println(" | " + result.equals(REFERENCE_RESULT));
    }

    private static byte[] loadData(String[] args) throws IOException {
        if(args.length > 0) REFERENCE_FILE = args[0];
        if(args.length > 1) REFERENCE_RESULT = args[1];
        return Files.readAllBytes(Paths.get(REFERENCE_FILE));
    }

    private static byte[] append(byte[] prev, byte[] next) {
        byte[] result = new byte[prev.length + next.length];
        System.arraycopy(prev, 0, result, 0, prev.length);
        System.arraycopy(next, 0, result, prev.length, next.length);
        return result;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
