package farmer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        List<Point> points = new ArrayList<>(10000);
        List<Region> regions = new ArrayList<>(10000);
        fill(points);
        setRegions(points, regions, 1);
        Integer result = selectBestAreaAndGetCountPoint(regions);
        System.out.println(result);
    }

    private static void setRegions(List<Point> allPoints, List<Region> regions, int numberRegion) {
        Point point = foundLooseFertilePoint(allPoints);
        if (point != null) {
            setRegionAllPoints(Arrays.asList(point), allPoints, numberRegion);
            int sumFertilePointInRegion = getSumFertilePointInRegion(allPoints, numberRegion);
            int sumAllPointInRegion = getSumAllPointInRegion(allPoints, numberRegion);
            regions.add(new Region(sumFertilePointInRegion,
                    sumAllPointInRegion,
                    numberRegion));
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

    private static int getSumFertilePointInRegion(List<Point> allPoints, Integer regionNumber) {
        long count = allPoints.parallelStream()
                .filter(point -> point.isFertile() && point.getRegionNumber() == regionNumber)
                .count();
        return (int) count;
    }

    private static int getSumAllPointInRegion(List<Point> allPoints, int regionNumber) {
        int maxHorizontalCoordinate = getMaxHorizontalCoordinate(allPoints, regionNumber);
        int minHorizontalCoordinate = getMinHorizontalCoordinate(allPoints, regionNumber);
        int amountPointHorizontalBorder = maxHorizontalCoordinate - minHorizontalCoordinate + 1;
        int maxVerticalCoordinate = getMaxVerticalCoordinate(allPoints, regionNumber);
        int minVerticalCoordinate = getMinVerticalCoordinate(allPoints, regionNumber);
        int amountPointVerticalBorder = maxVerticalCoordinate - minVerticalCoordinate + 1;
        return amountPointHorizontalBorder * amountPointVerticalBorder;
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

    private static void setRegionAllPoints(List<Point> hasRegionPoints, List<Point> allPoints, int regionNumber) {
        boolean isExistLoosePoints = true;
        while (isExistLoosePoints) {
            List<Point> loosePoints = hasRegionPoints.parallelStream()
                    .distinct()
                    .map(hasRegionPoint -> findLooseNeighbors(allPoints, hasRegionPoint))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
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
                new BigDecimal(amountFertile).divide(new BigDecimal(allPoint),6, RoundingMode.DOWN);
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

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public boolean isFertile() {
        return isFertile;
    }

    public void setFertile(boolean fertile) {
        isFertile = fertile;
    }

    public Integer getRegionNumber() {
        return regionNumber;
    }

    public void setRegionNumber(Integer regionNumber) {
        this.regionNumber = regionNumber;
    }
}