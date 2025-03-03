import java.util.Random;
import java.util.Scanner;

/**
 * mi clase Battleship_1
 * La primera version del juego battleship de un jugador contra la maquina
 * @author Santiago Cabal
 * @version 1.0
 */
public class Battleship_1 {
    public static final int SIZE = 10;
    // Estados: 
    // 0: Agua, 1: Barco (solo visible en el propio tablero), 2: Tocado, 3: Hundido
    public static final char WATER = '0';
    public static final char SHIP = '1';
    public static final char HIT = '2';
    public static final char SUNK = '3';
    public static final char MISS = 'M';
    
    // Tableros (listas de 1d)
    public static char[] playerBoard = new char[SIZE];      // se ve 0,1,2,3
    public static char[] aiBoard = new char[SIZE];          // se oculta, se ve solo en hit/hundido
    public static char[] playerTracking = new char[SIZE];   // lo ve el jugador del enemigo (0,2,3)
    
    // Para evitar que se ataque la misma casilla (cada tablero tiene un seguimiento de ataques)
    public static boolean[] enemyAttacked = new boolean[SIZE];   // para aiBoard (jugador ataca)
    public static boolean[] playerAttacked = new boolean[SIZE];    // para playerBoard (ataque máquina)
    
    // Guardamos la posición inicial (mínima) y tamaño de cada barco para cada jugador
    // Orden: [0]: Lancha (1 casilla), [1]: Barco Médico (2 casillas), [2]: Barco de Munición (3 casillas)
    public static int[] playerShipStart = new int[3];
    public static int[] playerShipSize = new int[3];
    public static int[] enemyShipStart = new int[3];
    public static int[] enemyShipSize = new int[3];
    
    public static Random random = new Random();
    public static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        // Inicializar tableros
        initializeBoard(playerBoard);
        initializeBoard(aiBoard);
        initializeBoard(playerTracking);
        // Inicializar arrays de seguimiento de ataques
        for (int i = 0; i < SIZE; i++) {
            enemyAttacked[i] = false;
            playerAttacked[i] = false;
        }
        
        // Colocar barcos
        placeShipsAI();
        placeShipsPlayer();
    
        System.out.println("Tu línea de mar queda así:");
        printBoard(playerBoard, false);
        System.out.println("¡Muy bien! ¡Ahora vamos a jugar!");
        
        boolean gameOver = false;
        // Bucle de juego: primero el jugador, luego la máquina
        while (!gameOver) {
            System.out.println("\nTablero del enemigo:");
            printBoard(playerTracking, true);
            System.out.println("Dime el punto a atacar (1-" + SIZE + "):");
            int playerInput = scanner.nextInt();
            int playerAttack = playerInput - 1; // convertir a índice 0
            while (!attack(aiBoard, playerTracking, enemyAttacked, playerAttack, enemyShipStart, enemyShipSize)) {
                System.out.println("Dime el punto a atacar (1-" + SIZE + "):");
                playerInput = scanner.nextInt();
                playerAttack = playerInput - 1;
            }
            // Verificar si el jugador hundió todos los barcos del enemigo
            if (checkWin(aiBoard)) {
                System.out.println("¡Ganaste! Todos los barcos enemigos han sido hundidos.");
                gameOver = true;
                break;
            }
            
            // Turno de la máquina
            int aiAttack;
            do {
                aiAttack = random.nextInt(SIZE);
            } while (playerAttacked[aiAttack]);
            System.out.println("\nLa máquina ataca en la posición " + (aiAttack + 1));
            attack(playerBoard, null, playerAttacked, aiAttack, playerShipStart, playerShipSize);
            // Mostrar el tablero del jugador tras el ataque
            System.out.println("Tu línea de mar ahora:");
            printBoard(playerBoard, false);
            if (checkWin(playerBoard)) {
                System.out.println("¡La máquina gana! Todos tus barcos han sido hundidos.");
                gameOver = true;
            }
        }
    }
    
    // Inicializo un tablero llenandolo con Water
    public static void initializeBoard(char[] board) {
        for (int i = 0; i < SIZE; i++) {
            board[i] = WATER;
        }
    }
    
    // Imprime un tablero. Si es del enemigo (isEnemy==true), se muestran solo 0,2 y 3.
    public static void printBoard(char[] board, boolean isEnemy) {
        for (int i = 0; i < SIZE; i++) {
            char toPrint = board[i];
            if (isEnemy && toPrint == MISS) {
                toPrint = WATER;
            }
            System.out.print(toPrint + " ");
        }
        System.out.println();
    }
    
    // Coloca los tres barcos de la máquina de forma aleatoria sin solaparse.
    public static void placeShipsAI() {
        // Lancha (1 casilla)
        placeShipRandomly(aiBoard, enemyAttacked, enemyShipStart, enemyShipSize, 0, 1);
        // Barco Médico (2 casillas)
        placeShipRandomly(aiBoard, enemyAttacked, enemyShipStart, enemyShipSize, 1, 2);
        // Barco de Munición (3 casillas)
        placeShipRandomly(aiBoard, enemyAttacked, enemyShipStart, enemyShipSize, 2, 3);
    }
    
    // Coloca un barco aleatoriamente en el tablero AI sin solaparse.
    // El parámetro shipIndex indica en qué posición del arreglo de barcos se guarda.
    public static void placeShipRandomly(char[] board, boolean[] attacked, int[] shipStart, int[] shipSize, int shipIndex, int sizeShip) {
        boolean placed = false;
        while (!placed) {
            int pos = random.nextInt(SIZE - sizeShip + 1);
            boolean free = true;
            for (int i = pos; i < pos + sizeShip; i++) {
                if (board[i] != WATER) {
                    free = false;
                    break;
                }
            }
            if (free) {
                for (int i = pos; i < pos + sizeShip; i++) {
                    board[i] = SHIP;
                }
                shipStart[shipIndex] = pos;
                shipSize[shipIndex] = sizeShip;
                placed = true;
            }
        }
    }
    
    // Permite al jugador colocar sus tres barcos sin solaparse.
    public static void placeShipsPlayer() {
        System.out.println("Coloca tus barcos en el tablero.");
        // Lancha (1 casilla)
        System.out.println("Coloca tu Lancha (1 casilla):");
        int pos = getCoordinate("Ingresa la coordenada de la Lancha (1-" + SIZE + "): ");
        pos--; // convertir a índice 0
        while (playerBoard[pos] != WATER) {
            System.out.println("Posición ya ocupada. Intenta de nuevo.");
            pos = getCoordinate("Ingresa la coordenada de la Lancha (1-" + SIZE + "): ") - 1;
        }
        playerBoard[pos] = SHIP;
        playerShipStart[0] = pos;
        playerShipSize[0] = 1;
        System.out.println("La Lancha se colocará en la casilla " + (pos + 1) + ".");
        
        // Barco Médico (2 casillas)
        System.out.println("Coloca el Barco Médico (2 casillas):");
        int start = getCoordinate("Ingresa la coordenada de la primera casilla del Barco Médico (1-" + SIZE + "): ") - 1;
        int end = getCoordinate("Ingresa la coordenada de la última casilla del Barco Médico (1-" + SIZE + "): ") - 1;
        // Asegurarse de que sean consecutivas y de tamaño 2
        while (Math.abs(end - start) != 1) {
            System.out.println("Las coordenadas deben ser consecutivas para un barco de 2 casillas. Intenta de nuevo.");
            start = getCoordinate("Ingresa la coordenada de la primera casilla del Barco Médico (1-" + SIZE + "): ") - 1;
            end = getCoordinate("Ingresa la coordenada de la última casilla del Barco Médico (1-" + SIZE + "): ") - 1;
        }
        int sMed = Math.min(start, end);
        int eMed = Math.max(start, end);
        // Verificar que las casillas estén libres
        for (int i = sMed; i <= eMed; i++) {
            if (playerBoard[i] != WATER) {
                System.out.println("Alguna posición ya está ocupada. Vuelve a colocar el Barco Médico.");
                sMed = -1; // para repetir
                break;
            }
        }
        while (sMed < 0) {
            start = getCoordinate("Ingresa la coordenada de la primera casilla del Barco Médico (1-" + SIZE + "): ") - 1;
            end = getCoordinate("Ingresa la coordenada de la última casilla del Barco Médico (1-" + SIZE + "): ") - 1;
            if (Math.abs(end - start) != 1) {
                System.out.println("Las coordenadas deben ser consecutivas para un barco de 2 casillas. Intenta de nuevo.");
                continue;
            }
            sMed = Math.min(start, end);
            eMed = Math.max(start, end);
            boolean free = true;
            for (int i = sMed; i <= eMed; i++) {
                if (playerBoard[i] != WATER) {
                    free = false;
                    break;
                }
            }
            if (!free) {
                System.out.println("Alguna posición ya está ocupada. Vuelve a colocar el Barco Médico.");
                sMed = -1;
            }
        }
        for (int i = sMed; i <= eMed; i++) {
            playerBoard[i] = SHIP;
        }
        playerShipStart[1] = sMed;
        playerShipSize[1] = 2;
        System.out.println("El Barco Médico se colocará en las casillas de la " + (sMed + 1) + " a la " + (eMed + 1) + ".");
        
        // Barco de Munición (3 casillas)
        System.out.println("Coloca el Barco de Munición (3 casillas):");
        int start3 = getCoordinate("Ingresa la coordenada de la primera casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
        int end3 = getCoordinate("Ingresa la coordenada de la última casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
        while (Math.abs(end3 - start3) != 2) {
            System.out.println("Las coordenadas deben ser consecutivas para un barco de 3 casillas. Intenta de nuevo.");
            start3 = getCoordinate("Ingresa la coordenada de la primera casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
            end3 = getCoordinate("Ingresa la coordenada de la última casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
        }
        int sMun = Math.min(start3, end3);
        int eMun = Math.max(start3, end3);
        for (int i = sMun; i <= eMun; i++) {
            if (playerBoard[i] != WATER) {
                System.out.println("Alguna posición ya está ocupada. Vuelve a colocar el Barco de Munición.");
                sMun = -1;
                break;
            }
        }
        while (sMun < 0) {
            start3 = getCoordinate("Ingresa la coordenada de la primera casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
            end3 = getCoordinate("Ingresa la coordenada de la última casilla del Barco de Munición (1-" + SIZE + "): ") - 1;
            if (Math.abs(end3 - start3) != 2) {
                System.out.println("Las coordenadas deben ser consecutivas para un barco de 3 casillas. Intenta de nuevo.");
                continue;
            }
            sMun = Math.min(start3, end3);
            eMun = Math.max(start3, end3);
            boolean free = true;
            for (int i = sMun; i <= eMun; i++) {
                if (playerBoard[i] != WATER) {
                    free = false;
                    break;
                }
            }
            if (!free) {
                System.out.println("Alguna posición ya está ocupada. Vuelve a colocar el Barco de Munición.");
                sMun = -1;
            }
        }
        for (int i = sMun; i <= eMun; i++) {
            playerBoard[i] = SHIP;
        }
        playerShipStart[2] = sMun;
        playerShipSize[2] = 3;
        System.out.println("El Barco de Munición se colocará en las casillas de la " + (sMun + 1) + " a la " + (eMun + 1) + ".");
    }
    
    // Método auxiliar para pedir una coordenada
    public static int getCoordinate(String prompt) {
        System.out.print(prompt);
        return scanner.nextInt();
    }
    
    // Realiza un ataque en el tablero "board" en la posición indicada.
    // El parámetro "tracking" se usa para actualizar la visualización del enemigo (puede ser null si se ataca el tablero propio).
    // El array "attacked" es para verificar casillas ya atacadas.
    // shipStart y shipSize son los arreglos correspondientes al dueño del tablero (para actualizar estado a SUNK).
    public static boolean attack(char[] board, char[] tracking, boolean[] attacked, int position, int[] shipStart, int[] shipSize) {
        if (position < 0 || position >= SIZE) {
            System.out.println("Ataque fuera de límites. Intenta de nuevo.");
            return false;
        }
        if (attacked[position]) {
            System.out.println("Ya atacaste esta casilla. Intenta otro lugar.");
            return false;
        }
        attacked[position] = true;
        
        if (board[position] == SHIP) {
            board[position] = HIT;
            if (tracking != null) {
                tracking[position] = HIT;
            }
            System.out.println("¡Impacto!");
            updateSunkStatus(board, shipStart, shipSize, position, tracking);
        } else {
            System.out.println("Agua...");
            // Si se ataca el tablero enemigo, marcamos el fallo (internamente) para evitar repetir
            if (tracking != null) {
                tracking[position] = MISS;
            }
        }
        return true;
    }
    
    // Revisa, para el barco que incluya la casilla "position", si todas sus casillas han sido tocadas.
    // En ese caso, las marca como hundidas (SUNK) y muestra un mensaje.
    public static void updateSunkStatus(char[] board, int[] shipStart, int[] shipSize, int position, char[] tracking) {
        for (int i = 0; i < shipStart.length; i++) {
            int start = shipStart[i];
            int size = shipSize[i];
            if (position >= start && position < start + size) {
                boolean sunk = true;
                for (int j = start; j < start + size; j++) {
                    if (board[j] != HIT && board[j] != SUNK) {
                        sunk = false;
                        break;
                    }
                }
                if (sunk) {
                    for (int j = start; j < start + size; j++) {
                        board[j] = SUNK;
                        if (tracking != null) {
                            tracking[j] = SUNK;
                        }
                    }
                    if (board == aiBoard) {
                        System.out.println("¡Has hundido un barco enemigo!");
                    } else {
                        System.out.println("¡Han hundido uno de tus barcos!");
                    }
                }
            }
        }
    }
    
    // Verifica si en el tablero ya solo quedan agua (WATER) o barcos hundidos (SUNK)
    public static boolean checkWin(char[] board) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i] == SHIP || board[i] == HIT) {
                return false;
            }
        }
        return true;
    }
}