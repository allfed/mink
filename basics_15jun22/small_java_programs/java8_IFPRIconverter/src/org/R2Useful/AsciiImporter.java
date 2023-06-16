package org.R2Useful;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsciiImporter {
    private double north;
    private double south;
    private double east;
    private double west;
    private int rows;
    private int cols;
    private List<Double> latitudes = new ArrayList<>();
    private List<Double> longitudes = new ArrayList<>();
    private List<Double> yields = new ArrayList<>();

    public AsciiImporter(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                switch (lineNumber) {
                    case 0:
                        north = Double.parseDouble(line.split(":")[1].trim().split("N")[0]);
                        break;
                    case 1:
                        south = Double.parseDouble(line.split(":")[1].trim().split("N")[0]);
                        break;
                    case 2:
                        east = Double.parseDouble(line.split(":")[1].trim().split("W")[0]);
                        break;
                    case 3:
                        west = Double.parseDouble(line.split(":")[1].trim().split("W")[0]);
                        break;
                    case 4:
                        rows = Integer.parseInt(line.split(":")[1].trim());
                        break;
                    case 5:
                        cols = Integer.parseInt(line.split(":")[1].trim());
                        break;
                    default:
                        String[] data = line.split("\\s+");
                        for (String d : data) {
                            if (!d.equals("*")) {
                                yields.add(Double.parseDouble(d));
                            } else {
                                yields.add(null);
                            }
                        }
                        break;
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        latitudes = generateArray(south, north, rows);
        longitudes = generateArray(west, east, cols);
    }

    private List<Double> generateArray(double start, double end, int size) {
        List<Double> result = new ArrayList<>();
        double step = (end - start) / (size - 1);
        for (int i = 0; i < size; i++) {
            result.add(start + i * step);
        }
        return result;
    }

    public double getNorth() {
        return north;
    }

    public double getSouth() {
        return south;
    }

    public double getEast() {
        return east;
    }

    public double getWest() {
        return west;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int[] getYields() {
        return yields;
    }
    latitudes
    public int getYields() {
        return yields;
    }

    public List<Double> getLatitudes(){
        return latitudes;
    }

    public List<Double> getLongitudes(){
        return longitudes;
    }

    public List<Double> getYields(){
        return yields;
    }
}