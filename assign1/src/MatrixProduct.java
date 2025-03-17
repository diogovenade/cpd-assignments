import java.util.Scanner;

public class MatrixProduct {

    public static void onMult(int m_ar, int m_br) {
        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_ar; j++) {
                pha[i][j] = 1.0;
            }
        }

        for (int i = 0; i < m_br; i++) {
            for (int j = 0; j < m_br; j++) {
                phb[i][j] = i + 1;
            }
        }

        double temp;

        long startTime = System.nanoTime();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i][k] * phb[k][j];
                }
                phc[i][j] = temp;
            }
        }

        long endTime = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (endTime - startTime) / 1e9);

        System.out.println("Result matrix:");
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[0][j] + " ");
        }
        System.out.println();
    }

    public static void onMultLine(int m_ar, int m_br) {
        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_ar; j++) {
                pha[i][j] = 1.0;
            }
        }

        for (int i = 0; i < m_br; i++) {
            for (int j = 0; j < m_br; j++) {
                phb[i][j] = i + 1;
            }
        }

        long startTime = System.nanoTime();

        for (int i = 0; i < m_ar; i++) {
            for (int k = 0; k < m_ar; k++) {
                double valA = pha[i][k];
                for (int j = 0; j < m_br; j++) {
                    phc[i][j] += valA * phb[k][j];
                }
            }
        }

        long endTime = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (endTime - startTime) / 1e9);

        System.out.println("Result matrix:");
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[0][j] + " ");
        }
        System.out.println();
    }

    public static void onMultBlock(int m_ar, int m_br, int bkSize) {
        double[][] pha = new double[m_ar][m_ar];
        double[][] phb = new double[m_ar][m_ar];
        double[][] phc = new double[m_ar][m_ar];

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_ar; j++) {
                pha[i][j] = 1.0;
            }
        }

        for (int i = 0; i < m_br; i++) {
            for (int j = 0; j < m_br; j++) {
                phb[i][j] = i + 1;
            }
        }

        double temp;

        long startTime = System.nanoTime();

        for (int i = 0; i < m_ar; i += bkSize) {
            for (int j = 0; j < m_br; j += bkSize) {
                for (int k = 0; k < m_ar; k += bkSize) {

                    for (int l = i; l < Math.min(i + bkSize, m_ar); l++) {
                        for (int m = k; m < Math.min(k + bkSize, m_ar); m++) {
                            temp = pha[l][m];
                            for (int n = j; n < Math.min(j + bkSize, m_br); n++) {
                                phc[l][n] += temp * phb[m][n];
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        System.out.printf("Time: %.3f seconds\n", (endTime - startTime) / 1e9);

        System.out.println("Result matrix:");
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[0][j] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int op;

        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("Selection?: ");
            op = scanner.nextInt();
            if (op == 0) break;

            System.out.print("Dimensions (lins=cols)? ");
            int lin = scanner.nextInt();
            int col = lin;

            switch (op) {
                case 1 -> onMult(lin, col);
                case 2 -> onMultLine(lin, col);
                case 3 -> {
                    System.out.print("Block size? ");
                    int bkSize = scanner.nextInt();
                    onMultBlock(lin, col, bkSize);
                }
                default -> System.out.println("Invalid option");
            }
        } while (op != 0);

        scanner.close();
    }
}