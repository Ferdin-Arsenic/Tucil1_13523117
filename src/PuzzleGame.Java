import java.io.*;
import java.util.*;

public class PuzzleGame {
    static final int MAX_SHAPE = 16;
    static final int MAX_ORIENTATIONS = 8;
    static Scanner scanner = new Scanner(System.in);

    static final String[] COLORS = {"\u001B[31m","\u001B[38;5;214m","\u001B[33m","\u001B[32m","\u001B[34m", "\u001B[38;5;63m","\u001B[35m",
                                    "\u001B[38;5;213m","\u001B[38;5;94m","\u001B[30m","\u001B[37m","\u001B[90m","\u001B[35m","\u001B[36m",
                                    "\u001B[38;5;37m","\u001B[38;5;13m","\u001B[38;5;214m","\u001B[38;5;220m","\u001B[38;5;8m","\u001B[38;5;58m",
                                    "\u001B[38;5;52m","\u001B[38;5;17m", "\u001B[38;5;209m","\u001B[38;5;37m","\u001B[38;5;88m","\u001B[38;5;230m"};
    static final String RESET = "\u001B[0m";

    static class Piece {
        char letter;
        char[][][] orientations = new char[MAX_ORIENTATIONS][MAX_SHAPE][MAX_SHAPE];
        int[] height = new int[MAX_ORIENTATIONS];
        int[] width = new int[MAX_ORIENTATIONS];
        boolean[] validOrientation = new boolean[MAX_ORIENTATIONS];
    }

    static char[][] board;
    static boolean[] used;
    static Piece[] pieces;
    static int N, M, P;
    static long casesChecked = 0;

    static void initializeBoard() {
        board = new char[N][M];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                board[i][j] = '.';
            }
        }
    }
    
    static boolean canPlacePiece(int x, int y, Piece piece, int orientation) {
        if (!piece.validOrientation[orientation]) return false;

        for (int i = 0; i < piece.height[orientation]; i++) {
            for (int j = 0; j < piece.width[orientation]; j++) {
                if (piece.orientations[orientation][i][j] == piece.letter) {
                    if (x + i >= N || y + j >= M || board[x + i][y + j] != '.') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static void placePiece(int x, int y, Piece piece, int orientation, char value) {
        for (int i = 0; i < piece.height[orientation]; i++) {
            for (int j = 0; j < piece.width[orientation]; j++) {
                if (piece.orientations[orientation][i][j] == piece.letter) {
                    board[x + i][y + j] = value;
                }
            }
        }
    }

    static void makeOrientations(Piece piece, List<String> shape) {
        int height = shape.size();
        int width = 0;
        for (String row : shape) {
            width = Math.max(width, row.length());
        }
        for (int i = 0; i < shape.size(); i++) {
            String row = shape.get(i);
            for (int j = 0; j < row.length(); j++) {
                piece.orientations[0][i][j] = row.charAt(j);
            }
        }
        piece.height[0] = height;
        piece.width[0] = width;
        piece.validOrientation[0] = true;

        for (int base = 0; base < 8; base += 4) {
            for (int rot = 1; rot < 4; rot++) {
                int curr = base + rot;
                int prev = base + rot - 1;

                int newHeight = piece.width[prev];
                int newWidth = piece.height[prev];
                
                for (int i = 0; i < piece.height[prev]; i++) {
                    for (int j = 0; j < piece.width[prev]; j++) {
                        piece.orientations[curr][j][newWidth - 1 - i] = piece.orientations[prev][i][j];
                    }
                }
                
                piece.height[curr] = newHeight;
                piece.width[curr] = newWidth;
                piece.validOrientation[curr] = true;
            }

            if (base == 0) {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        piece.orientations[4][i][width - 1 - j] = 
                            piece.orientations[0][i][j];
                    }
                }

                piece.height[4] = height;
                piece.width[4] = width;
                piece.validOrientation[4] = true;
            }
        }

        for (int i = 0; i < MAX_ORIENTATIONS; i++) {
            if (!piece.validOrientation[i]) continue;
            for (int j = 0; j < i; j++) {
                if (!piece.validOrientation[j]) continue;
                if (isSameOrientation(piece, i, j)) {
                    piece.validOrientation[i] = false;
                    break;
                }
            }
        }
    }

    static boolean isSameOrientation(Piece piece, int o1, int o2) {
        if (piece.height[o1] != piece.height[o2] || piece.width[o1] != piece.width[o2]) 
            return false;

        for (int i = 0; i < piece.height[o1]; i++) {
            for (int j = 0; j < piece.width[o1]; j++) {
                if (piece.orientations[o1][i][j] != piece.orientations[o2][i][j]) 
                    return false;
            }
        }
        return true;
    }

    static boolean solvePuzzle() {
        boolean allUsed = true;
        for (boolean isUsed : used) {
            if (!isUsed) {
                allUsed = false;
                break;
            }
        }
        if (allUsed) {
            boolean boardFull = true;
            for (int i = 0; i < N && boardFull; i++) {
                for (int j = 0; j < M; j++) {
                    if (board[i][j] == '.') {
                        boardFull = false;
                        break;
                    }
                }
            }
            return boardFull;
        }
        int startX = -1, startY = -1;
        boolean found = false;
        for (int i = 0; i < N && !found; i++) {
            for (int j = 0; j < M; j++) {
                if (board[i][j] == '.') {
                    startX = i;
                    startY = j;
                    found = true;
                    break;
                }
            }
        }

        if (!found) return false;

        for (int p = 0; p < P; p++) {
            if (used[p]) continue;

            for (int o = 0; o < MAX_ORIENTATIONS; o++) {
                if (!pieces[p].validOrientation[o]) continue;

                casesChecked++;
                
                if (canPlacePiece(startX, startY, pieces[p], o)) {
                    used[p] = true;
                    placePiece(startX, startY, pieces[p], o, pieces[p].letter);

                    if (solvePuzzle()) return true;

                    used[p] = false;
                    placePiece(startX, startY, pieces[p], o, '.');
                }
            }
        }

        return false;
    }

    static void printBoard() {
        Map<Character, Integer> colorMap = new HashMap<>();
        int colorIndex = 0;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                char c = board[i][j];
                if (c != '.') {
                    if (!colorMap.containsKey(c)) {
                        colorMap.put(c, colorIndex++ % COLORS.length);
                    }
                    System.out.print(COLORS[colorMap.get(c)] + c + RESET);
                } else {
                    System.out.print(c);
                }
            }
            System.out.println();
        }
    }

    static void saveSolution(String filename, long solveTime) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("test/" + filename))) {
            writer.println(N + " " + M + " " + P);
            writer.println();
            
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    writer.print(board[i][j]);
                }
                writer.println();
            }
            writer.println("\n");
            writer.println("Hasil: ");
            writer.println("Waktu penyelesaian: " + solveTime + " ms");
            writer.println("Kasus yang diperiksa: " + casesChecked);
            
        } catch (IOException e) {
            System.out.println("Error saat menyimpan solusi: " + e.getMessage());
        }
    }

    static boolean readPuzzle(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader("test/" + filename))) {
            String line;
            
            line = reader.readLine();
            while (line != null && line.trim().isEmpty()) {
                line = reader.readLine();
            }
            
            String[] dims = line.split(" ");
            
            if (dims.length != 3) {
                System.out.println("Informasi puzzle tidak lengkap! Harap dicek!\n");
                return false;
            }
    
            boolean checkIfInteger = true;
            for (String dim : dims) {
                try {
                    Integer.parseInt(dim);
                } catch (NumberFormatException e) {
                    checkIfInteger = false;
                    break;
                }
            }
    
            if (!checkIfInteger) {
                System.out.println("Informasi Puzzle tidak dalam angka! Coba dicek!\n");
                return false;
            }
    
            N = Integer.parseInt(dims[0]);
            M = Integer.parseInt(dims[1]);
            P = Integer.parseInt(dims[2]);
    
            if (N < 1) {
                System.out.println("Error: Jumlah baris papan tidak valid! Setidaknya papan memiliki 1 baris!\n");
                return false;
            }
            if (M < 1) {
                System.out.println("Error: Jumlah kolom papan tidak valid! Setidaknya kolom memiliki 1 baris!\n");
                return false;
            }
            if (P < 0 || P > 26) {
                System.out.println("Error: Banyaknya potongan puzzle tidak valid! Harus di antara 0 sampai 26!\n");
                return false;
            }
    
            line = reader.readLine();
            while (line != null && line.trim().isEmpty()) {
                line = reader.readLine();
            }
    
            if (line == null || !line.equals("DEFAULT")) {
                System.out.println("Error: Tipe papan tidak valid! Coba dicek!\n");
                return false;
            }
    
            initializeBoard();
            pieces = new Piece[P];
            used = new boolean[P];
    
            int filePiece = 0;
            String nextPieceFirstLine = null;
    
            while (true) {
                if (nextPieceFirstLine != null) {
                    line = nextPieceFirstLine;
                    nextPieceFirstLine = null;
                } else {
                    line = reader.readLine();
                }
    
                if (line == null) {
                    break;
                }
    
                if (line.trim().isEmpty()) {
                    continue; 
                }
    
                if (filePiece >= P) {
                    System.out.println("Error: Terlalu banyak potongan puzzle! Seharusnya hanya " + P + " potongan.\n");
                    return false;
                }
    
                pieces[filePiece] = new Piece();
                List<String> shape = new ArrayList<>();
    
                pieces[filePiece].letter = line.charAt(0);
                shape.add(line);
    
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        break;
                    }
    
                    if (line.length() > 0 && Character.isLetter(line.charAt(0)) && 
                        line.charAt(0) != pieces[filePiece].letter) {
                        nextPieceFirstLine = line;
                        break;
                    }
    
                    shape.add(line);
                }
    
                makeOrientations(pieces[filePiece], shape);
                filePiece++;
            }
    
            if (filePiece < P) {
                System.out.println("Error: Kekurangan potongan puzzle! Seharusnya ada " + P + " potongan, tapi hanya ada\n" + filePiece);
                return false;
            }
    
            System.out.println("Potongan puzzle berhasil dibaca!\n");
            return true;
    
        } catch (IOException e) {
            System.out.println("Error saat membaca file input: " + e.getMessage());
            return false;
        }
    }        

    static void showMainMenu() {
        while (true) {
            System.out.println("====== WELCOME TO ======\n");
            System.out.println("=== IQ Puzzle Solver ===\n");
            System.out.println("1. Selesaikan Puzzle");
            System.out.println("2. Keluar\n");
            System.out.print("Pilih (1-2): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    playPuzzle();
                    break;
                case "2":
                    System.out.println("Terima kasih sudah mampir!\n");
                    return;
                default:
                    System.out.println("Opsi tidak valid! Masukan opsi yang tersedia!\n");
            }
        }
    }
    
    static void playPuzzle() {
        System.out.print("Masukkan nama file puzzle lengkap dengan .txt: ");
        String filename = scanner.nextLine();
        try {
            if (!readPuzzle(filename)) {
                return;
            }

            System.out.println("Menyelesaikan puzzle...\n");
            long startTime = System.currentTimeMillis();
            boolean solved = solvePuzzle();
            long endTime = System.currentTimeMillis();
            long solveTime = endTime - startTime;
        
            if (solved) {
                System.out.println("Puzzle Selesai!\n");
                printBoard();
                System.out.println("\n");
                System.out.println("Waktu penyelesaian: " + solveTime + " ms");
                System.out.println("Kasus yang diperiksa: " + casesChecked);
        
                System.out.print("Apakah solusi ingin disimpan? (ya/tidak): \n");
                String save = scanner.nextLine();
                if (save.equalsIgnoreCase("ya")) {
                    System.out.print("Enter output filename: ");
                    String outFile = scanner.nextLine();
                    if (!outFile.toLowerCase().endsWith(".txt")) {
                        outFile += ".txt";
                    }
                    
                    saveSolution(outFile, solveTime);
                    System.out.println("Solusi tersimpan di " + outFile);
                }
            } else {
                System.out.println("Puzzle tidak dapat terselesaikan!\n");
                System.out.println("Waktu pengerjaan: " + solveTime + " ms");
                System.out.println("Kasus yang diperiksa: " + casesChecked);
            }
        } catch (Exception e) {
            System.out.println("Error saat pemecahan puzzle: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        showMainMenu();
        scanner.close();
    }
}    