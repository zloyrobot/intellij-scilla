package com.zloyrobot.scilla.lang

import com.intellij.testFramework.ParsingTestCase

abstract class ScillaParsingTestBase(baseDir: String) : ParsingTestCase(baseDir, "scilla", true, ScillaParserDefinition()) {
  override fun getTestDataPath() = "src/test/data"
}

class ScillaParsingTest : ScillaParsingTestBase("parser") {

  fun testComment0() = doTest(true)
  fun testComment1() = doTest(true)
  fun testComment2() = doTest(true)
  fun testComment3() = doTest(true)
  fun testField0() = doTest(true)
  fun testField1() = doTest(true)
  fun testField2() = doTest(true)
  fun testEmptyContract0() = doTest(true)
  fun testEmptyContract1() = doTest(true)
  fun testLetExpression0() = doTest(true)
  fun testApplicationExpression0() = doTest(true)
  fun testApplicationExpression1() = doTest(true)
  fun testTapplicationExpression0() = doTest(true)
  fun testConstructorExpression0() = doTest(true)
  fun testConstructorExpression1() = doTest(true)
  fun testConstructorExpression2() = doTest(true)
  fun testMatchExpression0() = doTest(true)
  fun testMatchExpression1() = doTest(true)
  fun testMatchExpression2() = doTest(true)
  fun testMatchExpression3() = doTest(true)
  fun testFunExpression0() = doTest(true)
  fun testFunExpression1() = doTest(true)
  fun testTfunExpression0() = doTest(true)
  fun testTfunExpression1() = doTest(true)
  fun testTfunExpression2() = doTest(true)
  fun testLiteralExpression0() = doTest(true)
  fun testLiteralExpression1() = doTest(true)
  fun testLiteralExpression2() = doTest(true)
  fun testBuiltinExpression0() = doTest(true)
  fun testBuiltinExpression1() = doTest(true)
  fun testBuiltinExpression2() = doTest(true)
  fun testType0() = doTest(true)
  fun testType1() = doTest(true)
  fun testRecovery0() = doTest(true)
  fun testRecovery1() = doTest(true)
  fun testRecovery2() = doTest(true)
  fun testRecovery3() = doTest(true)
  fun testRecovery4() = doTest(true)
  fun testRecovery5() = doTest(true)
}
