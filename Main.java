package battleship;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Math.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final int FIELD_WIDTH = 10;
    private static final int FIELD_HEIGHT = 10;
    private static final int[][] player1Field = new int[FIELD_HEIGHT][FIELD_WIDTH];
    private static final int[][] player2Field = new int[FIELD_HEIGHT][FIELD_WIDTH];
    private static int player1AliveShips;
    private static int player2AliveShips;

    public static void main(String[] args) {
        initFields();
        initShips(player1Field, 1);
        initShips(player2Field, 2);

        while (!isFinish()) {
            System.out.println("Press Enter and pass the move to another player\n...");
            sc.nextLine();
            print(true, player2Field);
            System.out.println("---------------------");
            System.out.println();

            playerTurn(player1Field, player2Field, 1);
            if (isFinish()) break;

            System.out.println("Press Enter and pass the move to another player\n...");
            sc.nextLine();
            print(true, player1Field);
            System.out.println("---------------------");
            System.out.println();

            playerTurn(player2Field, player1Field, 2);
        }
    }

    private static boolean isValidShot(String shot) {
        if (shot.length() < 2 || shot.length() > 3) {
            return false;
        }

        char row = shot.charAt(0);
        int col;

        try {
            col = Integer.parseInt(shot.substring(1));
        } catch (NumberFormatException e) {
            return false; // Not a valid number for column
        }

        return row >= 'A' && row <= 'J' && col >= 1 && col <= 10;
    }



    private static void playerTurn(int[][] currentPlayerField, int[][] opponentField, int player) {
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (int i = 0; i < 10; i++) {
            System.out.print((char) ('A' + i) + " ");
            for (int j = 0; j < 10; j++) {
                System.out.print(printCell(currentPlayerField[i][j]) + " ");
            }
            System.out.println();
        }

        // Now prompt the player for their input
        System.out.printf("Player %d, it's your turn:\n", player);

        String shot = sc.nextLine().toUpperCase(); // Convert input to uppercase
        while (!isValidShot(shot)) {
            System.out.println("Invalid shot! Please enter a valid coordinate ");
            shot = sc.nextLine().toUpperCase();
        }

        processShot(shot, currentPlayerField, opponentField);
    }



    private static boolean isFinish() {
        return player1AliveShips == 0 || player2AliveShips == 0;
    }

    private static boolean processShot(String shot, int[][] currentPlayerField, int[][] opponentField) {
        boolean result = true;
        try {
            int y = shot.charAt(0) - 'A';
            int x = Integer.parseInt(shot.substring(1)) - 1;
            if (opponentField[y][x] > 0 || opponentField[y][x] == -1) {
                opponentField[y][x] = -1;
                if (isSankShip(opponentField)) {
                    print(true, opponentField);
                    System.out.println("You sank a ship");
                    if (isFinish()) {
                        print(false, opponentField);
                        System.out.println("You sank the last ship. You won. Congratulations!");
                    }
                } else {
                    print(true, opponentField);
                    System.out.println("You hit a ship");
                }
            } else if (opponentField[y][x] == 0 || opponentField[y][x] == -2) {
                opponentField[y][x] = -2;
                print(true, opponentField);
                System.out.println("You missed");
            }
        } catch (Exception e) {
            System.out.println("Invalid shot! Please enter a valid coordinate");
            result = false;
        }
        return result;
    }


    private static Set<Integer> hitShipsPlayer1 = new HashSet<>();
    private static Set<Integer> hitShipsPlayer2 = new HashSet<>();

    private static boolean isSankShip(int[][] opponentField) {
        Set<Integer> ships = new HashSet<>();
        for (int j = 0; j < FIELD_HEIGHT; j++) {
            for (int i = 0; i < FIELD_WIDTH; i++) {
                if (opponentField[j][i] > 0) {
                    ships.add(opponentField[j][i]);
                }
            }
        }
        if (opponentField == player1Field) {
            if (ships.size() < player1AliveShips) {
                hitShipsPlayer1.add(player1AliveShips - ships.size());
                player1AliveShips--;
                return true;
            }
        } else {
            if (ships.size() < player2AliveShips) {
                hitShipsPlayer2.add(player2AliveShips - ships.size());
                player2AliveShips--;
                return true;
            }
        }
        return false;
    }

    private static void initShips(int[][] field, int player) {
        print(false, field);
        TypeShip[] typeShips = TypeShip.values();
        for (int i = 0; i < typeShips.length; i++) {
            TypeShip typeShip = typeShips[i];
            System.out.printf("Player %d, enter the coordinates of the %s (%d cells):\n", player, typeShip.name, typeShip.length);
            String input = sc.nextLine();
            Ship ship = new Ship();
            if (ship.setCoordinates(input, typeShip)) {
                if (ship.length == typeShip.length) {
                    if (isPossibleLocate(ship, field)) {
                        fillField(ship, field);
                        if (player == 1) player1AliveShips++;
                        else player2AliveShips++;
                        print(false, field);
                    } else {
                        System.out.println("Error! You placed it too close to another one. Try again:");
                        i--;
                    }
                } else {
                    System.out.printf("Error! Wrong length of the %s! Try again:\n", typeShip.name);
                    i--;
                }
            } else {
                System.out.println("Error! Wrong ship location! Try again:");
                i--;
            }
        }
    }

    private static boolean isPossibleLocate(Ship ship, int[][] field) {
        int x = ship.x;
        int y = ship.y;
        for (int i = 0; i < ship.length; i++) {
            if (ship.direction == 0) {
                x = ship.x + i;
            } else {
                y = ship.y + i;
            }
            if (field[max(y - 1, 0)][max(x - 1, 0)] > 0
                    || field[max(y - 1, 0)][x] > 0
                    || field[max(y - 1, 0)][min(x + 1, FIELD_WIDTH - 1)] > 0
                    || field[y][max(x - 1, 0)] > 0
                    || field[y][x] > 0
                    || field[y][min(x + 1, FIELD_WIDTH - 1)] > 0
                    || field[min(y + 1, FIELD_HEIGHT - 1)][max(x - 1, 0)] < 0
                    || field[min(y + 1, FIELD_HEIGHT - 1)][x] > 0
                    || field[min(y + 1, FIELD_HEIGHT - 1)][min(x + 1, FIELD_WIDTH - 1)] > 0) {
                return false;
            }
        }
        return true;
    }

    private static void fillField(Ship ship, int[][] field) {
        int currentX = ship.x;
        int currentY = ship.y;
        for (int i = 0; i < ship.length; i++) {
            field[currentY][currentX] = ship.typeShip.index;
            if (ship.direction == 0) {
                currentX++;
            } else {
                currentY++;
            }
        }
    }

    private static void initFields() {
        initField(player1Field);
        initField(player2Field);
    }

    private static void initField(int[][] field) {
        for (int j = 0; j < FIELD_HEIGHT; j++) {
            for (int i = 0; i < FIELD_WIDTH; i++) {
                field[j][i] = 0;
            }
        }
    }

    private static void print(boolean fogOfWar, int[][] field) {
        System.out.print("  ");
        for (int i = 0; i < FIELD_WIDTH; i++) {
            System.out.printf("%d ", i + 1);
        }
        System.out.println();

        for (int j = 0; j < FIELD_HEIGHT; j++) {
            System.out.printf("%c ", j + 65);
            for (int i = 0; i < FIELD_WIDTH; i++) {
                System.out.printf("%s ", fogOfWar && field[j][i] > 0 ? printCell(0) : printCell(field[j][i]));
            }
            System.out.println();
        }
    }

    private static String printCell(int value) {
        if (value > 0) {
            return "O";
        } else if (value == 0) {
            return "~";
        } else if (value == -1) {
            return "X";
        } else {
            return "M";
        }
    }

    private static class Ship {

        private int x, y, length, direction;
        TypeShip typeShip;

        public boolean setCoordinates(String input, TypeShip typeShip) {
            try {
                String[] parts = input.split(" ");
                int y1 = parts[0].charAt(0) - 65;
                int y2 = parts[1].charAt(0) - 65;
                int x1 = Integer.parseInt(parts[0].substring(1)) - 1;
                int x2 = Integer.parseInt(parts[1].substring(1)) - 1;

                if (abs(x2 - x1) > 0 && abs(y2 - y1) > 0) {
                    return false;
                } else if (x1 < 0 || x1 > FIELD_WIDTH - 1) {
                    return false;
                } else if (x2 < 0 || x2 > FIELD_WIDTH - 1) {
                    return false;
                } else if (y1 < 0 || y1 > FIELD_HEIGHT - 1) {
                    return false;
                } else if (y2 < 0 || y2 > FIELD_HEIGHT - 1) {
                    return false;
                } else {
                    x = min(x1, x2);
                    y = min(y1, y2);
                    length = max(abs(x2 - x1), abs(y2 - y1)) + 1;
                    direction = (y1 == y2) ? 0 : 1;
                    this.typeShip = typeShip;
                    return true;
                }

            } catch (Exception ignored) {
                return false;
            }
        }
    }

    public enum TypeShip {

        AIRCRAFT_CARRIER("Aircraft Carrier", 5, 1),
        BATTLESHIP("Battleship", 4, 2),
        SUBMARINE("Submarine", 3, 3),
        CRUISER("Cruiser", 3, 4),
        DESTROYER("Destroyer", 2, 5);

        private final String name;
        private final int length;
        private final int index;

        TypeShip(String name, int length, int index) {
            this.name = name;
            this.length = length;
            this.index = index;
        }
    }
}
