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
ace.define("ace/snippets/snippets",["require","exports","module"], function(require, exports, module) {
"use strict";

exports.snippetText = "# snippets for making snippets :)\n\
snippet snip\n\
	snippet ${1:trigger}\n\
		${2}\n\
snippet msnip\n\
	snippet ${1:trigger} ${2:description}\n\
		${3}\n\
snippet v\n\
	{VISUAL}\n\
";
exports.scope = "snippets";

});
