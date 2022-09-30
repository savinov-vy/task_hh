package farmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Point> points = new ArrayList<>();

        points.add(Point.of(0, 0, false));
        points.add(Point.of(0, 1, true));
        points.add(Point.of(0, 2, true));
        points.add(Point.of(0, 3, false));

        points.add(Point.of(1, 0, true));
        points.add(Point.of(1, 1, true));
        points.add(Point.of(1, 2, true));
        points.add(Point.of(1, 3, false));

        points.add(Point.of(2, 0, true));
        points.add(Point.of(2, 1, true));
        points.add(Point.of(2, 2, false));
        points.add(Point.of(2, 3, false));

        points.add(Point.of(3, 0, false));
        points.add(Point.of(3, 1, false));
        points.add(Point.of(3, 2, false));
        points.add(Point.of(3, 3, true));

        points.add(Point.of(4, 0, false));
        points.add(Point.of(4, 1, true));
        points.add(Point.of(4, 2, true));
        points.add(Point.of(4, 3, false));


        Point firstPointFirstRegion = setFirstPoint(points, 1);
        setRegionAllPoints(Arrays.asList(firstPointFirstRegion), points, 1);
        Point point = setFirstPoint(points, 2);
        setRegionAllPoints(Arrays.asList(point), points, 2);
        Integer sumFertileInFirstRegion = getSumFertilePointInRegion(points, 1);
        Integer sumFertileInSecondRegion = getSumFertilePointInRegion(points, 2);
        Integer sumAllPointsInFirstRegion = getSumAllPointInRegion(points, 1);
        Integer sumAllPointsInSecondRegion = getSumAllPointInRegion(points, 2);

        Integer result = selectBestAreaAndGetCountPoint(sumFertileInFirstRegion, sumAllPointsInFirstRegion,
                sumFertileInSecondRegion, sumAllPointsInSecondRegion);
        System.out.println(result);
    }

    private static Integer selectBestAreaAndGetCountPoint(Integer sumFertileInFirstRegion,
                                                          Integer sumAllPointsInFirstRegion,
                                                          Integer sumFertileInSecondRegion,
                                                          Integer sumAllPointsInSecondRegion) {
        double effectivenessFirstRegion = (double) sumFertileInFirstRegion / sumAllPointsInFirstRegion;
        double effectivenessSecondRegion = (double) sumFertileInSecondRegion / sumAllPointsInSecondRegion;
        if (effectivenessFirstRegion > effectivenessSecondRegion) {
            return sumAllPointsInFirstRegion;
        } else if (effectivenessFirstRegion == effectivenessSecondRegion) {
            return sumAllPointsInFirstRegion >= sumAllPointsInSecondRegion ? sumAllPointsInFirstRegion :
                    sumAllPointsInSecondRegion;
        } else {
            return sumAllPointsInSecondRegion;
        }
    }
    
    private static Integer getSumFertilePointInRegion(List<Point> allPoints, Integer regionNumber) {
        long count = allPoints.stream()
                .filter(point -> point.isFertile() && point.getRegionNumber() == regionNumber)
                .count();
        return (int) count;
    }

    private static Integer getSumAllPointInRegion(List<Point> allPoints, Integer regionNumber) {
        Integer maxHorizontalCoordinate = getMaxHorizontalCoordinate(allPoints, regionNumber);
        Integer minHorizontalCoordinate = getMinHorizontalCoordinate(allPoints, regionNumber);
        Integer amountPointHorizontalBorder = maxHorizontalCoordinate - minHorizontalCoordinate + 1;
        Integer maxVerticalCoordinate = getMaxVerticalCoordinate(allPoints, regionNumber);
        Integer minVerticalCoordinate = getMinVerticalCoordinate(allPoints, regionNumber);
        Integer amountPointVerticalBorder = maxVerticalCoordinate - minVerticalCoordinate + 1;
        return amountPointHorizontalBorder * amountPointVerticalBorder;
    }

    private static Integer getMaxHorizontalCoordinate(List<Point> allPoints, Integer regionNumber) {
    return allPoints.stream()
            .filter(point -> point.getRegionNumber() == regionNumber)
            .mapToInt(Point::getX).max().getAsInt();
    }

    private static Integer getMinHorizontalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.stream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getX).min().getAsInt();
    }

    private static Integer getMaxVerticalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.stream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getY).max().getAsInt();
    }

    private static Integer getMinVerticalCoordinate(List<Point> allPoints, Integer regionNumber) {
        return allPoints.stream()
                .filter(point -> point.getRegionNumber() == regionNumber)
                .mapToInt(Point::getY).min().getAsInt();
    }

    private static Point setFirstPoint(List<Point> list, Integer regionNumber) {
        Point first = list.stream()
                .filter(point -> point.isFertile() && point.getRegionNumber() == null)
                .findFirst().orElse(null);
        if (first == null) {
            return null;
        } else {
            first.setRegionNumber(regionNumber);
        }
        return first;
    }

    private static void setRegionAllPoints(List<Point> hasRegionPoints, List<Point> allPoints, Integer regionNumber) {
        boolean isExistLoosePoints = true;
        while (isExistLoosePoints) {
            List<Point> loosePoints = hasRegionPoints.stream()
                    .map(hasRegionPoint -> findLooseNeighbors(allPoints, hasRegionPoint))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            if (loosePoints.isEmpty()) {
                isExistLoosePoints = false;
            } else {
                loosePoints.forEach(point -> point.setRegionNumber(regionNumber));
                setRegionAllPoints(loosePoints, allPoints, regionNumber);
            }
        }
    }

    private static List<Point> findLooseNeighbors(List<Point> allPoints, Point hasRegionPoint) {
        return allPoints.stream()
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