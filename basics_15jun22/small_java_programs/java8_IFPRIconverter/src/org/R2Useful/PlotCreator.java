package org.R2Useful;


// import static org.matplotlib4j.Plot.create;

// import java.util.List;
// import java.util.stream.Collectors;
// import org.matplotlib4j.Plot;
// import org.matplotlib4j.color.Colors;
// import org.matplotlib4j.style.Styles;

public class PlotCreator {

    public void testPlotScatter() throws IOException, PythonExecutionException {
        // List<Double> x = NumpyUtils.linspace(-3, 3, 100);
        // List<Double> y = x.stream().map(xi -> Math.sin(xi) + Math.random()).collect(Collectors.toList());

        // Plot plt = new PlotImpl(true);
        // plt.plot().add(x, y, "o").label("sin");
        // plt.title("scatter");
        // plt.legend().loc("upper right");
        // plt.show();
    }

    // public static void plotAgainstEachOther(
    //     String crop,
    //     String catOrCntrl,
    //     List<Double> controlGdf,
    //     List<Double> historicalGdf,
    //     List<Double> lat,
    //     String resultsFolder
    // ) {
    //     List<Double> controlGdfNonZero = controlGdf.stream()
    //         .filter(yield -> historicalGdf.get(controlGdf.indexOf(yield)) != 0)
    //         .collect(Collectors.toList());
    //     List<Double> historicalGdfNonZero = historicalGdf.stream()
    //         .filter(yield -> yield != 0)
    //         .collect(Collectors.toList());

    //     double controlMean = controlGdfNonZero.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    //     double historicalMean = historicalGdfNonZero.stream().mapToDouble(Double::doubleValue).average().orElse(0);

    //     Plot plot = create();
    //     plot.scatter(
    //         controlGdfNonZero,
    //         historicalGdfNonZero,
    //         c=lat,
    //         cmap=Styles.CMAP_VIRIDIS
    //     );
    //     List<Double> x = plot.linspace(plot.xlim()[0], plot.xlim()[1], 100);
    //     plot.plot(x, x, Styles.LINE_DASHED, Colors.BLACK);
    //     plot.xlabel("Crop Model Yield (kg/ha)");
    //     plot.ylabel("Historical Yield (kg/ha)");
    //     plot.colorbar(label="Latitude");
    //     plot.title(crop + " Crop Model Yields vs Historical Yields " + catOrCntrl);
    //     plot.savefig(resultsFolder + catOrCntrl + "_" + crop + "_Crop_Model_Yield_vs_Historical_Yield.png", 200, 5, 5);
    //     plot.show();

    //     plot = create();
    //     plot.scatter(historicalGdfNonZero, lat, Styles.MARKER_O, Colors.BLUE, label="Historical Yield");
    //     plot.scatter(controlGdfNonZero, lat, Styles.MARKER_O, Colors.RED, label="Crop model Yield");
    //     plot.xlabel("Latitude");
    //     plot.ylabel("Yield (kg/ha)");
    //     plot.axhline(historicalMean, c=Colors.BLUE, label="Historical Mean");
    //     plot.axhline(controlMean, c=Colors.RED, label="Crop Model Mean");
    //     plot.title(crop + "Crop Model vs Historical Yield " + catOrCntrl);
    //     plot.legend();
    //     plot.savefig(resultsFolder + catOrCntrl + crop + "Y