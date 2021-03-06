/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.util;

import java.util.Arrays;
import java.util.List;

import org.auraframework.util.AuraTextUtil.JSONEscapedFunctionStringBuilder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AuraTextUtilTest {
    /**
     * A simple input/expected pair for testing.
     */
    private static class StringPair {
        public final String input;
        public final String expected;

        /**
         * The full constructor.
         * 
         * @param input the input string to the method.
         * @param expected the expected output string.
         */
        public StringPair(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        /**
         * A constructor for unchanged values.
         * 
         * @param input the input string to the method, this will be used as the expected value.
         */
        public StringPair(String input) {
            this.input = input;
            this.expected = input;
        }
    }

    private final static StringPair[] INIT_LOWER_CASE_PAIRS = new StringPair[] { new StringPair(null, null),
            new StringPair("", ""), new StringPair("A", "a"), new StringPair("a", "a"),
            new StringPair("Apple", "apple"), new StringPair("apple", "apple"), new StringPair("1", "1"),
            new StringPair("=", "="), new StringPair(" A", " A"), new StringPair("Ñ", "ñ"), };

    @Test
    public void testInitLowerCase() {
        for (StringPair p : INIT_LOWER_CASE_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.initLowerCase(p.input));
        }
    }

    @Test
    public void testArrayToStringNulls() {
        assertEquals(null, AuraTextUtil.arrayToString(null, ",", 1, true, true));
        try {
            AuraTextUtil.arrayToString(new String[] {}, null, 1, true, true);
            fail("expected illegal argument exception on null delimiter");
        } catch (IllegalArgumentException expected) {
            // ignore
        }
    }

    @Test
    public void testArrayToStringEmpty() {
        assertEquals("", AuraTextUtil.arrayToString(new String[] {}, ",", 1, false, false));
        assertEquals("[]", AuraTextUtil.arrayToString(new String[] {}, ",", 1, true, false));
    }

    @Test
    public void testArrayToStringSingle() {
        assertEquals("1", AuraTextUtil.arrayToString(new String[] { "1" }, ",", 1, false, false));
        assertEquals("[1]", AuraTextUtil.arrayToString(new String[] { "1" }, ",", 1, true, false));
        assertEquals("1", AuraTextUtil.arrayToString(new String[] { "1" }, ",", 1, false, true));
        assertEquals("[1]", AuraTextUtil.arrayToString(new String[] { "1" }, ",", 1, true, true));
    }

    @Test
    public void testArrayToStringTwoEntry() {
        assertEquals("1,2", AuraTextUtil.arrayToString(new String[] { "1", "2" }, ",", 2, false, false));
        assertEquals("[1,2]", AuraTextUtil.arrayToString(new String[] { "1", "2" }, ",", 2, true, false));
        assertEquals("1,2", AuraTextUtil.arrayToString(new String[] { "1", "2" }, ",", 2, false, true));
        assertEquals("[1,2]", AuraTextUtil.arrayToString(new String[] { "1", "2" }, ",", 2, true, true));

        assertEquals("1xxx2", AuraTextUtil.arrayToString(new String[] { "1", "2" }, "xxx", 2, false, false));
        assertEquals("[1xxx2]", AuraTextUtil.arrayToString(new String[] { "1", "2" }, "xxx", 2, true, false));
        assertEquals("1xxx2", AuraTextUtil.arrayToString(new String[] { "1", "2" }, "xxx", 2, false, true));
        assertEquals("[1xxx2]", AuraTextUtil.arrayToString(new String[] { "1", "2" }, "xxx", 2, true, true));
    }

    @Test
    public void testArrayToStringTwoEntryLimit() {
        assertEquals("1,2", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, false, false));
        assertEquals("[1,2]", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, true, false));
        assertEquals("1,2...", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, false, true));
        assertEquals("[1,2...]", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, true, true));

        // the other call case...
        assertEquals("1,2...", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, false));
        assertEquals("[1,2...]", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, ",", 2, true));

        assertEquals("1xxx2", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, "xxx", 2, false, false));
        assertEquals("[1xxx2]", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, "xxx", 2, true, false));
        assertEquals("1xxx2...", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, "xxx", 2, false, true));
        assertEquals("[1xxx2...]", AuraTextUtil.arrayToString(new String[] { "1", "2", "3" }, "xxx", 2, true, true));
    }

    @Test
    public void testArrayToStringTwoEntryHasNull() {
        assertEquals("1,null", AuraTextUtil.arrayToString(new String[] { "1", null }, ",", 2, false, false));
    }

    @Test
    public void testArrayToStringManyEntry() {
        assertEquals("1,2,3,4", AuraTextUtil.arrayToString(new String[] { "1", "2", "3", "4" }, ",", -1, false, false));
    }

    @Test
    public void testIsNullOrEmpty() {
        assertEquals(true, AuraTextUtil.isNullOrEmpty(null));
        assertEquals(true, AuraTextUtil.isNullOrEmpty(""));
        assertEquals(false, AuraTextUtil.isNullOrEmpty("a"));
        assertEquals(false, AuraTextUtil.isNullOrEmpty(" "));
        assertEquals(false, AuraTextUtil.isNullOrEmpty("\t"));
        assertEquals(false, AuraTextUtil.isNullOrEmpty("\t\n "));
        assertEquals(false, AuraTextUtil.isNullOrEmpty("\r\n "));
        assertEquals(false,
                AuraTextUtil.isNullOrEmpty("\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009\u0010\u0011 "));
        assertEquals(false, AuraTextUtil.isNullOrEmpty("\t\n a"));
    }

    @Test
    public void testIsEmptyOrWhitespace() {
        assertEquals(false, AuraTextUtil.isEmptyOrWhitespace(null));
        assertEquals(true, AuraTextUtil.isEmptyOrWhitespace(""));
        assertEquals(false, AuraTextUtil.isEmptyOrWhitespace("a"));
        assertEquals(true, AuraTextUtil.isEmptyOrWhitespace(" "));
        assertEquals(true, AuraTextUtil.isEmptyOrWhitespace("\t"));
        assertEquals(true, AuraTextUtil.isEmptyOrWhitespace("\t\n "));
        assertEquals(true, AuraTextUtil.isEmptyOrWhitespace("\r\n "));
        assertEquals(true,
                AuraTextUtil.isEmptyOrWhitespace("\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009\u0010\u0011 "));
        assertEquals(false, AuraTextUtil.isEmptyOrWhitespace("\t\n a"));
        assertEquals(false, AuraTextUtil.isEmptyOrWhitespace("\ufffe"));
    }

    /**
     * JS replacement strings.
     */
    private static StringPair[] JS_STRING_PAIRS = new StringPair[] { new StringPair("'", "\\'"),
            new StringPair("\r", "\\r"), new StringPair("\n", "\\n"), new StringPair("\u2028", "\\n"),
            new StringPair("'abc'", "\\'abc\\'"), new StringPair("<!--", "\\u003C\\u0021--"),
            new StringPair("-->", "--\\u003E"), new StringPair("\"", "\\\""), new StringPair("\\", "\\\\"),
            new StringPair("\u0000", ""),
            new StringPair("0123456789/!@#$%^&*()-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), };

    @Test
    public void testEscapeForJavascriptString() {
        for (StringPair p : JS_STRING_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.escapeForJavascriptString(p.input));
        }
    }

    /**
     * JSON replacement strings.
     */
    private static StringPair[] JSON_STRING_PAIRS = new StringPair[] {
            new StringPair("\u2028", "\n"),
            new StringPair("\u0000", ""),
            new StringPair("0123456789/!@#$%^&*()-_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    };

    @Test
    public void testEscapeForJSONString() {
        for (StringPair p : JSON_STRING_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.escapeForJSONString(p.input));
        }
    }

    @Test
    public void testJSONEscapedFunctionStringBuilder() throws Exception {
        StringBuilder sb = new StringBuilder();
        new JSONEscapedFunctionStringBuilder(sb).append("/* */");
        assertEquals("/* \\u002A/", sb.toString());
    }

    private static class SplitMatch {
        public final String input;
        public final String delimiter;
        public final int expectedSize;
        public final List<String> result;

        public SplitMatch(String input, String delimiter, int expectedSize, String[] result) {
            this.input = input;
            this.delimiter = delimiter;
            this.expectedSize = expectedSize;
            if (result != null) {
                this.result = Arrays.asList(result);
            } else {
                this.result = null;
            }
        }

        @Override
        public String toString() {
            return "split('" + this.delimiter + "','" + this.input + "','" + this.expectedSize + ")";
        }

        public void checkResult(List<String> actual) {
            String label = toString();
            int i;

            if (this.result == null || actual == null) {
                assertEquals(this.result, actual);
                return;
            }
            assertEquals(label + " size mismatch", this.result.size(), actual.size());
            for (i = 0; i < this.result.size(); i += 1) {
                assertEquals(label + " item mismatch at " + i, this.result.get(i), actual.get(i));
            }
        }
    };

    private static final SplitMatch[] splitTests = new SplitMatch[] {
            new SplitMatch(null, ",", 0, null),
            new SplitMatch("", ",", 1, new String[] { "" }),
            new SplitMatch("a", ",", 1, new String[] { "a" }),
            new SplitMatch("a,b", ",", 1, new String[] { "a", "b" }),
            new SplitMatch("axxxb", "xxx", 1, new String[] { "a", "b" }),
            new SplitMatch("axxxb", "xxx", 0, new String[] { "a", "b" }),
            new SplitMatch("a, b", ",", 2, new String[] { "a", " b" }),
            new SplitMatch("a,b ", ",", 2, new String[] { "a", "b " }),
            new SplitMatch("a,b,", ",", 2, new String[] { "a", "b", "" }), };

    @Test
    @Deprecated
    public void testSplitSimple2() {
        for (SplitMatch sm : splitTests) {
            sm.checkResult(AuraTextUtil.splitSimple(sm.delimiter, sm.input));
        }
    }

    @Test
    @Deprecated
    public void testSplitSimple3() {
        for (SplitMatch sm : splitTests) {
            sm.checkResult(AuraTextUtil.splitSimple(sm.delimiter, sm.input, sm.expectedSize));
        }
    }

    private static final SplitMatch[] splitTrimTests = new SplitMatch[] {
            new SplitMatch("", ",", 1, new String[] { "" }),
            new SplitMatch("a", ",", 1, new String[] { "a" }),
            new SplitMatch("a,b", ",", 1, new String[] { "a", "b" }),
            new SplitMatch("axxxb", "xxx", 1, new String[] { "a", "b" }),
            new SplitMatch("axxxb", "xxx", 0, new String[] { "a", "b" }),
            new SplitMatch("a, b", ",", 2, new String[] { "a", "b" }),
            new SplitMatch("a,b ", ",", 2, new String[] { "a", "b" }),
            new SplitMatch("a,b,", ",", 2, new String[] { "a", "b", "" }),
            new SplitMatch("a,b,  ", ",", 2, new String[] { "a", "b", "" }), };

    @Test
    @Deprecated
    public void testSplitSimpleAndTrim() {
        for (SplitMatch sm : splitTrimTests) {
            sm.checkResult(AuraTextUtil.splitSimpleAndTrim(sm.input, sm.delimiter, sm.expectedSize));
        }
    }

    private static final SplitMatch[] splitLimitTests = new SplitMatch[] {
            new SplitMatch("a", ",", 1, new String[] { "a" }), new SplitMatch("a,b", ",", 1, new String[] { "a,b" }),
            new SplitMatch("axxxb", "xxx", 1, new String[] { "axxxb" }),
            new SplitMatch("axxxb", "xxx", 2, new String[] { "a", "b" }),
            new SplitMatch("axxxbxxxc", "xxx", 2, new String[] { "a", "bxxxc" }),
            new SplitMatch("a,b,", ",", 2, new String[] { "a", "b," }),
            new SplitMatch("a,b,", ",", 10, new String[] { "a", "b", "" }), };

    @Test
    @Deprecated
    public void testSplitSimpleLimit() {
        for (SplitMatch sm : splitLimitTests) {
            sm.checkResult(AuraTextUtil.splitSimpleLimit(sm.input, sm.delimiter, sm.expectedSize));
        }
    }

    private static final SplitMatch[] splitLimitTrimTests = new SplitMatch[] {
            new SplitMatch("", ",", 1, new String[] { "" }),
            new SplitMatch("a   ", ",", 1, new String[] { "a" }),
            new SplitMatch("a , b", ",", 1, new String[] { "a , b" }),
            new SplitMatch("axxx b", "xxx", 1, new String[] { "axxx b" }),
            new SplitMatch("axxx b", "xxx", 2, new String[] { "a", "b" }),
            new SplitMatch("axxxbxxx c", "xxx", 2, new String[] { "a", "bxxx c" }),
            new SplitMatch("a,b,   ", ",", 2, new String[] { "a", "b," }),
            new SplitMatch("a,b,   ", ",", 10, new String[] { "a", "b", "" }), };

    @Test
    @Deprecated
    public void testSplitSimpleLimitTrim() {
        for (SplitMatch sm : splitLimitTrimTests) {
            sm.checkResult(AuraTextUtil.splitSimpleLimitAndTrim(sm.input, sm.delimiter, sm.expectedSize));
        }
    }

    @Test
    @Deprecated
    public void testSplitReturnsModifiableCollection() {
        List<String> result = AuraTextUtil.splitSimple(",", "a,b");
        result.remove(result.size() - 1);
        assertEquals("result should have been left with only one element after splitting to two and removing the last one", 1, result.size());
        assertEquals("result should have been left with just 'a'", "a", result.get(0));
    }

    private final static StringPair[] INIT_CAP_PAIRS = new StringPair[] { new StringPair(null, null),
            new StringPair("", ""), new StringPair("A", "A"), new StringPair("a", "A"),
            new StringPair("Apple", "Apple"), new StringPair("apple", "Apple"), new StringPair("1", "1"),
            new StringPair("=", "="), new StringPair(" a", " a"), new StringPair("ñ", "Ñ"), };

    @Test
    public void testInitCap() {
        for (StringPair p : INIT_CAP_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.initCap(p.input));
        }
    }

    private final static StringPair[] URL_DECODE_PAIRS = new StringPair[] { new StringPair("", ""), };

    @Test
    public void testURLDecodeNull() {
        try {
            AuraTextUtil.urldecode(null);
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void testURLDecode() {
        for (StringPair p : URL_DECODE_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.urldecode(p.input));
        }
    }

    private final static StringPair[] URL_ENCODE_PAIRS = new StringPair[] { new StringPair("", ""), };

    @Test
    public void testURLEncodeNull() {
        try {
            AuraTextUtil.urlencode(null);
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void testURLEncode() {
        for (StringPair p : URL_ENCODE_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.urlencode(p.input));
        }
    }

    @Test
    public void testvalidateAttributeName() {
        assertTrue("Attribute name<name> should be valid ", AuraTextUtil.validateAttributeName("name"));
        assertTrue("Attribute name<_name> should be valid", AuraTextUtil.validateAttributeName("_name"));
        assertTrue("Attribute name<na-me> should be valid", AuraTextUtil.validateAttributeName("na-me"));
        assertTrue("Attribute name<na_me> should be valid", AuraTextUtil.validateAttributeName("na_me"));
        assertTrue("Attribute name<nam123e> should be valid", AuraTextUtil.validateAttributeName("nam123e"));
        assertFalse("Attribute name <na$:me> should be invalid", AuraTextUtil.validateAttributeName("na$:me"));
        assertFalse("Attribute name <123name> should be invalid", AuraTextUtil.validateAttributeName("123name"));
        assertFalse("Attribute name <nam e> should be invalid", AuraTextUtil.validateAttributeName("nam e"));
        assertFalse("Attribute name <nam'a'e> should be invalid", AuraTextUtil.validateAttributeName("nam'a'e"));
    }

    private final static StringPair[] JSON_FUNCTION_ENCODE_PAIRS = new StringPair[] {
            new StringPair("", ""),
            new StringPair("\u2029", "\\u2029"),
            new StringPair("\u2028", "\n"),
            new StringPair("\u0000", ""),
            new StringPair("*/", "\\u002A/"),
    };

    @Test
    public void testJSONFunction() {
        for (StringPair p : JSON_FUNCTION_ENCODE_PAIRS) {
            assertEquals(p.expected, AuraTextUtil.escapeForJSONFunction(p.input));
        }
    }

    @Test
    public void testStringsHaveSameContent() {
        assertTrue(AuraTextUtil.stringsHaveSameContent("abc", "abc"));
        assertTrue(AuraTextUtil.stringsHaveSameContent("abc", "cba"));
        assertFalse(AuraTextUtil.stringsHaveSameContent("abc", "def"));
    }

    @Test
    public void testIsValidJsIdentifier() throws Exception {
        assertTrue("variable is a valid js identifier", AuraTextUtil.isValidJsIdentifier("variable"));
        assertTrue("$ is a valid js identifier", AuraTextUtil.isValidJsIdentifier("$"));
        assertTrue("$$ is a valid js identifier", AuraTextUtil.isValidJsIdentifier("$$"));
        assertTrue("_ is a valid js identifier", AuraTextUtil.isValidJsIdentifier("_"));
        assertTrue("_$_1A23$_a2Bc is a valid js identifier", AuraTextUtil.isValidJsIdentifier("_$_1A23$_a2Bc"));
        assertFalse("{} is not a valid js identifier", AuraTextUtil.isValidJsIdentifier("{}"));
        assertFalse("function is not a valid js identifier", AuraTextUtil.isValidJsIdentifier("function() {}"));
        assertFalse("null String is not a valid js identifier", AuraTextUtil.isValidJsIdentifier(null));
    }
}
