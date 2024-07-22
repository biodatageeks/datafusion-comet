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

package org.apache.comet.exec

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{CometTestBase, Row, SequilaSession}
import org.biodatageeks.sequila.rangejoins.IntervalTree.IntervalTreeJoinStrategyOptim

class SeQuiLaSuite extends CometTestBase {
  val schema1 = StructType(
    Seq(StructField("start1", IntegerType, nullable = false), StructField("end1", IntegerType)))
  val schema2 = StructType(
    Seq(StructField("start2", IntegerType, nullable = false), StructField("end2", IntegerType)))
  val schema3 = StructType(
    Seq(StructField("chr1", StringType, nullable = false),
      StructField("start1", IntegerType, nullable = false),
      StructField("end1", IntegerType, nullable = false)))

  test("SeQuiLaAnalyzer") {
    val ds3 = readResourceParquetFile("test-data-sequila/ds3.parquet")
    val ds4 = readResourceParquetFile("test-data-sequila/ds4.parquet")

    ds3.createOrReplaceTempView("s3")
    ds4.createOrReplaceTempView("s4")

    spark.experimental.extraStrategies = new IntervalTreeJoinStrategyOptim(
      spark) :: Nil
    spark.sparkContext.setLogLevel("INFO")
    val sqlQuery =
      "select s3.chr1 as s3_chr,s3.start1 as s3_start1, s3.*,s4.* from s4 JOIN s3 ON ( s3.chr1=s4.chr1 and s3.end1>=s4.start1 and s3.start1<=s4.end1)"
    spark.sql(sqlQuery).explain(true)
    assert( spark.sql(sqlQuery).count() == 16)

  }
}
