import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MatrixProductTest {

    public static void main(String[] args) {
        int[] sizes = {600, 1000, 1400, 1800, 2200, 2600, 3000};
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/matrix_multiplication_java.txt"))) {
            for (int size : sizes) {
                writer.printf("Matrix size: %dx%d\n", size, size);
                
                long startTime = System.nanoTime();
                MatrixProduct.onMult(size, size);
                long endTime = System.nanoTime();
                writer.printf("onMult time: %.3f seconds\n", (endTime - startTime) / 1e9);
                
                startTime = System.nanoTime();
                MatrixProduct.onMultLine(size, size);
                endTime = System.nanoTime();
                writer.printf("onMultLine time: %.3f seconds\n\n", (endTime - startTime) / 1e9);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}