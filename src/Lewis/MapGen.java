package Lewis;

import java.util.Random;

public class MapGen {

    public static int[][] makeMap() {

        int[][] map;
        int minRoomWidth = 2; //absolute minimum is 1
        int maxRoomWidth = 10;
        int minRoomHeight = 2; //absolute minimum is 1
        int maxRoomHeight = 6; //absolute maximum is 6
        int roomNumber = 0;
        Random rand = new Random();

        map = new int[17][401];
        for (int i = 0; i <= 16; i++) {
            for (int j = 0; j <= 400; j++) {
                map[i][j] = 0; //initialises everything in map[][]
            }
        }


        int i = 2;
        {
            for (int j = 1; j <= 399; j++) { //checks tiles for column 6
                int randomRoomWidth;
                do {
                    randomRoomWidth = rand.nextInt(maxRoomWidth + 1);
                } while (randomRoomWidth < minRoomWidth && randomRoomWidth != 0);
                int randomRoomHeight;
                do {
                    randomRoomHeight = rand.nextInt(maxRoomHeight + 1);
                } while (randomRoomHeight < minRoomHeight);
                if (map[3 * i][j] == 0) {
                    if (map[3 * i][j - 1] == 0) {
                        if (map[3 * i][j + 1] == 0) {
                            if (j + randomRoomWidth <= 400) {
                                if (randomRoomWidth != 0) {
                                    roomNumber++;
                                    if (roomNumber > 1) {
                                        int randomDoorwayHeight = rand.nextInt(randomRoomHeight);
                                        for (int corridorWidthI = 1; map[(3 * i) - randomDoorwayHeight][j - corridorWidthI] == 0 && j - corridorWidthI != 1; corridorWidthI++) {
                                            map[(3 * i) - randomDoorwayHeight][j - corridorWidthI] = 128;
                                        }
                                    }
                                }
                                for (int a = 0; a <= (randomRoomWidth - 1); a++) {
                                    for (int b = -(randomRoomHeight - 1); b <= 0; b++) {
                                        map[(3 * i) + b][j + a] = 128;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        i = 3;
        roomNumber = 0;
        {
            for (int j = 1; j <= 399; j++) { //checks tiles for column 9
                int randomRoomWidth;
                do {
                    randomRoomWidth = rand.nextInt(maxRoomWidth + 1);
                } while (randomRoomWidth < minRoomWidth && randomRoomWidth != 0);
                int randomRoomHeight;
                do {
                    randomRoomHeight = rand.nextInt(maxRoomHeight + 1);
                } while (randomRoomHeight < minRoomHeight);
                if (map[3 * i][j] == 0) {
                    if (map[3 * i][j - 1] == 0) {
                        if (map[3 * i][j + 1] == 0) {
                            if (j + randomRoomWidth <= 400) {
                                if (randomRoomWidth != 0) {
                                    roomNumber++;
                                    int randomDoorwayWidth = rand.nextInt(randomRoomWidth);
                                    for (int corridorHeightI = 1; map[(3 * i) - corridorHeightI][j + randomDoorwayWidth] == 0 && (3 * i) - corridorHeightI != 1; corridorHeightI++) {
                                        map[(3 * i) - corridorHeightI][j + randomDoorwayWidth] = 128;
                                    }
                                    if (roomNumber > 1) {
                                        int randomDoorwayHeight = rand.nextInt(randomRoomHeight);
                                        for (int corridorWidthI = 1; map[(3 * i) + randomDoorwayHeight][j - corridorWidthI] == 0 && j - corridorWidthI != 1; corridorWidthI++) {
                                            map[(3 * i) + randomDoorwayHeight][j - corridorWidthI] = 128;
                                        }
                                    }
                                }
                                for (int a = 0; a <= (randomRoomWidth - 1); a++) {
                                    for (int b = (randomRoomHeight - 1); b >= 0; b--) {
                                        map[(3 * i) + b][j + a] = 128;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //for (i = 0; i <= 15; i++) { //outputs arrays
            //    System.out.println(Arrays.toString(map[i]));
            //    System.out.println(Arrays.toString(map[i]));
            //}


            int[][] fudge = new int[401][17];
            for (int fudgeI = 0; fudgeI < 401; fudgeI++) {
                for (int fudgeJ = 0; fudgeJ < 17; fudgeJ++) {
                    fudge[fudgeI][fudgeJ] = map[fudgeJ][fudgeI] == 128 ? 0 : 128;
                }
            }

            return fudge;
        }
    }
}