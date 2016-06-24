ace.define("ace/ext/breakpoints", function (require, exports) {
    "use strict";

    editor.on("guttermousedown", function (e) {
        var target = e.domEvent.target;

        // Check if we clicked an existing line
        if (target.className.indexOf("ace_gutter-cell") == -1)
            return;

        // Check if editor is focused
        if (!editor.isFocused())
            return;

        // Check if we clicked the right area in the gutter
        if (e.clientX > 45 + target.getBoundingClientRect().left)
            return;

        var row = e.getDocumentPosition().row;

        if (hasBreakpoint(row)) {
            removeBreakpoint(row);
        } else {
            if (canHaveBreakpoint(row)) {
                addBreakpoint(row);
            }
        }

        e.stop()
    });

    editor.on("change", function (e) {

        var delta = e.end.row - e.start.row;

        if (delta == 0) {
            return;
        }


        switch (e.action) {
            case "insert":
                shiftBreakpoints(e.start.row, delta);
                break;
            case "remove":
                removeBreakpoints(e.start.row, e.end.row);
                shiftBreakpoints(e.end.row, -delta);
                break;
            default:
                console.log("Unknown action: " + e.action);
                break;
        }
    });

    /**
     * Remove all breakpoints that are in a certain range.
     * @param start
     * @param end
     */
    function removeBreakpoints(start, end) {
        var breakpoints = editor.session.getBreakpoints();
        var madeChange = false;
        var newBreakpoints = [];

        breakpoints.forEach(
            function (value, key) {
                if (value) {
                    if (key >= end || key < start) {
                        newBreakpoints.push(key);
                    } else {
                        madeChange = true;
                    }
                }
            }
        );

        if (madeChange) {
            editor.session.setBreakpoints(newBreakpoints);
        }
    }

    /**
     * Shift all breakpoints after a certain point.
     * @param firstLine
     * @param delta
     */
    function shiftBreakpoints(firstLine, delta) {
        var breakpoints = editor.session.getBreakpoints();
        var newBreakpoints = [];

        breakpoints.forEach(
            function (value, key) {
                if (value) {
                    var newRow = key;
                    if (newRow >= firstLine) {
                        newRow += delta;
                    }
                    newBreakpoints.push(newRow);
                }
            }
        );

        if (newBreakpoints.length > 0) {
            editor.session.setBreakpoints(newBreakpoints);
        }
    }

    /**
     * Check if a breakpoint exists on a row.
     * @param row
     * @returns {boolean}
     */
    function hasBreakpoint(row) {
        return typeof(editor.session.getBreakpoints()[row]) !== 'undefined';
    }

    /**
     * Set a breakpoint on the editor.
     * @param row
     */
    function addBreakpoint(row) {
        editor.session.setBreakpoint(row);
    }

    /**
     * Clear a breakpoint.
     * @param row
     */
    function removeBreakpoint(row) {
        editor.session.clearBreakpoint(row);
    }

    /**
     * Check if a row is eligible to get a breakpoint
     * @param row
     * @returns {boolean}
     */
    function canHaveBreakpoint(row) {
        var currentLine = editor.session.getLine(row).trim();
        var noCommentLine = currentLine.replace(/\/\/.*/, "").trim();
        return currentLine && noCommentLine;
    }

});