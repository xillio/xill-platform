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
ace.define("ace/snippets/sql",["require","exports","module"], function(require, exports, module) {
"use strict";

exports.snippetText = "snippet tbl\n\
	create table ${1:table} (\n\
		${2:columns}\n\
	);\n\
snippet col\n\
	${1:name}	${2:type}	${3:default ''}	${4:not null}\n\
snippet ccol\n\
	${1:name}	varchar2(${2:size})	${3:default ''}	${4:not null}\n\
snippet ncol\n\
	${1:name}	number	${3:default 0}	${4:not null}\n\
snippet dcol\n\
	${1:name}	date	${3:default sysdate}	${4:not null}\n\
snippet ind\n\
	create index ${3:$1_$2} on ${1:table}(${2:column});\n\
snippet uind\n\
	create unique index ${1:name} on ${2:table}(${3:column});\n\
snippet tblcom\n\
	comment on table ${1:table} is '${2:comment}';\n\
snippet colcom\n\
	comment on column ${1:table}.${2:column} is '${3:comment}';\n\
snippet addcol\n\
	alter table ${1:table} add (${2:column} ${3:type});\n\
snippet seq\n\
	create sequence ${1:name} start with ${2:1} increment by ${3:1} minvalue ${4:1};\n\
snippet s*\n\
	select * from ${1:table}\n\
";
exports.scope = "sql";

});
