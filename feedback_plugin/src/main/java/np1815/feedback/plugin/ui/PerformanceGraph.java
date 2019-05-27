package np1815.feedback.plugin.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import np1815.feedback.metricsbackend.model.LineExecution;
import np1815.feedback.metricsbackend.model.Request;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.DateTimeUtils;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PerformanceGraph {

    private static final Logger LOG = Logger.getInstance(PerformanceGraph.class);
    private static final TextTitle NO_PERFORMANCE_AVAILABLE_SUBTITLE = new TextTitle("No performance history available");

    private final TimeSeriesCollection performanceDataset;
    private final JFreeChart performanceChart;
    private final TimeSeries sampleTimeSeries;
    private final TimeSeries averageSampleTimeSeries;
    private final FeedbackDrivenDevelopment feedbackComponent;
    private final int line;

    public PerformanceGraph(FeedbackDrivenDevelopment feedbackComponent, int line) {
        this.feedbackComponent = feedbackComponent;
        this.line = line;
        this.performanceDataset = new TimeSeriesCollection();
        this.sampleTimeSeries = new TimeSeries("Sample Time");
        this.averageSampleTimeSeries = new TimeSeries("Average Sample Time");
        this.performanceDataset.addSeries(sampleTimeSeries);
        this.performanceDataset.addSeries(averageSampleTimeSeries);

        this.performanceChart = ChartFactory.createTimeSeriesChart(
            "Performance Over Time",
            "Time",
            "Global Average",
            performanceDataset,
            true,
            true,
            true);


        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        XYPlot xyPlot = performanceChart.getXYPlot();
        XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer(true, true);
        xyPlot.setRenderer(rend);

        // Text
        String fontName = "Lucida Sans";
        theme.setExtraLargeFont( new Font(fontName, Font.PLAIN, 14)); // title
        theme.setLargeFont(new Font(fontName, Font.BOLD, 12)); // axis-title
        theme.setRegularFont(new Font(fontName, Font.PLAIN, 11));

        theme.setTitlePaint(JBColor.decode("#4572a7"));

        // Major gridlines
        theme.setRangeGridlinePaint(JBColor.gray);
        theme.setDomainGridlinePaint(JBColor.gray);

        // Background
        theme.setPlotBackgroundPaint(JBColor.decode("0xECECEC")); // Outer plot area (where the subtitles, legend, etc. are)
        theme.setChartBackgroundPaint(JBColor.decode("0xECECEC")); // Inner plot area (where the points are)
        theme.setLegendBackgroundPaint(JBColor.decode("0xECECEC"));

        // Axes
        theme.setAxisOffset(new RectangleInsets(0,0,0,0));
        theme.setAxisLabelPaint(JBColor.decode("#666666"));
        theme.apply(performanceChart);

        xyPlot.getRangeAxis().setAxisLineVisible(true);
        xyPlot.getRangeAxis().setTickMarksVisible(true);
        xyPlot.getRangeAxis().setTickLabelPaint(Color.decode("#666666"));

        xyPlot.getDomainAxis().setAxisLineVisible(true);
        xyPlot.getDomainAxis().setTickMarksVisible(true);
        xyPlot.getDomainAxis().setTickLabelPaint(Color.decode("#666666"));

        // Series
//        rend.setDefaultPaint(JBColor.decode("#4572a7"));
//        rend.setSeriesPaint(performanceDataset.getSeriesIndex("Sample Time"), JBColor.decode("#4572a7"));

        rend.setDefaultItemLabelsVisible(true);
        rend.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
        rend.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));


        // Other
        performanceChart.setTextAntiAlias(true);
        performanceChart.setAntiAlias(true);
        xyPlot.setOutlineVisible(false);

        // theme.setBarPainter(new StandardBarPainter());
        // getCategoryPlot (barchart)
        //        rend.setShadowVisible( true );
        //        rend.setShadowXOffset( 2 );
        //        rend.setShadowYOffset( 0 );
        //        rend.setShadowPaint( Color.decode( "#C0C0C0"));
        //        rend.setMaximumBarWidth( 0.1);

        // Legend
        LegendItemSource[] sources = performanceChart.getLegend().getSources();
        performanceChart.removeLegend();

        LegendTitle legend = new LegendTitle(new LegendItemSource() {
            @Override
            public LegendItemCollection getLegendItems() {
                LegendItemCollection legendItemCollection = new LegendItemCollection();
                for (LegendItemSource source : sources) {
                    legendItemCollection.addAll(source.getLegendItems());
                }
                legendItemCollection.add(new LegendItem("New Version", JBColor.YELLOW));
                return legendItemCollection;
            }
        });

        legend.setPosition(RectangleEdge.BOTTOM);
        performanceChart.addLegend(legend);
    }

    public ChartMouseListener getMouseListener(ChartPanel chartPanel) {
        return new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {

            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();

                LOG.info(entity.getClass().getName());

                final XYPlot plot = event.getChart().getXYPlot();
                final DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
                final ValueAxis rangeAxis = plot.getRangeAxis();
                final Rectangle2D plotRectangle = entity.getArea().getBounds2D();
                final double chartX = domainAxis.java2DToValue(event.getTrigger().getX(), plotRectangle, plot.getDomainAxisEdge());
                final double chartY = rangeAxis.java2DToValue(event.getTrigger().getY(), plotRectangle, plot.getRangeAxisEdge());
                entity.setToolTipText(DateFormat.getInstance().format(new Date((long) chartX)) + " " + chartY);
            }
        };
    }

    public JFreeChart getPerformanceChart() {
        return performanceChart;
    }

    public void update(FileFeedbackWrapper feedbackWrapper) {
        LocalDateTime from = DateTimeUtils.parseDateTimeString(feedbackComponent.getState().fromDateTime).orElse(LocalDateTime.MIN);
        LocalDateTime to = DateTimeUtils.parseDateTimeString(feedbackComponent.getState().toDateTime).orElse(LocalDateTime.MAX);

        List<LineExecution> history = feedbackWrapper.getPerformanceHistory(line).stream().filter(h ->
               h.getProfileStartTimestamp().isAfter(from)
            && h.getProfileStartTimestamp().isBefore(to)).collect(Collectors.toList());

        // TODO: Possible setting to allow domain range to stretch beyond the first and last point
        // performanceChart.getXYPlot().getDomainAxis().setRange(from.toInstant(ZoneOffset.UTC).toEpochMilli(), to.toInstant(ZoneOffset.UTC).toEpochMilli());

        performanceChart.getXYPlot().clearDomainMarkers();
        sampleTimeSeries.clear();
        averageSampleTimeSeries.clear();
        performanceChart.removeSubtitle(NO_PERFORMANCE_AVAILABLE_SUBTITLE);

        if (history.size() > 0) {
            for (LineExecution h : history) {
                double average =
                    history.stream().filter(s ->
                        s.getProfileStartTimestamp().isBefore(h.getProfileStartTimestamp()) || s.getProfileStartTimestamp().isEqual(h.getProfileStartTimestamp()))
                    .mapToDouble(LineExecution::getSampleTime).average().orElse(0.0);

                if (average != 0.0) {
                    averageSampleTimeSeries.addOrUpdate(new Millisecond(Date.from(h.getProfileStartTimestamp().toInstant(ZoneOffset.UTC))), average);
                }
                sampleTimeSeries.addOrUpdate(new Millisecond(Date.from(h.getProfileStartTimestamp().toInstant(ZoneOffset.UTC))), h.getSampleTime());
            }

            for (Request request : feedbackWrapper.getFirstRequestsForLine(line)) {
                performanceChart.getXYPlot().addDomainMarker(new ValueMarker(request.getStartTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli(), JBColor.YELLOW, new BasicStroke(2)));
            }
        } else {
            performanceChart.addSubtitle(NO_PERFORMANCE_AVAILABLE_SUBTITLE);
        }
    }
}
