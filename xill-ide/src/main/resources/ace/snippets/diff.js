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
ace.define("ace/snippets/diff",["require","exports","module"], function(require, exports, module) {
"use strict";

exports.snippetText = "# DEP-3 (http://dep.debian.net/deps/dep3/) style patch header\n\
snippet header DEP-3 style header\n\
	Description: ${1}\n\
	Origin: ${2:vendor|upstream|other}, ${3:url of the original patch}\n\
	Bug: ${4:url in upstream bugtracker}\n\
	Forwarded: ${5:no|not-needed|url}\n\
	Author: ${6:`g:snips_author`}\n\
	Reviewed-by: ${7:name and email}\n\
	Last-Update: ${8:`strftime(\"%Y-%m-%d\")`}\n\
	Applied-Upstream: ${9:upstream version|url|commit}\n\
\n\
";
exports.scope = "diff";

});
