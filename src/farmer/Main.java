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
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        List<Point> points = new ArrayList<>(10000);

        List<Region> regions = new ArrayList<>(10000);
        fill(points);
        Map<String, Point> pointByCoordinate = points.parallelStream()
                .collect(Collectors.toMap(point -> point.getX() + " " + point.getY(), Function.identity()));
        setRegions(points, pointByCoordinate, regions, 1);
        Integer result = selectBestAreaAndGetCountPoint(regions);
        System.out.println(result);
    }

    private static void setRegions(List<Point> allPoints, Map<String, Point> pointByCoordinate, List<Region> regions, Integer numberRegion) {
        Point point = foundLooseFertilePoint(allPoints);
        if (point != null) {
            setRegionBasePoints(new HashSet<>(Arrays.asList(point)), pointByCoordinate, numberRegion);
            Region region = collectRegion(allPoints, numberRegion, pointByCoordinate);
            regions.add(region);
            setRegions(allPoints, pointByCoordinate, regions, (numberRegion + 1));
        }
    }

    private static Point foundLooseFertilePoint(List<Point> allPoints) {
        return allPoints.parallelStream()
                .filter(point -> point.getRegionNumber() == null)
                .findFirst().orElse(null);
    }

    private static void fill(List<Point> points) throws FileNotFoundException {
//        Scanner sc = new Scanner(System.in);
        Scanner sc = new Scanner(new FileReader("test2.txt"));
        String line = sc.nextLine();
        List<Integer> terms = toIntList(line);
        for (int y = 0; y < terms.get(1); y++) {
            line = sc.nextLine();
            List<Integer> data = toIntList(line);
            for (int x = 0; x < terms.get(0); x++)
                if (data.get(x) == 1) {
                    points.add(new Point(x, y));
                }
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

    private static Region collectRegion(List<Point> allPoints, int regionNumber, Map<String, Point> pointByCoordinate) {

        int maxXCoordinate = getMaxHorizontalCoordinate(allPoints, regionNumber);
        int minXCoordinate = getMinHorizontalCoordinate(allPoints, regionNumber);
        int amountPointHorizontalBorder = maxXCoordinate - minXCoordinate + 1;
        int maxYCoordinate = getMaxVerticalCoordinate(allPoints, regionNumber);
        int minYCoordinate = getMinVerticalCoordinate(allPoints, regionNumber);
        int amountPointVerticalBorder = maxYCoordinate - minYCoordinate + 1;

        int countFertileInRegion = countFertilePointInRegion(maxXCoordinate, minXCoordinate,
                maxYCoordinate, minYCoordinate, pointByCoordinate);

        int allPointRegion = amountPointHorizontalBorder * amountPointVerticalBorder;

        return new Region(countFertileInRegion, allPointRegion);
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
                if (point != null) {
                    countFertile++;
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

    private static void setRegionBasePoints(Set<Point> hasRegionPoints, Map<String, Point> pointByCoordinate, int regionNumber) {
        boolean isExistLoosePoints = true;
        while (isExistLoosePoints) {
            Set<Point> loosePoints = hasRegionPoints.parallelStream()
                    .map(hasRegionPoint -> findLooseNeighbors(pointByCoordinate, hasRegionPoint))
                    .flatMap(Collection::parallelStream)
                    .collect(Collectors.toSet());
            if (loosePoints.isEmpty()) {
                isExistLoosePoints = false;
                hasRegionPoints.forEach(point -> point.setRegionNumber(regionNumber));
            } else {
                loosePoints.forEach(point -> point.setRegionNumber(regionNumber));
                setRegionBasePoints(loosePoints, pointByCoordinate, regionNumber);
            }
        }
    }

    private static List<Point> findLooseNeighbors(Map<String, Point> pointByCoordinate, Point hasRegionPoint) {
        List<String> strings = neighborCoordinates(hasRegionPoint);
        return strings.stream()
                .map(pointByCoordinate::get)
                .filter(Objects::nonNull)
                .filter(point -> point.getRegionNumber() == null)
                .collect(Collectors.toList());
    }

    private static List<String> neighborCoordinates(Point hasRegionPoint) {
        Integer x1 = hasRegionPoint.getX();
        Integer y1 = hasRegionPoint.getY();
        int x0 = x1 - 1;
        int x2 = x1 + 1;
        int y0 = y1 - 1;
        int y2 = y1 + 1;
        return Arrays.asList(
                x0 + " " + y0,
                x1 + " " + y0,
                x2 + " " + y0,
                x0 + " " + y1,
                x2 + " " + y1,
                x0 + " " + y2,
                x1 + " " + y2,
                x2 + " " + y2);
    }

    private static List<Integer> toIntList(String line) {
        String[] dataArr = line.split(" ");
        return Arrays.stream(dataArr)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}

class Region {
    private final int allPoint;
    private final BigDecimal effectiveness;

    public Region(int amountFertile, int allPoint) {
        this.allPoint = allPoint;
        this.effectiveness =
                new BigDecimal(amountFertile).divide(new BigDecimal(allPoint), 4, RoundingMode.DOWN);
    }

    public int getAllPoint() {
        return allPoint;
    }

    public BigDecimal getEffectiveness() {
        return effectiveness;
    }

}

class Point {
    private final Integer x;
    private final Integer y;
    private Integer regionNumber;

    public Point(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getRegionNumber() {
        return regionNumber;
    }

    public void setRegionNumber(Integer regionNumber) {
        this.regionNumber = regionNumber;
    }
}