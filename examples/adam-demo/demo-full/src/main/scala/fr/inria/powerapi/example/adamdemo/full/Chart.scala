/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.example.adamdemo.full

import java.awt.Dimension
import java.awt.Toolkit
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.time.FixedMillisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeriesDataItem
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities
import java.awt.BasicStroke

/**
 * Display received CpuFormulaValues to the wrapped JFreeChart chart.
 *
 * @author abourdon
 */
class Chart(title: String) {
  val dataset = new TimeSeriesCollection
  val chart = ChartFactory.createTimeSeriesChart(title,
    Chart.xValues, Chart.yValues, dataset, true, true, false)
  val timeSeries = collection.mutable.HashMap[String, TimeSeries]()
  private var offset = 0.0

  def add(values: Map[String, Double], timestamp: Long) {
    values.foreach({ value =>
      if (!timeSeries.contains(value._1)) {
        val serie = new TimeSeries(value._1)
        dataset.addSeries(serie)
        timeSeries += (value._1 -> serie)
        chart.getXYPlot().getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(3))
      }
      timeSeries(value._1).addOrUpdate(new FixedMillisecond(timestamp), value._2 + getOffset)
    })
  }

  def clear() {
    timeSeries.clear()
    dataset.removeAllSeries()
  }

  def setOffset(offset: Double) = synchronized {
    this.offset = offset
    println("Setting offset to " + offset)
  }

  def getOffset = synchronized {
    offset
  }
}

/**
 * Chart companion object containing the JFreeChart's ApplicationFrame and some configurations.
 *
 * @author abourdon
 */
object Chart {
  val xValues = "Time (s)"
  val yValues = "Power (W)"
  val title = "PowerAPI"
  val chart = {
    val ch = new Chart(title)
    val plot = ch.chart.getXYPlot()
    plot.setBackgroundPaint(java.awt.Color.WHITE)
    plot.setDomainGridlinesVisible(true)
    plot.setDomainGridlinePaint(java.awt.Color.GRAY)
    plot.setRangeGridlinesVisible(true)
    plot.setRangeGridlinePaint(java.awt.Color.GRAY)
    ch
  }

  val chartPanel = {
    val panel = new ChartPanel(chart.chart)
    panel.setMouseWheelEnabled(true)
    panel.setDomainZoomable(true)
    panel.setFillZoomRectangle(true)
    panel.setRangeZoomable(true)
    panel
  }

  val applicationFrame = {
    val app = new ApplicationFrame(title)
    app
  }

  def setTitle(title: String) {
    chartPanel.getChart().setTitle(title)
    applicationFrame.setTitle(title)
  }

  def run() {
    applicationFrame.setContentPane(chartPanel)
    applicationFrame.setSize(new Dimension(800, 600))
    applicationFrame.setVisible(true)
  }

  def add(values: Map[String, Double], timestamp: Long) {
    chart.add(values, timestamp)
  }

  def clear() {
    chart.clear()
  }

  def setOffset(offset: Double) {
    chart.setOffset(offset)
  }
}