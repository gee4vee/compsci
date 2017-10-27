

public class ArrayStuff {
    public static void main(String[] args) {
        int[][] image = { { 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 0, 1 }, { 1, 1, 1, 0, 0, 0, 1 }, // 3,5
                { 1, 1, 1, 1, 1, 1, 1 } };

        System.out.println(image.length);
        System.out.println(image[0].length);
        int[] result = findRect(image);
        System.out.println("x,y=" + result[0] + "," + result[1] + ", width=" + result[2] + ", height=" + result[3]);
    }

    /**
     * Imagine we have an image. We’ll represent this image as a simple 2D array where every pixel is a 1 or a 0. The image you get 
     * is known to have a single rectangle of 0s on a background of 1s. Write a function that takes in the image and returns the coordinates 
     * of the rectangle -- either top-left and bottom-right; or top-left, width, and height.
     */
    public static int[] findRect(int[][] image) {
        int topLeftX = 0, topLeftY = 0, width = 0, height = 0;
        boolean foundRect = false;

        for (int i = 0, io = image.length - 1; i < image.length && io >= 0; i++, io--) {
            for (int j = 0, jo = image[i].length - 1; j < image[i].length && jo >= 0; j++, jo--) {
                System.out.println(i);
                System.out.println(j);
                System.out.println(io);
                System.out.println(jo);
                if (i == image.length || (image[i][j] == 1 && image[io][jo] == 1)) {
                    continue;
                }

                if (image[i][j] == 0) {
                    topLeftX = i;
                    topLeftY = j;
                    // search right to find width
                    int nextJ = j++;
                    while (image[i][nextJ] != 1 && nextJ < image[i].length) {
                        width++;
                        nextJ++;
                    }
                    // search down to find height
                    int nextI = i;
                    while (image[nextI][j] != 1 && nextI < image.length) {
                        height++;
                        nextI++;
                    }
                    foundRect = true;
                    break;
                }

                if (image[io][jo] == 0) {
                    topLeftX = io;
                    topLeftY = jo;
                    // search left to find width
                    int nextJ = jo--;
                    while (image[io][nextJ] != 1 && nextJ >= 0) {
                        width++;
                        nextJ--;
                    }
                    // search up to find height
                    int nextI = io--;
                    while (image[nextI][jo] != 1 && nextI >= 0) {
                        height++;
                        nextI--;
                    }

                    // TODO this is still not working correctly (off by 1 or 2).
                    topLeftX -= width;
                    topLeftY -= height - 1;

                    foundRect = true;
                    break;
                }
            }

            if (foundRect) {
                break;
            }
        }

        int[] result = new int[] { topLeftX, topLeftY, width, height };
        return result;
    }
}
