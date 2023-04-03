package packingBoxing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Robin Dai
 *
 * N orderline, different size Xn, Yn, Zn, each Orderline quantity is Mn
 *
 * unit: inch
 *
 * (X1*Y1*Z1*M1+X2*Y2*Z2*M2+…+Xn*Yn*Zn*Mn) + a = X*Y*Z
 * X < 108
 * X+2Y+2Z < 165
 */

public class packingBoxing {
    // To test place as much production as possible
    private static final int MAX_SIZE = 165000;
    private static final int MAX_LENGTH = 108000;
    // UPS limit
//    private static final int MAX_SIZE = 165;
//    private static final int MAX_LENGTH = 108;
    private static int boxMaxX = 0, boxMinX = 0, boxMaxY = 0, boxMinY = 0, boxMaxZ = 0, boxMinZ = 0;
    private static int currentBoxMaxX = boxMaxX, currentBoxMinX = boxMinX, currentBoxMaxY = boxMaxY,
            currentBoxMinY = boxMinY, currentBoxMaxZ = boxMaxZ, currentBoxMinZ = boxMinZ;
    private static int newPositionBoxMaxX = boxMaxX, newPositionBoxMinX = boxMinX, newPositionBoxMaxY = boxMaxY,
            newPositionBoxMinY = boxMinY, newPositionBoxMaxZ = boxMaxZ, newPositionBoxMinZ = boxMinZ;
    private static int newPositionLength = 0, newPositionWight = 0, newPositionHeight = 0, newPositionVolume = 0, newPositionSize = 0;
    private static Production finalBox = setDefaultBox(null);
    private static List<Production> finalProductions = new ArrayList<>();
    private static List<Point> finalProductionPoints = new ArrayList<>();
    private static Gson gson = new Gson();

    private static Production setDefaultBox(@Nullable Production box) {
        if (box == null) {
            box = new Production();
        }
        box.setLength(0); box.setWidth(0); box.setHeight(0);
        box.setQuantity(1); box.setSize(); box.setVolume();
        return box;
    }

    /**
     * main method
     * @param productions
     * @return
     */
    public static int minBoxingSize(List<Production> productions) {
        // arrange productions, max volume first
        Collections.sort(productions, (a, b) -> {
            a.setVolume();
            b.setVolume();
            return b.getVolume() - a.getVolume();
        });

        // put each product in box
        for (Production production : productions) {
            putNextProduction(production);
        }

        // INFO, print space utilization
        if (finalProductions.size() > 0) {
            int productionVolume = 0;
            for (Production production : finalProductions) {
                productionVolume += production.getVolume();
            }
            System.out.println(finalBox.getQuantity() + " productions has been put in.");
            System.out.println("The final box size is {Length: " + finalBox.getLength() + ", width: " + finalBox.getWidth() + ", height: " + finalBox.getHeight() + "}");
            System.out.println("Space utilization: " + (double) productionVolume / finalBox.getVolume());
        }
        return finalBox.getSize();
    }

    /**
     * arrange a, b, c DESC
     * @param a
     * @param b
     * @param c
     * @return {size, length, width, height, volume}
     */
    private static int[] calculateSize(int a, int b, int c) {
        int[] result = {0,0,0,0,0};
        int temp;
        if(a<b) {
            temp=a;
            a=b;
            b=temp;
        }
        if(a<c) {
            temp=a;
            a=c;
            c=temp;
        }
        if(b<c) {
            temp=b;
            b=c;
            c=temp;
        }
        // size
        result[0] = a + b * 2 + c * 2;
        result[1] = a;
        result[2] = b;
        result[3] = c;
        // volume
        result[4] = a * b * c;
        return result;
    }

    /**
     * Only set Length, Width, Height, Size, Volume
     */
    private static void setFinalBox (@NotNull int length, int width, int height) {
        finalBox.setLength(length);
        finalBox.setWidth(width);
        finalBox.setHeight(height);
        finalBox.setSize();
        finalBox.setVolume();
    }

    /**
     * main put a production in
     */
    private static void putNextProduction(Production production) {
        if (production.getSize() > MAX_SIZE || production.getLength() > MAX_LENGTH) {
            // oversize
            return;
        }
        if (finalProductions.size() == 0) {
            // set default size if is the first box to put in
            production.setPoints(generateNewPositionPoint(new Point(0, 0, 0), production.getLength(), production.getWidth(), production.getHeight()));
            setFinalBox(production.getLength(), production.getWidth(), production.getHeight());
            finalBox.setPoints(production.getPoints());
            finalProductions.add(production);
            finalProductionPoints.addAll(finalBox.getPoints());
            int[] boxBorder = setNewBoxBorder(finalBox.getPoints(), boxMaxX, boxMinX, boxMaxY, boxMinY, boxMaxZ, boxMinZ);
            boxMaxX = boxBorder[0]; boxMinX = boxBorder[1]; boxMaxY = boxBorder[2]; boxMinY = boxBorder[3]; boxMaxZ = boxBorder[4]; boxMinZ = boxBorder[5];
            currentBoxMaxX = boxMaxX; currentBoxMinX = boxMinX; currentBoxMaxY = boxMaxY; currentBoxMinY = boxMinY; currentBoxMaxZ = boxMaxZ; currentBoxMinZ = boxMinZ;
            newPositionBoxMaxX = boxMaxX; newPositionBoxMinX = boxMinX; newPositionBoxMaxY = boxMaxY;
            newPositionBoxMinY = boxMinY; newPositionBoxMaxZ = boxMaxZ; newPositionBoxMinZ = boxMinZ;
            finalBox.setPoints(production.getPoints());
        } else {
            // put new box
            List<Point> currentProductionPoints = new ArrayList<>();
            int currentLength = 0, currentWidth = 0, currentHeight = 0, currentVolume = 0, currentSize = 0;
            int [] originalProductionMeasures = {production.getLength(), production.getWidth(), production.getHeight()};
            // Loop to try every position
            for (Point point : finalProductionPoints) {
                List<Point> newProductionPoints = new ArrayList<>();
                int originalLengthIndex = 0, originalWidthIndex = 1, originalHeightIndex = 2, switchCoefficient = 3;
                for (int j = 0; j < 2; j++) {
                    if (j != 0) {
                        originalLengthIndex = 2; originalHeightIndex = 0;
                    }
                    for (int k = 0; k < switchCoefficient; k++) {
                        int newLengthIndex = originalLengthIndex - k < 0 ? originalLengthIndex - k + switchCoefficient : originalLengthIndex - k;
                        int newWidthIndex = originalWidthIndex - k < 0 ? originalWidthIndex - k + switchCoefficient : originalWidthIndex - k;
                        int newHeightIndex = originalHeightIndex - k < 0 ? originalHeightIndex - k + switchCoefficient : originalHeightIndex - k;
                        production.setLength(originalProductionMeasures[newLengthIndex]);
                        production.setWidth(originalProductionMeasures[newWidthIndex]);
                        production.setHeight(originalProductionMeasures[newHeightIndex]);
                        for (int x = 0; x < 2; x++) {
                            for (int y = 0; y < 2; y++) {
                                for (int z = 0; z < 2; z++) {
                                    int length = x == 0 ? production.getLength() : -production.getLength();
                                    int width = y == 0 ? production.getWidth() : -production.getWidth();
                                    int height = z == 0 ? production.getHeight() : -production.getHeight();
                                    production.setPoints(generateNewPositionPoint(point, length, width, height));
                                    if (isBetterAvailable(production)) {
                                        newProductionPoints = jsonToArrayList(gson.toJson(production.getPoints()), Point.class);
                                    }
                                }
                            }
                        }
                    }
                }

                if (newPositionVolume != 0 &&
                        (currentVolume == 0 ||
                                (newPositionVolume < currentVolume ||
                                        (newPositionVolume == currentVolume && newPositionSize < currentSize)))) {
                    // has better position
                    currentLength = newPositionLength; currentWidth = newPositionWight; currentHeight = newPositionHeight;
                    currentVolume = newPositionVolume; currentSize = newPositionSize;
                    currentProductionPoints = jsonToArrayList(gson.toJson(newProductionPoints), Point.class);
                    currentBoxMaxX = newPositionBoxMaxX; currentBoxMinX = newPositionBoxMinX; currentBoxMaxY = newPositionBoxMaxY;
                    currentBoxMinY = newPositionBoxMinY; currentBoxMaxZ = newPositionBoxMaxZ; currentBoxMinZ = newPositionBoxMinZ;
                }
                // reset point and box border
                newPositionLength = 0; newPositionWight = 0; newPositionHeight = 0; newPositionVolume = 0; newPositionSize = 0;
                newPositionBoxMaxX = boxMaxX; newPositionBoxMinX = boxMinX; newPositionBoxMaxY = boxMaxY;
                newPositionBoxMinY = boxMinY; newPositionBoxMaxZ = boxMaxZ; newPositionBoxMinZ = boxMinZ;
            }
            if (currentVolume != 0) {
                // could put in
                setFinalBox(currentLength, currentWidth, currentHeight);
                finalBox.setQuantity(finalBox.getQuantity() + 1);
                production.setPoints(jsonToArrayList(gson.toJson(currentProductionPoints), Point.class));
                finalProductions.add(production);
                boxMaxX = currentBoxMaxX; boxMinX = currentBoxMinX; boxMaxY = currentBoxMaxY; boxMinY = currentBoxMinY; boxMaxZ = currentBoxMaxZ; boxMinZ = currentBoxMinZ;
                List<Point> boxPoints = generateCurrentBoxPoints(boxMaxX, boxMinX, boxMaxY, boxMinY, boxMaxZ, boxMinZ);
                finalBox.setPoints(boxPoints);
                // finalProductionPoints only contains endpoint
                List<Point> pointToAdd = getDistinctPoints(finalProductionPoints, currentProductionPoints);
                List<Point> pointFromOld = getDistinctPoints(currentProductionPoints, finalProductionPoints);
                finalProductionPoints = Stream.concat(pointToAdd.stream(), pointFromOld.stream()).collect(Collectors.toList());
                finalProductionPoints.sort((a, b) -> a.getZ() - b.getZ() != 0 ? a.getZ() - b.getZ()
                        : (a.getX() - b.getX() != 0 ? a.getX() - b.getX() : a.getY() - b.getY()));
            } else {
                // reset current box border
                currentBoxMaxX = boxMaxX; currentBoxMinX = boxMinX; currentBoxMaxY = boxMaxY; currentBoxMinY = boxMinY; currentBoxMaxZ = boxMaxZ; currentBoxMinZ = boxMinZ;
            }
            newPositionBoxMaxX = boxMaxX; newPositionBoxMinX = boxMinX; newPositionBoxMaxY = boxMaxY;
            newPositionBoxMinY = boxMinY; newPositionBoxMaxZ = boxMaxZ; newPositionBoxMinZ = boxMinZ;

//             INFO, checkpoint
//            System.out.println("One production tried with volume: " + production.getVolume());
        }
    }

    private static int [] setNewBoxBorder(List<Point> pointList, int currentMaxX, int currentMinX, int currentMaxY, int currentMinY, int currentMaxZ, int currentMinZ) {
        int maxX = currentMaxX, minX = currentMinX, maxY = currentMaxY, minY = currentMinY, maxZ = currentMaxZ, minZ = currentMinZ;
        for (Point point : pointList) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();
            maxX = Math.max(maxX, x);
            minX = Math.min(minX, x);
            maxY = Math.max(maxY, y);
            minY = Math.min(minY, y);
            maxZ = Math.max(maxZ, z);
            minZ = Math.min(minZ, z);
        }
        return new int[]{maxX, minX, maxY, minY, maxZ, minZ};
    }

    private static List<Point> generateNewPositionPoint(Point originalPoint, int length, int width, int height) {
        List<Point> points = new ArrayList<>();
        points.add(originalPoint);
        points.add(new Point(originalPoint.getX() + length, originalPoint.getY(), originalPoint.getZ()));
        points.add(new Point(originalPoint.getX() + length, originalPoint.getY() + width, originalPoint.getZ()));
        points.add(new Point(originalPoint.getX() + length, originalPoint.getY() + width, originalPoint.getZ() + height));
        points.add(new Point(originalPoint.getX(), originalPoint.getY() + width, originalPoint.getZ()));
        points.add(new Point(originalPoint.getX(), originalPoint.getY() + width, originalPoint.getZ() + height));
        points.add(new Point(originalPoint.getX(), originalPoint.getY(), originalPoint.getZ() + height));
        points.add(new Point(originalPoint.getX() + length, originalPoint.getY(), originalPoint.getZ() + height));
        return points;
    }

    private static List<Point> generateCurrentBoxPoints(int currentMaxX, int currentMinX, int currentMaxY, int currentMinY, int currentMaxZ, int currentMinZ) {
        List<Point> currentBoxPoints = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    int x = i == 0 ? currentMinX : currentMaxX;
                    int y = j == 0 ? currentMinY : currentMaxY;
                    int z = k == 0 ? currentMinZ : currentMaxZ;
                    Point endPoint = new Point();
                    endPoint.setX(x);
                    endPoint.setY(y);
                    endPoint.setZ(z);
                    currentBoxPoints.add(endPoint);
                }
            }
        }
        return currentBoxPoints;
    }

    private static List<Point> getDistinctPoints(List<Point> originalPoints, List<Point> pointsToCompare) {
        List<Point> pointsToAdd = new ArrayList<>();
        for (int i = 0; i < pointsToCompare.size(); i++) {
            boolean isSamePoint = false;
            Point currentPoint = pointsToCompare.get(i);
            for (int j = 0; j < originalPoints.size(); j++) {
                Point originalPoint = originalPoints.get(j);
                if (originalPoint.equals(currentPoint)) {
                    isSamePoint = true;
                    break;
                }
            }
            if (!isSamePoint) {
                pointsToAdd.add(currentPoint);
            }
        }
        return pointsToAdd;
    }

    /**
     * over lapping judge with endpoint, use coordinate system
     * @param boxAPoints
     * @param boxBPoints
     * @return
     */
    private static boolean isTwoBoxOverlapping(List<Point> boxAPoints, List<Point> boxBPoints) {
        int[] boxABorders = setNewBoxBorder(boxAPoints, boxAPoints.get(0).getX(), boxAPoints.get(0).getX(),
                boxAPoints.get(0).getY(), boxAPoints.get(0).getY(), boxAPoints.get(0).getZ(), boxAPoints.get(0).getZ());
        int[] boxBBorders = setNewBoxBorder(boxBPoints, boxBPoints.get(0).getX(), boxBPoints.get(0).getX(),
                boxBPoints.get(0).getY(), boxBPoints.get(0).getY(), boxBPoints.get(0).getZ(), boxBPoints.get(0).getZ());
        return (boxABorders[1] < boxBBorders[0] && boxABorders[0] > boxBBorders[1])
                && (boxABorders[3] < boxBBorders[2] && boxABorders[2] > boxBBorders[3])
                && (boxABorders[5] < boxBBorders[4] && boxABorders[4] > boxBBorders[5]);
    }

    private static boolean isOverlapping(Production production) {
        List<Point> productionPoints = production.getPoints();
        for (Production currentFinalProduction: finalProductions) {
            if (isTwoBoxOverlapping(currentFinalProduction.getPoints(), productionPoints)) {
                // return if already over lapping
                return true;
            }
        }
        return false;
    }

    private static boolean isBetterAvailable(Production production) {
        int newVolume, newSize, newLength, newWidth, newHeight;
        if (!isOverlapping(production)) {
            // if not over lapping
            int[] boxBorder = setNewBoxBorder(production.getPoints(), boxMaxX, boxMinX, boxMaxY, boxMinY, boxMaxZ, boxMinZ);
            int maxX = boxBorder[0], minX = boxBorder[1], maxY = boxBorder[2], minY = boxBorder[3], maxZ = boxBorder[4], minZ = boxBorder[5];
            newLength = (maxX - minX); newWidth = (maxY - minY); newHeight = (maxZ - minZ);
            // calculate size and volume, get new length, width, and height
            int[] newValue = calculateSize(newLength, newWidth, newHeight);
            newSize = newValue[0]; newLength = newValue[1]; newWidth = newValue[2]; newHeight = newValue[3]; newVolume = newValue[4];
            if (newSize > MAX_SIZE || newLength > MAX_LENGTH) {
                // oversize
                return false;
            }
            // compare if new position is more effective
            if ((newVolume < newPositionVolume || newPositionVolume == 0) ||
                    (newVolume == newPositionVolume && (newSize < newPositionSize || newPositionSize == 0))) {
                newPositionLength = newLength;
                newPositionWight = newWidth;
                newPositionHeight = newHeight;
                newPositionVolume = newVolume;
                newPositionSize = newSize;
                newPositionBoxMaxX = boxBorder[0]; newPositionBoxMinX = boxBorder[1]; newPositionBoxMaxY = boxBorder[2];
                newPositionBoxMinY = boxBorder[3]; newPositionBoxMaxZ = boxBorder[4]; newPositionBoxMinZ = boxBorder[5];
                return true;
            }
        }
        return false;
    }

    /**
     * Deep clone a list
     */
    private static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList <T> arrayList = new ArrayList<>();
        for (JsonObject jsonObject: jsonObjects) {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }

    public static void main(String[] args) {
        List<Production> productionList = getProductionList(10);

        long startTime = System.currentTimeMillis();
        minBoxingSize(productionList);
        long endTime = System.currentTimeMillis();
        System.out.println("Calculate time: " + (endTime - startTime) + "ms.");

        // INFO, print each production's Point
//        for (int i = 0; i < finalProductions.size(); i++) {
//            Production production = finalProductions.get(i);
//            System.out.format("Production %d. \n", production.getIndex());
//            for (Point point: production.getPoints()) {
//                point.print();
//            }
//        }
    }

    /**
     * generate production with UPS limit
     */
    public static List<Production> getProductionList(int boxCount) {
        List<Production> productionList = new ArrayList<>(boxCount);
        for (int i = 0; i < boxCount; i++) {
            Production production = new Production();
            int length = (int) (Math.random() * 107) + 1;
            int leftSize = (165 - 107) / 2;
            int width = (int) (Math.random() * (Math.min(length, leftSize)) + 1);
            int leftHeight = leftSize - width;
            int height = (int) (Math.random() * (Math.min(width, leftHeight)) + 1);
            production.setLength(length);
            production.setWidth(width);
            production.setHeight(height);
            production.setVolume();
            production.setSize();
            production.setQuantity(1);
            production.setIndex(i);
            productionList.add(production);

            // print production information
            System.out.println("Information of production " + i + " is length, width, height is ( " + length + ", " + width + ", " + height +" )");
        }
        return productionList;
    }
}
