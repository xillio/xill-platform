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
ace.define("ace/theme/xillio",["require","exports","module","ace/lib/dom"], function(require, exports, module) {

exports.isDark = false;
exports.cssClass = "ace-xillio";
exports.cssText = "\
.ace-xillio .ace_gutter {\
    background: #F5F5F5;\
    color: #3f4c54;\
    overflow : hidden;\
}\
.ace-xillio .ace_print-margin {\
    width: 1px;\
    background: #f0f0f0;\
}\
.ace-xillio {\
    background-color: #FFFFFF;\
    color: black;\
}\
.ace-xillio .ace_cursor { \
    color: black;\
}\
.ace-xillio .ace_invisible { \
    color: rgb(191, 191, 191);\
}\
\
.ace-xillio .ace_line span {\
    display: inline-block;\
}\
\
/* highlighting */\
.ace-xillio .ace_constant.ace_buildin { \
    color: rgb(88, 72, 246);\
}\
.ace-xillio .ace_constant.ace_language { \
    color: rgb(88, 92, 246);\
}\
.ace-xillio .ace_constant.ace_library { \
    color: rgb(6, 150, 14);\
}\
.ace-xillio .ace_invalid {\
    background-color: rgb(153, 0, 0);\
    color: white;\
}\
.ace-xillio .ace_fold { \
    \
}\
.ace-xillio .ace_support.ace_function {\
    color: rgb(60, 76, 114);\
}\
.ace-xillio .ace_support.ace_constant { \
    color: rgb(6, 150, 14);\
}\
.ace-xillio .ace_support.ace_type,\
.ace-xillio .ace_support.ace_class,\
.ace-xillio .ace_support.ace_other {\
    color: rgb(109, 121, 222);\
}\
.ace-xillio .ace_variable.ace_parameter {\
    font-style: italic;\
    color: #FD971F;\
}\
.ace-xillio .ace_keyword.ace_operator { \
    color: rgb(104, 118, 135);\
}\
.ace-xillio .ace_comment { \
    color: #5FA9BC;\
}\
.ace-xillio .ace_comment.ace_doc {\
    color: #5FA9BC;\
}\
.ace-xillio .ace_comment.ace_doc.ace_tag {\
    color: #5FA9BC;\
}\
.ace-xillio .ace_constant.ace_numeric {\
    color: rgb(0, 0, 205);\
}\
.ace-xillio .ace_variable {\
    color: rgb(49, 132, 149);\
}\
.ace-xillio .ace_xml-pe {\
    color: rgb(104, 104, 91);\
}\
.ace-xillio .ace_entity.ace_name.ace_function {\
    color: #0000A2;\
}\
.ace-xillio .ace_heading {\
    color: rgb(12, 7, 255);\
}\
.ace-xillio .ace_list {\
    color: rgb(185, 6, 144);\
}\
.ace-xillio .ace_keyword { \
    color: rgb(147, 15, 128);\
}\
.ace-xillio .ace_string.ace_regex {\
    color: rgb(255, 0, 0);\
}\
.ace-xillio .ace_string {\
    color: #1A1AA6;\
}\
.ace-xillio .ace_plugin { \
    color: rgb(150,150,150);\
}\
.ace-xillio .ace_construct { \
    color: rgb(85,85,85);\
}\
.ace-xillio .ace_lparen { \
    color: #DC042B; \
}\
.ace-xillio .ace_rparen { \
    color: #DC042B; \
}\
.ace-xillio .ace_functionArgument { \
    font-style: italic; \
    margin-right: 1px; \
}\
\
/* code selection, end brace markers, etc */\
.ace-xillio .ace_marker-layer .ace_selection {\
    background: rgba(185, 227, 238, 0.5);\
}\
.ace-xillio .ace_marker-layer .ace_selected-word {\
    background: #ffffff;\
    border: 1px solid #75c9de;\
}\
.ace-xillio .ace_marker-layer .ace_step {\
    background: rgb(252, 255, 0);\
}\
.ace-xillio .ace_marker-layer .ace_stack {\
    background: rgb(164, 229, 101);\
}\
.ace-xillio .ace_marker-layer .ace_bracket {\
    margin: -1px 0 0 -1px;\
    border: 1px solid rgb(192, 192, 192);\
}\
\
/* line highlights */\
.ace-xillio .ace_marker-layer .ace_active-line { \
    background:rgba(0,0,0,0.05); \
}\
.ace-xillio .ace_gutter-active-line { \
    background-color : #dcdcdc; \
}\
\
.ace-xillio .ace_marker-layer .ace_error { \
    background-color : #ffdcdc;\
    position: absolute;\
}\
\
/* current line while running */\
.ace-xillio .ace_marker-layer .ace_highlight { \
    background-color : #FFFFBF;\
    position: absolute;\
}\
\
.ace-xillio .ace_entity.ace_other.ace_attribute-name { \
    color: #994409;\
}\
\
/* tab marker */\
.ace-xillio .ace_indent-guide { \
    border-right: 1px solid rgba(0, 0, 0, 0.1);\
    margin-right: -1px;\
}\
\
/* auto complete */\
.ace_editor.ace-xillio.ace_autocomplete {\
    background: #ffffff;\
    border: 1px solid #A4A4A4;\
    box-shadow: 1px 1px 6px -2px rgba(0, 0, 0, 1);\
    color: #444444;\
    line-height: 1.5;\
    position: fixed;\
    width: 280px;\
    z-index: 200000;\
}\
.ace_editor.ace-xillio.ace_autocomplete .ace_marker-layer .ace_line-hover {\
    background: #f0f0f0;\
    border: 0 none;\
    margin: 0;\
}\
.ace_editor.ace-xillio.ace_autocomplete .ace_marker-layer .ace_active-line {\
    background-color: #dcdcdc;\
    border: 0 none;\
}\
.ace_editor.ace-xillio.ace_autocomplete .ace_completion-highlight {\
    color: #000;\
    font-weight: bold;\
}";

var dom = require("../lib/dom");
dom.importCssString(exports.cssText, exports.cssClass);
});
