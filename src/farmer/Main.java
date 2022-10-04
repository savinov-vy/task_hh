package farmer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
//        long l = System.currentTimeMillis();
        List<Point> points = new ArrayList<>(10000);
        List<Region> regions = new ArrayList<>(10000);
        fill(points);
        setRegions(points, regions, 1);
        Integer result = selectBestAreaAndGetCountPoint(regions);
        System.out.println(result);
//        System.out.println(System.currentTimeMillis() - l);
    }

    private static void setRegions(List<Point> allPoints, List<Region> regions, int numberRegion) {
        Point point = foundLooseFertilePoint(allPoints);
        if (point != null) {
            setRegionAllPoints(new HashSet<>(Arrays.asList(point)), allPoints, numberRegion);
            Region region = collectRegion(allPoints, numberRegion);

            regions.add(region);
            setRegions(allPoints, regions, (numberRegion + 1));
        }
    }

    private static Point foundLooseFertilePoint(List<Point> allPoints) {
        return allPoints.parallelStream()
                .filter(point -> point.isFertile() && point.getRegionNumber() == null)
                .findFirst().orElse(null);
    }

    private static void fill(List<Point> points) throws FileNotFoundException {
        Scanner sc = new Scanner(System.in);
//        Scanner sc = new Scanner(new FileReader("test2.txt"));
        String line = sc.nextLine();
        List<Integer> terms = toIntList(line);
        for (int y = 0; y < terms.get(1); y++) {
            line = sc.nextLine();
            List<Integer> data = toIntList(line);
            for (int x = 0; x < terms.get(0); x++)
                points.add(Point.of(x, y, data.get(x) == 1));
        }
    }

    private static Integer selectBestAreaAndGetCountPoint(List<Region> regions) {
        int allPointBestRegion = 0;
        BigDecimal effectivenessBestRegion = new BigDecimal(0);

        if (regions.isEmpty()) {
            return 0;
        }

        for (Region region : regions) {
            int allPoint = region.getAllPoint();
            if (allPoint > 1) {
                BigDecimal effectiveness = region.getEffectiveness();
                if (effectiveness.compareTo(effectivenessBestRegion) > 0) {
                    allPointBestRegion = allPoint;
                    effectivenessBestRegion = effectiveness;
                } else if (effectivenessBestRegion.compareTo(effectiveness) == 0 && allPoint > allPointBestRegion) {
                    allPointBestRegion = allPoint;
                }
            }
        }
        return allPointBestRegion;
    }

    private static Region collectRegion(List<Point> allPoints, int regionNumber) {

        Map<String, Point> pointByCoordinate = allPoints.parallelStream()
                .collect(Collectors.toMap(point -> point.getX() + " " + point.getY(), Function.identity()));

        int maxXCoordinate = getMaxHorizontalCoordinate(allPoints, regionNumber);
        int minXCoordinate = getMinHorizontalCoordinate(allPoints, regionNumber);
        int amountPointHorizontalBorder = maxXCoordinate - minXCoordinate + 1;
        int maxYCoordinate = getMaxVerticalCoordinate(allPoints, regionNumber);
        int minYCoordinate = getMinVerticalCoordinate(allPoints, regionNumber);
        int amountPointVerticalBorder = maxYCoordinate - minYCoordinate + 1;

        int countFertileInRegion = countFertilePointInRegion(maxXCoordinate, minXCoordinate,
                maxYCoordinate, minYCoordinate, pointByCoordinate);

        int allPointRegion = amountPointHorizontalBorder * amountPointVerticalBorder;

        return new Region(countFertileInRegion, allPointRegion, regionNumber);
    }

    private static int countFertilePointInRegion(int maxXCoordinate,
                                               int minXCoordinate,
                                               int maxYCoordinate,
                                               int minYCoordinate,
                                               Map<String, Point> pointByCoordinate) {
        int countFertile = 0;
        for (int x = minXCoordinate; x <= maxXCoordinate; x++) {
            for (int y = minYCoordinate; y <= maxYCoordinate; y++) {
                Point point = pointByCoordinate.get(x + " " + y);
                if (point.isFertile()) {
                    countFertile ++;
                }
            }
        }
        return countFertile;
    }

    private static Integer getMaxHorizontalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.parallelStream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getX).max().getAsInt();
    }

    private static Integer getMinHorizontalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.parallelStream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getX).min().getAsInt();
    }

    private static Integer getMaxVerticalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.parallelStream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getY).max().getAsInt();
    }

    private static Integer getMinVerticalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.parallelStream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getY).min().getAsInt();
    }

    private static void setRegionAllPoints(Set<Point> hasRegionPoints, List<Point> allPoints, int regionNumber) {
        boolean isExistLoosePoints = true;
        while (isExistLoosePoints) {
            Set<Point> loosePoints = hasRegionPoints.parallelStream()
                    .map(hasRegionPoint -> findLooseNeighbors(allPoints, hasRegionPoint))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            if (loosePoints.isEmpty()) {
                isExistLoosePoints = false;
                hasRegionPoints.forEach(point -> point.setRegionNumber(regionNumber));
            } else {
                loosePoints.forEach(point -> point.setRegionNumber(regionNumber));
                setRegionAllPoints(loosePoints, allPoints, regionNumber);
            }
        }
    }

    private static List<Point> findLooseNeighbors(List<Point> allPoints, Point hasRegionPoint) {
        return allPoints.parallelStream()
                .filter(searchPoint
                        -> searchPoint.getRegionNumber() == null &&
                        searchPoint.isFertile() &&
                        isNeighbor(searchPoint, hasRegionPoint))
                .collect(Collectors.toList());
    }

    private static boolean isNeighbor(Point test, Point control) {
        return isHorizontalNeighbor(test, control) ||
                isVerticalNeighbor(test, control) ||
                isDescendingDiagonalNeighbor(test, control) ||
                isRisingDiagonalNeighbor(test, control);
    }

    private static boolean isHorizontalNeighbor(Point test, Point control) {
        return control.getY() == test.getY() &&
                (control.getX() == test.getX() - 1 || control.getX() == test.getX() + 1);
    }

    private static boolean isVerticalNeighbor(Point test, Point control) {
        return control.getX() == test.getX() &&
                (control.getY() == test.getY() - 1 || control.getY() == test.getY() + 1);
    }

    private static boolean isDescendingDiagonalNeighbor(Point test, Point control) {
        return (control.getX() == test.getX() + 1 && control.getY() == test.getY() + 1) ||
                (control.getX() == test.getX() - 1 && control.getY() == test.getY() - 1);
    }

    private static boolean isRisingDiagonalNeighbor(Point test, Point control) {
        return (control.getX() == test.getX() + 1 && control.getY() == test.getY() - 1) ||
                (control.getX() == test.getX() - 1 && control.getY() == test.getY() + 1);
    }

    private static List<Integer> toIntList(String line) {
        String[] dataArr = line.split(" ");
        return Arrays.stream(dataArr)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}

class Region {
    int amountFertile;
    int allPoint;
    int regionNumber;
    BigDecimal effectiveness;

    public Region(int amountFertile, int allPoint, int regionNumber) {
        this.amountFertile = amountFertile;
        this.allPoint = allPoint;
        this.regionNumber = regionNumber;
        this.effectiveness =
                new BigDecimal(amountFertile).divide(new BigDecimal(allPoint), 6, RoundingMode.DOWN);
    }

    public int getAllPoint() {
        return allPoint;
    }

    public BigDecimal getEffectiveness() {
        return effectiveness;
    }

}

class Point {
    private Integer x;
    private Integer y;
    private boolean isFertile;
    private Integer regionNumber;


    private Point(Integer x, Integer y, boolean isFertile) {
        this.x = x;
        this.y = y;
        this.isFertile = isFertile;
    }

    public static Point of(int x, int y, boolean isFertile) {
        return new Point(x, y, isFertile);
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public boolean isFertile() {
        return isFertile;
    }

    public Integer getRegionNumber() {
        return regionNumber;
    }

    public void setRegionNumber(Integer regionNumber) {
        this.regionNumber = regionNumber;
    }
}