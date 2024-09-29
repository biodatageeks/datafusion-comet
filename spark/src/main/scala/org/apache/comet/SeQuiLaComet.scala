/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.comet

import org.biodatageeks.sequila.rangejoins.IntervalTree.IntervalTreeJoinStrategyOptim

import org.apache.spark.sql.SparkSession

object SeQuiLaComet {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SeQuiLaComet")
      .getOrCreate()
    spark.experimental.extraStrategies = new IntervalTreeJoinStrategyOptim(spark) :: Nil
    if (args.length < 2) {
      // scalastyle:off println
      println("Usage: SeQuiLaComet <number_of_slices>")
      // scalastyle:on println
      System.exit(1)
    } else {
      val tablePathLeft = args(0)
      val tablePathRight = args(1)
      val ds1 = spark.read.parquet(tablePathLeft)
      val ds2 = spark.read.parquet(tablePathRight)
      ds1.createOrReplaceTempView("tableLeft")
      ds2.createOrReplaceTempView("tableRight")
      val sqlQuery =
        "select count(*) from tableLeft a, tableRight b  " +
          "where (a.column0=b.column0 and a.column2>=b.column1 and a.column1<=b.column2);"
      spark.sql(sqlQuery).explain(true)
      spark.time {
        spark
          .sql(sqlQuery)
          .show()
      }

      // scalastyle:off println
      println("Pi is roughly ")
      // scalastyle:on
      spark.stop()
    }
  }
}
