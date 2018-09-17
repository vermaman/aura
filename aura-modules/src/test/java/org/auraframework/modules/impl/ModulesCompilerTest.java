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
package org.auraframework.modules.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import org.auraframework.def.module.ModuleDef.CodeType;
import org.auraframework.modules.ModulesCompilerData;
import org.auraframework.service.LoggingService;
import org.lwc.bundle.BundleType;
import org.auraframework.tools.node.api.NodeLambdaFactory;
import org.auraframework.tools.node.impl.sidecar.NodeLambdaFactorySidecar;
import org.auraframework.util.test.util.UnitTestCase;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * Tests for the ModulesCompiler implementations
 */
public class ModulesCompilerTest extends UnitTestCase {

    // NOTE: use to specify which service type to use when running tests
    private static final NodeLambdaFactory FACTORY = NodeLambdaFactorySidecar.INSTANCE;

    @Inject
    private LoggingService loggingService;

    @Test
    public void test() throws Exception {
        String expected = Files.toString(getResourceFile("/testdata/modules/moduletest/expected.js"), Charsets.UTF_8);

        ModulesCompilerData compilerData = compileModule("modules/moduletest/moduletest");

        assertEquals(expected.trim(), compilerData.codes.get(CodeType.DEV).trim());
        assertEquals("[lwc, x/test]", compilerData.bundleDependencies.toString());
    }

    @Test
    public void testWireMetadata() throws Exception {
        ModulesCompilerData compilerData = compileModule("modules/wireDecoratorTest/wireDecoratorTest");

        Set<ModulesCompilerData.WireDecoration> wireDecorations = compilerData.wireDecorations;
        assertEquals(1, wireDecorations.size());
        ModulesCompilerData.WireDecoration wireDecorator = wireDecorations.iterator().next();
        assertEquals("getRecord", wireDecorator.adapter.name);
        assertEquals("x-record-api", wireDecorator.adapter.reference);
        assertEquals(ImmutableMap.of("id", "recordId"), wireDecorator.params);
    }

    @Test
    public void testErrorInHtml() throws Exception {
        try {
            compileModule("modules/errorInHtml/errorInHtml");
            fail("should report a syntax error");
        } catch (Exception e) {
            e.printStackTrace();
            String message = Throwables.getRootCause(e).getMessage();
            assertTrue(message, message.contains(
                    "Invalid HTML syntax: non-void-html-element-start-tag-with-trailing-solidus. For more information, please visit https://html.spec.whatwg.org/multipage/parsing.html#parse-error-non-void-html-element-start-tag-with-trailing-solidus"));
        }
    }

    @Test
    public void testErrorInJs() throws Exception {
        try {
            compileModule("modules/errorInJs/errorInJs");
            fail("should report a syntax error");
        } catch (Exception e) {
            String message = Throwables.getRootCause(e).getMessage();
            // since linting is disabled for inernal bundle types, the compiler will throw instead of producing diagnostic
            assertEquals(message.contains("bad result: { SyntaxError: Unexpected token, expected { (2:4)"), true);
        }
    }

    @Test
    public void testInternalLintingInJs() throws Exception {
        ModulesCompiler compiler = new ModulesCompilerNode(FACTORY, loggingService);
        String entry = "modules/errorInJs/errorInJs.js";
        String sourceClass = "console.log('error');";

        Map<String, String> sources = new HashMap<>();
        sources.put("modules/errorInJs/errorInJs.js", sourceClass);

        ModulesCompilerData result = compiler.compile(entry, sources, BundleType.internal);
        assertEquals(result.compilerReport.diagnostics.size(), 0);
        assertTrue(result.compilerReport.success);
    }

    @Test
    public void testPlatformLintingInJs() throws Exception {
        ModulesCompiler compiler = new ModulesCompilerNode(FACTORY, loggingService);
        String entry = "modules/errorInJs/errorInJs.js";
        String sourceClass = "console.log('error');";

        Map<String, String> sources = new HashMap<>();
        sources.put("modules/errorInJs/errorInJs.js", sourceClass);

        try {
            compiler.compile(entry, sources, BundleType.platform);
            fail("should report a syntax error");
        } catch (Exception e) {
            String message = Throwables.getRootCause(e).getMessage();
            assertEquals(message, "Invalid syntax encountered in the 'errorInJs.js' file of the 'modules:errorInJs' component: \n" +
                    "Unexpected console statement.");
        }
    }

    private ModulesCompilerData compileModule(String modulePath) throws Exception {
        ModulesCompiler compiler = new ModulesCompilerNode(FACTORY, loggingService);
        String jsModule = modulePath + ".js";
        String htmlModule = modulePath + ".html";
        String sourceTemplate = Files.toString(getResourceFile("/testdata/" + htmlModule), Charsets.UTF_8);
        String sourceClass = Files.toString(getResourceFile("/testdata/" + jsModule), Charsets.UTF_8);
        Map<String, String> sources = new HashMap<>();
        sources.put(jsModule, sourceClass);
        sources.put(htmlModule, sourceTemplate);

        return compiler.compile(jsModule, sources);
    }

}
