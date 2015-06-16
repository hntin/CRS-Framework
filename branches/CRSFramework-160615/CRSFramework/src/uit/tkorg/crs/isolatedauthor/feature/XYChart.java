package uit.tkorg.crs.isolatedauthor.feature;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @see http://stackoverflow.com/questions/7205742
 * @see http://stackoverflow.com/questions/7208657
 * @see http://stackoverflow.com/questions/7071057
 */
public class XYChart extends JFrame {

    private static final int N = 8;//  Number of point
    private static final String title = "XY Chart Demo";
    private static final Random rand = new Random();

    public XYChart(String s) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel();
        this.add(chartPanel, BorderLayout.CENTER);
        JPanel control = new JPanel();
        this.add(control, BorderLayout.SOUTH);
    }
    
    public XYChart(String s, XYSeriesCollection dataset) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel(dataset);
        this.add(chartPanel, BorderLayout.CENTER);
        JPanel control = new JPanel();
        this.add(control, BorderLayout.SOUTH);
    }

    private ChartPanel createDemoPanel( XYDataset dataset) {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            title, " ABCD XYZ", " Huynh Ngoc Tin", dataset,
            PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
        return new ChartPanel(jfreechart);
    }
     private ChartPanel createDemoPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            title, " ABCD XYZ", " Huynh Ngoc Tin", LoadDataset(),
            PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setVerticalTickLabels(true);
        return new ChartPanel(jfreechart);
    }
    private XYDataset LoadDataset ()
    {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(createDataLabelTrue());
        xySeriesCollection.addSeries(createDataLabelFail());
        return xySeriesCollection;
        
    }
    
    private XYSeries createDataLabelTrue()
    {
         XYSeries seriesTrue = new XYSeries("True");
            for (int i = 0; i < N * N; i++) {
                double x = rand.nextGaussian();
                double y = rand.nextGaussian();
                seriesTrue.add(x, y);
            }
          return seriesTrue;
    }
    
     private XYSeries createDataLabelFail()
    {
         XYSeries seriesFail = new XYSeries("Fail");
            for (int i = 0; i < N * N; i++) {
                double x = rand.nextGaussian();
                double y = rand.nextGaussian();
                seriesFail.add(x, y);
            }
         return seriesFail;
    }

    public static void main(String args[]) {

                XYChart demo = new XYChart(title);
                demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                demo.pack();
                demo.setLocationRelativeTo(null);
                demo.setVisible(true);
    }
}