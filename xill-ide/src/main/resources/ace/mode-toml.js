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
ace.define("ace/mode/toml_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var TomlHighlightRules = function() {
    var keywordMapper = this.createKeywordMapper({
        "constant.language.boolean": "true|false"
    }, "identifier");

    var identifierRe = "[a-zA-Z\\$_\u00a1-\uffff][a-zA-Z\\d\\$_\u00a1-\uffff]*\\b";

    this.$rules = {
    "start": [
        {
            token: "comment.toml",
            regex: /#.*$/
        },
        {
            token : "string",
            regex : '"(?=.)',
            next  : "qqstring"
        },
        {
            token: ["variable.keygroup.toml"],
            regex: "(?:^\\s*)(\\[\\[([^\\]]+)\\]\\])"
        },
        {
            token: ["variable.keygroup.toml"],
            regex: "(?:^\\s*)(\\[([^\\]]+)\\])"
        },
        {
            token : keywordMapper,
            regex : identifierRe
        },
        {
           token : "support.date.toml",
           regex: "\\d{4}-\\d{2}-\\d{2}(T)\\d{2}:\\d{2}:\\d{2}(Z)"
        },
        {
           token: "constant.numeric.toml",
           regex: "-?\\d+(\\.?\\d+)?"
        }
    ],
    "qqstring" : [
        {
            token : "string",
            regex : "\\\\$",
            next  : "qqstring"
        },
        {
            token : "constant.language.escape",
            regex : '\\\\[0tnr"\\\\]'
        },
        {
            token : "string",
            regex : '"|$',
            next  : "start"
        },
        {
            defaultToken: "string"
        }
    ]
    }

};

oop.inherits(TomlHighlightRules, TextHighlightRules);

exports.TomlHighlightRules = TomlHighlightRules;
});

ace.define("ace/mode/folding/ini",["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"], function(require, exports, module) {
"use strict";

var oop = require("../../lib/oop");
var Range = require("../../range").Range;
var BaseFoldMode = require("./fold_mode").FoldMode;

var FoldMode = exports.FoldMode = function() {
};
oop.inherits(FoldMode, BaseFoldMode);

(function() {

    this.foldingStartMarker = /^\s*\[([^\])]*)]\s*(?:$|[;#])/;

    this.getFoldWidgetRange = function(session, foldStyle, row) {
        var re = this.foldingStartMarker;
        var line = session.getLine(row);
        
        var m = line.match(re);
        
        if (!m) return;
        
        var startName = m[1] + ".";
        
        var startColumn = line.length;
        var maxRow = session.getLength();
        var startRow = row;
        var endRow = row;

        while (++row < maxRow) {
            line = session.getLine(row);
            if (/^\s*$/.test(line))
                continue;
            m = line.match(re);
            if (m && m[1].lastIndexOf(startName, 0) !== 0)
                break;

            endRow = row;
        }

        if (endRow > startRow) {
            var endColumn = session.getLine(endRow).length;
            return new Range(startRow, startColumn, endRow, endColumn);
        }
    };

}).call(FoldMode.prototype);

});

ace.define("ace/mode/toml",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/toml_highlight_rules","ace/mode/folding/ini"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var TomlHighlightRules = require("./toml_highlight_rules").TomlHighlightRules;
var FoldMode = require("./folding/ini").FoldMode;

var Mode = function() {
    this.HighlightRules = TomlHighlightRules;
    this.foldingRules = new FoldMode();
};
oop.inherits(Mode, TextMode);

(function() {
    this.lineCommentStart = "#";
    this.$id = "ace/mode/toml";
}).call(Mode.prototype);

exports.Mode = Mode;
});
