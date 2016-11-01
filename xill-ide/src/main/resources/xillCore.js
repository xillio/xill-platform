/*
 * Copyright (C) 2014 Xillio (support@xillio.com)
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
var xillCoreOverride = null;

/**
 * This object represents a communication interface to the xill back-end.
 * This here you can see a default implementation to allow running in the browser
 */
window.xillCore = {
    debug: function (message) {
        this.log("DEBUG", message);
    },
    info: function (message) {
        this.log("INFO", message);
    },
    warn: function (message) {
        this.log("WARN", message);
    },
    error: function (message) {
        this.log("ERROR", message);
    },
    getKeywords: function () {
        return ["as", "break", "callbot", "continue", "else", "filter", "foreach", "function", "if", "in", "include", "map", "private", "return", "use", "var", "while", "runBulk", "do", "fail", "success", "finally", "consume", "collect", "peek", "reduce"]
    },
    getPluginNames: function () {
        if (xillCoreOverride) {
            return xillCoreOverride.getPluginNames();
        }
        return ["System"];
    },
    getLanguageConstants: function () {
        return ["ATOMIC", "LIST", "NaN", "OBJECT", "argument", "null", "true", "false"]
    },
    getCompletions: function (state, session, pos, prefix, aceCallback) {
        if (xillCoreOverride) {

            xillCoreOverride.getCompletions({
                state: state,
                session: session,
                prefix: prefix,
                column: pos.column,
                row: pos.row,
                currentLine: session.getLine(pos.row),
                callback: function (name, value, score, meta) {
                    aceCallback(null, {name: name, caption: name, snippet: value, score: score, meta: meta});
                }
            });
        }
    }
};
window.initConsole = function () {
    if (xillCoreOverride != null) {
        console.log = function () {
            var output = "[JAVASCRIPT]";
            for (var key in arguments) {
                output += " " + arguments[key];
            }
            xillCoreOverride.info(output);
        };
        console.error = console.log;
        console.warn = console.log;
    }
}
