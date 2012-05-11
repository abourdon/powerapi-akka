/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
package powerapi.listener.cpulistener.jfreechart
import org.jfree.chart.ChartFactory
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.ui.ApplicationFrame
import akka.util.duration.intToDurationInt
import javax.swing.JFrame
import javax.swing.SwingUtilities
import org.jfree.data.time.TimeSeries
import org.jfree.chart.ChartPanel
import java.awt.Dimension
import org.jfree.ui.RefineryUtilities
import powerapi.formula.cpuformula.CpuFormulaValues
import powerapi.core.Process
import org.jfree.data.time.TimeSeriesDataItem
import org.jfree.data.time.Millisecond

class Chart(title: String) {
  val dataset = new TimeSeriesCollection
  val chart = ChartFactory.createTimeSeriesChart(title,
    Chart.xValues, Chart.yValues, dataset, true, true, false);
  val timeSeries = collection.mutable.HashMap[Process, TimeSeries]()

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    val pid = cpuFormulaValues.tick.subscription.process
    if (!timeSeries.contains(pid)) {
      val serie = new TimeSeries(pid.toString)
      dataset.addSeries(serie)
      timeSeries += (pid -> serie)
    }
    val serie = timeSeries(pid)
    serie.add(new TimeSeriesDataItem(new Millisecond(), cpuFormulaValues.energy.power))
  }
}

object Chart {
  val xValues = "Time (s)"
  val yValues = "Power (W)"
  val title = "PowerAPI"
  lazy val chart = new Chart(title)

  val chartPanel = {
    val panel = new ChartPanel(chart.chart)
    panel.setPreferredSize(new Dimension(1000, 270));
    panel.setMouseWheelEnabled(true);
    panel.setDomainZoomable(true);
    panel.setFillZoomRectangle(true);
    panel.setRangeZoomable(true);
    panel
  }

  val applicationFrame = {
    val app = new ApplicationFrame(title)
    app
  }

  def run() {
    applicationFrame.setContentPane(chartPanel)
    applicationFrame.setVisible(true)
    applicationFrame.pack()
    RefineryUtilities.centerFrameOnScreen(applicationFrame)
  }

  def process(implicit cpuFormulaValues: CpuFormulaValues) {
    chart.process
  }
}